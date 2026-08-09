package com.xiaogong.csestudy.ui.main

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.xiaogong.csestudy.CseApplication
import com.xiaogong.csestudy.data.model.ExamLevel
import com.xiaogong.csestudy.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ExamLevelState {
    object Loading : ExamLevelState()
    object NotSelected : ExamLevelState()
    data class Selected(val level: ExamLevel) : ExamLevelState()
}

sealed class ProfileState {
    object Loading : ProfileState()
    object NotSet : ProfileState()
    object Ready : ProfileState()
}

class MainViewModel(private val prefsRepo: UserPreferencesRepository) : ViewModel() {

    val examLevelState: StateFlow<ExamLevelState> = prefsRepo.examLevelFlow
        .map { level -> if (level == null) ExamLevelState.NotSelected else ExamLevelState.Selected(level) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ExamLevelState.Loading)

    val profileState: StateFlow<ProfileState> = prefsRepo.userProfileFlow
        .map { profile -> if (profile.nickname.isBlank()) ProfileState.NotSet else ProfileState.Ready }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ProfileState.Loading)

    fun saveLevel(level: ExamLevel) {
        viewModelScope.launch { prefsRepo.saveExamLevel(level) }
    }

    fun saveProfile(nickname: String, avatarUri: String) {
        viewModelScope.launch { prefsRepo.saveUserProfile(nickname, avatarUri) }
    }
}

class MainViewModelFactory(private val application: CseApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(application.container.userPreferencesRepository) as T
    }
}
