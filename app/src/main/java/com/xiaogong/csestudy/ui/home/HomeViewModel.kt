package com.xiaogong.csestudy.ui.home

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.xiaogong.csestudy.CseApplication
import com.xiaogong.csestudy.data.model.ExamLevel
import com.xiaogong.csestudy.data.repository.QuestionRepository
import com.xiaogong.csestudy.data.repository.UserPreferencesRepository
import com.xiaogong.csestudy.data.repository.UserProgressRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val examLevel: ExamLevel = ExamLevel.INTERMEDIATE,
    val nickname: String = "",
    val avatarUri: String = "",
    val encouragingQuote: String = "",
    val todayAnswered: Int = 0,
    val todayCorrect: Int = 0,
    val streakDays: Int = 0,
    val totalQuestions: Int = 0,
    val wrongCount: Int = 0,
    val answeredQuestions: Int = 0,
    val totalCorrect: Int = 0,
    val totalAnswerTimes: Int = 0,
    val checkInDays: Int = 0
) {
    val todayAccuracy: Int get() =
        if (todayAnswered == 0) 0 else (todayCorrect * 100 / todayAnswered)

    /** 题库整体完成度 0f..1f */
    val overallProgress: Float get() =
        if (totalQuestions == 0) 0f
        else (answeredQuestions.toFloat() / totalQuestions).coerceIn(0f, 1f)

    val overallProgressPercent: Int get() = (overallProgress * 100).toInt()

    val overallAccuracy: Int get() =
        if (totalAnswerTimes == 0) 0 else (totalCorrect * 100 / totalAnswerTimes)
}

private val encouragingQuotes = listOf(
    "每天进步一点点，坚持就是胜利！",
    "学习很苦，但证书很甜，加油！",
    "今天的努力，是明天的底气。",
    "不积跬步，无以至千里。",
    "你比自己想象的更强大！",
    "每一个刷题的夜晚，都在为未来铺路。",
    "成功没有捷径，但有方法——坚持就是最好的方法。",
    "别怕错，错题是最好的老师。",
    "世上无难事，只要肯登攀。",
    "今天的学习，是对未来最好的投资。",
    "慢一点也没关系，别停下就好。",
    "现在多刷一题，考场上就多一分从容。",
    "厚积薄发，你的积累终会兑现。",
    "把书读薄，把题做透。",
    "自律的人，运气都不会太差。",
    "熬过枯燥的部分，就是收获的时候。",
    "会做的题再做一遍，不会的题弄懂它。",
    "你已经走了很远，别小看自己。"
)

private data class DailyStats(
    val totalQuestions: Int,
    val todayAnswered: Int,
    val todayCorrect: Int,
    val wrongCount: Int,
    val streakDays: Int,
    val checkInDays: Int
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val questionRepo: QuestionRepository,
    private val progressRepo: UserProgressRepository,
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(encouragingQuote = encouragingQuotes.random())
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** 点击鼓励语时换一句，保证与当前那句不同 */
    fun refreshQuote() {
        val current = _uiState.value.encouragingQuote
        val next = encouragingQuotes.filter { it != current }.randomOrNull() ?: current
        _uiState.update { it.copy(encouragingQuote = next) }
    }

    init {
        viewModelScope.launch {
            // 题量按考试等级可练习的科目范围统计（初级仅法规+管理）
            prefsRepo.examLevelFlow.flatMapLatest { level ->
                val lv = level ?: ExamLevel.INTERMEDIATE
                combine(
                    questionRepo.getCountForLevel(lv),
                    progressRepo.getTodayAnswered(lv),
                    progressRepo.getTodayCorrect(lv),
                    progressRepo.getWrongCount(lv),
                    progressRepo.getCheckInDays()
                ) { total, todayAns, todayCorr, wrong, checkIn ->
                    DailyStats(
                        totalQuestions = total,
                        todayAnswered = todayAns,
                        todayCorrect = todayCorr,
                        wrongCount = wrong,
                        streakDays = progressRepo.getStreakDays(),
                        checkInDays = checkIn
                    )
                }
            }.collect { stats ->
                _uiState.update {
                    it.copy(
                        totalQuestions = stats.totalQuestions,
                        todayAnswered = stats.todayAnswered,
                        todayCorrect = stats.todayCorrect,
                        streakDays = stats.streakDays,
                        wrongCount = stats.wrongCount,
                        checkInDays = stats.checkInDays
                    )
                }
            }
        }

        viewModelScope.launch {
            prefsRepo.examLevelFlow.flatMapLatest { level ->
                val lv = level ?: ExamLevel.INTERMEDIATE
                combine(
                    progressRepo.getAnsweredQuestionCount(lv),
                    progressRepo.getTotalAnswered(lv),
                    progressRepo.getTotalCorrect(lv)
                ) { answered, times, correct ->
                    Triple(answered, times, correct)
                }
            }.collect { (answered, times, correct) ->
                _uiState.update {
                    it.copy(
                        answeredQuestions = answered,
                        totalAnswerTimes = times,
                        totalCorrect = correct
                    )
                }
            }
        }

        viewModelScope.launch {
            prefsRepo.examLevelFlow.collect { level ->
                _uiState.update { it.copy(examLevel = level ?: ExamLevel.INTERMEDIATE) }
            }
        }

        viewModelScope.launch {
            prefsRepo.userProfileFlow.collect { profile ->
                _uiState.update { it.copy(nickname = profile.nickname, avatarUri = profile.avatarUri) }
            }
        }
    }
}

class HomeViewModelFactory(private val application: CseApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(
            application.container.questionRepository,
            application.container.userProgressRepository,
            application.container.userPreferencesRepository
        ) as T
    }
}
