package com.xiaogong.csestudy.ui.study

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.xiaogong.csestudy.CseApplication
import com.xiaogong.csestudy.data.model.ExamLevel
import com.xiaogong.csestudy.data.model.Subject
import com.xiaogong.csestudy.data.repository.QuestionRepository
import com.xiaogong.csestudy.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudyViewModel(
    private val questionRepo: QuestionRepository,
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    // 科目列表及各科题目数量
    private val _subjectCounts = MutableStateFlow<Map<Subject, Int>>(emptyMap())
    val subjectCounts: StateFlow<Map<Subject, Int>> = _subjectCounts.asStateFlow()

    // 当前科目的章节列表
    private val _chapters = MutableStateFlow<List<String>>(emptyList())
    val chapters: StateFlow<List<String>> = _chapters.asStateFlow()

    // 当前章节的知识点列表
    private val _knowledgePoints = MutableStateFlow<List<String>>(emptyList())
    val knowledgePoints: StateFlow<List<String>> = _knowledgePoints.asStateFlow()

    init {
        loadSubjectCounts()
    }

    private fun loadSubjectCounts() {
        viewModelScope.launch {
            val level = prefsRepo.examLevelFlow.first() ?: ExamLevel.INTERMEDIATE
            val counts = Subject.forLevel(level).associateWith { subject ->
                questionRepo.getCountBySubject(subject).first()
            }
            _subjectCounts.value = counts
        }
    }

    fun loadChapters(subjectName: String) {
        val subject = Subject.valueOf(subjectName)
        viewModelScope.launch {
            _chapters.value = questionRepo.getChaptersBySubject(subject).first()
        }
    }

    fun loadKnowledgePoints(subjectName: String, chapterName: String) {
        val subject = Subject.valueOf(subjectName)
        viewModelScope.launch {
            _knowledgePoints.value = questionRepo.getKnowledgePoints(subject, chapterName).first()
        }
    }
}

class StudyViewModelFactory(private val application: CseApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return StudyViewModel(
            application.container.questionRepository,
            application.container.userPreferencesRepository
        ) as T
    }
}
