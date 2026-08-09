package com.xiaogong.csestudy.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.xiaogong.csestudy.CseApplication
import com.xiaogong.csestudy.data.model.ExamLevel
import com.xiaogong.csestudy.data.repository.QuestionRepository
import com.xiaogong.csestudy.data.repository.UserPreferencesRepository
import com.xiaogong.csestudy.data.repository.UserProgressRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class QuizModeUiState(
    val totalCount: Int = 0,
    val totalAnswered: Int = 0,
    val totalCorrect: Int = 0,
    val sequentialProgress: Int = 0   // last saved index for all-questions sequential mode
) {
    val accuracyPercent: Int get() = if (totalAnswered == 0) 0 else totalCorrect * 100 / totalAnswered
}

@OptIn(ExperimentalCoroutinesApi::class)
class QuizModeViewModel(
    private val questionRepo: QuestionRepository,
    private val progressRepo: UserProgressRepository,
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    // 题量按考试等级可练习的科目范围统计（初级仅法规+管理）
    val uiState: StateFlow<QuizModeUiState> = prefsRepo.examLevelFlow
        .flatMapLatest { level ->
            val lv = level ?: ExamLevel.INTERMEDIATE
            combine(
                questionRepo.getCountForLevel(lv),
                progressRepo.getTotalAnswered(lv),
                progressRepo.getTotalCorrect(lv)
            ) { total, answered, correct ->
                val savedIndex = prefsRepo.getSequentialProgress("")
                QuizModeUiState(
                    totalCount = total,
                    totalAnswered = answered,
                    totalCorrect = correct,
                    sequentialProgress = savedIndex
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuizModeUiState())
}

class QuizModeViewModelFactory(private val application: CseApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return QuizModeViewModel(
            application.container.questionRepository,
            application.container.userProgressRepository,
            application.container.userPreferencesRepository
        ) as T
    }
}
