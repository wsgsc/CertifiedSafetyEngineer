package com.xiaogong.csestudy.ui.profile

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.xiaogong.csestudy.CseApplication
import com.xiaogong.csestudy.data.local.entity.StudyRecordEntity
import com.xiaogong.csestudy.data.model.ExamLevel
import com.xiaogong.csestudy.data.repository.ReminderTime
import com.xiaogong.csestudy.data.repository.UserPreferencesRepository
import com.xiaogong.csestudy.data.repository.UserProfile
import com.xiaogong.csestudy.data.repository.UserProgressRepository
import com.xiaogong.csestudy.util.DailyReminderWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val nickname: String = "",
    val avatarUri: String = "",
    val totalAnswered: Int = 0,
    val totalCorrect: Int = 0,
    val streakDays: Int = 0,
    val favoriteCount: Int = 0,
    val examLevel: ExamLevel? = null,
    val weeklyStats: List<StudyRecordEntity> = emptyList(),
    val reminderEnabled: Boolean = true,
    val reminderHour1: Int = 9,
    val reminderMinute1: Int = 0,
    val reminderHour2: Int = 22,
    val reminderMinute2: Int = 0
) {
    val overallAccuracy: Int get() =
        if (totalAnswered == 0) 0 else totalCorrect * 100 / totalAnswered
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val app: Application,
    private val progressRepo: UserProgressRepository,
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            progressRepo.getWeeklyStats().collect { stats ->
                _uiState.update { it.copy(weeklyStats = stats) }
            }
        }
        viewModelScope.launch {
            // 统计随考试等级切换，只算该等级可练习科目的数据
            prefsRepo.examLevelFlow.flatMapLatest { level ->
                val lv = level ?: ExamLevel.INTERMEDIATE
                combine(
                    progressRepo.getTotalAnswered(lv),
                    progressRepo.getTotalCorrect(lv),
                    progressRepo.getFavoriteCount(lv)
                ) { total, correct, fav ->
                    ProgressSnapshot(total, correct, fav, level)
                }
            }.combine(prefsRepo.userProfileFlow) { snap, profile ->
                val streak = progressRepo.getStreakDays()
                ProfileState(snap.total, snap.correct, snap.fav, snap.level, profile, streak)
            }.combine(prefsRepo.reminderEnabledFlow) { state, enabled ->
                state to enabled
            }.combine(prefsRepo.reminderTimeFlow) { (state, enabled), time ->
                state.toUiState(enabled, time)
            }.collect { _uiState.value = it }
        }
    }

    private data class ProgressSnapshot(
        val total: Int,
        val correct: Int,
        val fav: Int,
        val level: ExamLevel?
    )

    private data class ProfileState(
        val total: Int,
        val correct: Int,
        val fav: Int,
        val level: ExamLevel?,
        val userProfile: UserProfile,
        val streak: Int
    ) {
        fun toUiState(reminderEnabled: Boolean, time: ReminderTime) = ProfileUiState(
            nickname = userProfile.nickname,
            avatarUri = userProfile.avatarUri,
            totalAnswered = total,
            totalCorrect = correct,
            streakDays = streak,
            favoriteCount = fav,
            examLevel = level,
            reminderEnabled = reminderEnabled,
            reminderHour1 = time.hour1,
            reminderMinute1 = time.minute1,
            reminderHour2 = time.hour2,
            reminderMinute2 = time.minute2
        )
    }

    fun saveProfile(nickname: String, avatarUri: String) {
        viewModelScope.launch { prefsRepo.saveUserProfile(nickname, avatarUri) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.saveReminderEnabled(enabled)
            if (enabled) {
                val s = _uiState.value
                DailyReminderWorker.scheduleBoth(app, s.reminderHour1, s.reminderMinute1, s.reminderHour2, s.reminderMinute2)
            } else {
                DailyReminderWorker.cancelAll(app)
            }
        }
    }

    fun setReminderTime(slot: Int, hour: Int, minute: Int) {
        viewModelScope.launch {
            prefsRepo.saveReminderTime(slot, hour, minute)
            if (_uiState.value.reminderEnabled) {
                val cur = _uiState.value
                val h1 = if (slot == 1) hour else cur.reminderHour1
                val m1 = if (slot == 1) minute else cur.reminderMinute1
                val h2 = if (slot == 2) hour else cur.reminderHour2
                val m2 = if (slot == 2) minute else cur.reminderMinute2
                DailyReminderWorker.scheduleBoth(app, h1, m1, h2, m2)
            }
        }
    }
}

class ProfileViewModelFactory(private val application: CseApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return ProfileViewModel(
            application,
            application.container.userProgressRepository,
            application.container.userPreferencesRepository
        ) as T
    }
}
