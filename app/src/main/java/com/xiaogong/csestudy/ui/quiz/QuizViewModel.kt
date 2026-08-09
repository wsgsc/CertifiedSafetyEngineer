package com.xiaogong.csestudy.ui.quiz

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.xiaogong.csestudy.CseApplication
import com.xiaogong.csestudy.data.model.Question
import com.xiaogong.csestudy.data.model.QuestionType
import com.xiaogong.csestudy.data.model.Subject
import com.xiaogong.csestudy.data.repository.QuestionRepository
import com.xiaogong.csestudy.data.repository.UserPreferencesRepository
import com.xiaogong.csestudy.data.repository.UserProgressRepository
import com.xiaogong.csestudy.ui.navigation.QuizMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class QuizUiState(
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptions: Set<String> = emptySet(),
    val isAnswered: Boolean = false,
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
    val answers: Map<Int, Set<String>> = emptyMap(),
    val correctMap: Map<Int, Boolean> = emptyMap(),
    val globalAnswered: Int = 0,
    val globalCorrect: Int = 0,
    val favoriteIds: Set<Long> = emptySet()
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
    val isCurrentFavorite: Boolean get() = currentQuestion?.id in favoriteIds
    val progress: String get() = "${currentIndex + 1} / ${questions.size}"
    val isCorrect: Boolean get() {
        val q = currentQuestion ?: return false
        return when (q.type) {
            QuestionType.MULTIPLE_CHOICE -> selectedOptions.sorted().joinToString("") == q.answer.toSortedSet().joinToString("")
            else -> selectedOptions.firstOrNull() == q.answer
        }
    }
    val hasPrev: Boolean get() = currentIndex > 0
    val hasAnsweredBefore: Boolean get() = currentIndex in answers && !isAnswered
    val answeredCount: Int get() = answers.size
    val correctCount: Int get() = correctMap.count { it.value }
    val sessionAccuracyPercent: Int get() = if (answeredCount == 0) 0 else correctCount * 100 / answeredCount
    val progressFraction: Float get() = if (questions.isEmpty()) 0f else (currentIndex + 1).toFloat() / questions.size
    val globalAccuracyPercent: Int get() = if (globalAnswered == 0) 0 else globalCorrect * 100 / globalAnswered
}

class QuizViewModel(
    private val questionRepo: QuestionRepository,
    private val progressRepo: UserProgressRepository,
    private val prefsRepo: UserPreferencesRepository,
    private val mode: QuizMode,
    private val param: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()
    val quizMode: QuizMode get() = mode

    init {
        loadQuestions()

        viewModelScope.launch {
            progressRepo.getFavoriteIdsFlow().collect { ids ->
                _uiState.update { it.copy(favoriteIds = ids) }
            }
        }
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            val questions = when (mode) {
                QuizMode.SEQUENTIAL -> {
                    if (param.isNotBlank()) {
                        questionRepo.getBySubject(Subject.valueOf(param)).first()
                    } else {
                        questionRepo.getAll().first()
                    }
                }
                QuizMode.RANDOM -> questionRepo.getRandom(20)
                QuizMode.CHAPTER -> {
                    val parts = param.split("|")
                    if (parts.size == 2) {
                        questionRepo.getByChapter(Subject.valueOf(parts[0]), parts[1]).first()
                    } else questionRepo.getRandom(20)
                }
                QuizMode.KNOWLEDGE -> {
                    questionRepo.getByKnowledgePoint(param).first()
                }
                QuizMode.WRONG -> {
                    val ids = progressRepo.getWrongQuestionIds()
                    questionRepo.getByIds(ids)
                }
                QuizMode.FAVORITE -> {
                    val ids = progressRepo.getFavoriteIds()
                    questionRepo.getByIds(ids)
                }
                QuizMode.REDO -> {
                    val id = param.toLongOrNull()
                    if (id != null) questionRepo.getByIds(listOf(id)) else emptyList()
                }
            }
            val savedIndex = if (mode == QuizMode.SEQUENTIAL) {
                prefsRepo.getSequentialProgress(param).coerceAtMost((questions.size - 1).coerceAtLeast(0))
            } else 0

            // 顺序练习：恢复历史作答，已做过的题直接展示答案与解析
            val history = if (mode == QuizMode.SEQUENTIAL) {
                val latest = progressRepo.getLatestAnswerMap()
                questions.withIndex().mapNotNull { (idx, q) ->
                    latest[q.id]?.let { idx to it }
                }.toMap()
            } else emptyMap()

            _uiState.update {
                it.copy(
                    questions = questions,
                    currentIndex = savedIndex,
                    isLoading = false,
                    answers = history.mapValues { (_, a) -> a.userAnswer.map(Char::toString).toSet() },
                    correctMap = history.mapValues { (_, a) -> a.isCorrect },
                    selectedOptions = history[savedIndex]
                        ?.userAnswer?.map(Char::toString)?.toSet() ?: emptySet(),
                    isAnswered = savedIndex in history
                )
            }

            if (mode == QuizMode.SEQUENTIAL) {
                launch {
                    prefsRepo.getSeqAnswered(param).collect { answered ->
                        _uiState.update { it.copy(globalAnswered = answered) }
                    }
                }
                launch {
                    prefsRepo.getSeqCorrect(param).collect { correct ->
                        _uiState.update { it.copy(globalCorrect = correct) }
                    }
                }
            }
        }
    }

    fun selectOption(option: String) {
        val state = _uiState.value
        if (state.isAnswered) return
        val q = state.currentQuestion ?: return

        val newSelected = when (q.type) {
            QuestionType.MULTIPLE_CHOICE ->
                if (option in state.selectedOptions) state.selectedOptions - option
                else state.selectedOptions + option
            else -> setOf(option)
        }
        _uiState.update { it.copy(selectedOptions = newSelected) }

        // 单答案题型点击即判卷，多选题仍需手动确认
        if (q.type != QuestionType.MULTIPLE_CHOICE) submitAnswer()
    }

    fun submitAnswer() {
        val state = _uiState.value
        if (state.isAnswered || state.selectedOptions.isEmpty()) return
        val q = state.currentQuestion ?: return

        val correct = state.isCorrect
        val idx = state.currentIndex
        _uiState.update {
            it.copy(
                isAnswered = true,
                answers = it.answers + (idx to state.selectedOptions),
                correctMap = it.correctMap + (idx to correct)
            )
        }

        viewModelScope.launch {
            val userAnswer = state.selectedOptions.sorted().joinToString("")
            progressRepo.recordAnswer(
                questionId = q.id,
                userAnswer = userAnswer,
                isCorrect = correct,
                subject = q.subject.name,
                chapter = q.chapter,
                knowledgePoint = q.knowledgePoint,
                quizMode = mode.name
            )
            if (mode == QuizMode.SEQUENTIAL) {
                prefsRepo.incrementSeqAnswered(param)
                if (correct) prefsRepo.incrementSeqCorrect(param)
            }
        }
    }

    fun prevQuestion() {
        val state = _uiState.value
        if (state.currentIndex == 0) return
        val newIdx = state.currentIndex - 1
        val prevAnswer = state.answers[newIdx]
        _uiState.update {
            it.copy(
                currentIndex = newIdx,
                selectedOptions = prevAnswer ?: emptySet(),
                isAnswered = prevAnswer != null
            )
        }
        if (mode == QuizMode.SEQUENTIAL) {
            viewModelScope.launch { prefsRepo.saveSequentialProgress(param, newIdx) }
        }
    }

    fun nextQuestion() {
        val state = _uiState.value

        // 多选题未判卷且已有选择时，先提交并展示解析，不翻页
        if (!state.isAnswered && state.selectedOptions.isNotEmpty()) {
            submitAnswer()
            return
        }

        if (state.currentIndex + 1 >= state.questions.size) {
            _uiState.update { it.copy(isFinished = true) }
            if (mode == QuizMode.SEQUENTIAL) {
                viewModelScope.launch { prefsRepo.clearSequentialProgress(param) }
            }
        } else {
            val newIdx = state.currentIndex + 1
            val nextAnswer = state.answers[newIdx]
            _uiState.update {
                it.copy(
                    currentIndex = newIdx,
                    selectedOptions = nextAnswer ?: emptySet(),
                    isAnswered = nextAnswer != null
                )
            }
            if (mode == QuizMode.SEQUENTIAL) {
                viewModelScope.launch { prefsRepo.saveSequentialProgress(param, newIdx) }
            }
        }
    }

    fun toggleFavorite() {
        val q = _uiState.value.currentQuestion ?: return
        viewModelScope.launch { progressRepo.toggleFavorite(q.id) }
    }
}

class QuizViewModelFactory(
    private val application: CseApplication,
    private val mode: QuizMode,
    private val param: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return QuizViewModel(
            application.container.questionRepository,
            application.container.userProgressRepository,
            application.container.userPreferencesRepository,
            mode, param
        ) as T
    }
}
