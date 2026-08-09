package com.xiaogong.csestudy.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import com.xiaogong.csestudy.data.model.ExamLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserProfile(
    val nickname: String = "",
    val avatarUri: String = ""
)

data class ReminderTime(
    val hour1: Int = 9,
    val minute1: Int = 0,
    val hour2: Int = 22,
    val minute2: Int = 0
)

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val EXAM_LEVEL_KEY = stringPreferencesKey("exam_level")
        private val NICKNAME_KEY = stringPreferencesKey("nickname")
        private val AVATAR_URI_KEY = stringPreferencesKey("avatar_uri")
        private val REMINDER_ENABLED_KEY = booleanPreferencesKey("reminder_enabled")
        private val REMINDER_HOUR1_KEY = intPreferencesKey("reminder_hour1")
        private val REMINDER_MINUTE1_KEY = intPreferencesKey("reminder_minute1")
        private val REMINDER_HOUR2_KEY = intPreferencesKey("reminder_hour2")
        private val REMINDER_MINUTE2_KEY = intPreferencesKey("reminder_minute2")
    }

    val examLevelFlow: Flow<ExamLevel?> = dataStore.data.map { prefs ->
        prefs[EXAM_LEVEL_KEY]?.let { runCatching { ExamLevel.valueOf(it) }.getOrNull() }
    }

    val userProfileFlow: Flow<UserProfile> = dataStore.data.map { prefs ->
        UserProfile(
            nickname = prefs[NICKNAME_KEY] ?: "",
            avatarUri = prefs[AVATAR_URI_KEY] ?: ""
        )
    }

    suspend fun saveExamLevel(level: ExamLevel) {
        dataStore.edit { prefs -> prefs[EXAM_LEVEL_KEY] = level.name }
    }

    val reminderEnabledFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[REMINDER_ENABLED_KEY] ?: true
    }

    suspend fun saveUserProfile(nickname: String, avatarUri: String) {
        dataStore.edit { prefs ->
            prefs[NICKNAME_KEY] = nickname
            prefs[AVATAR_URI_KEY] = avatarUri
        }
    }

    val reminderTimeFlow: Flow<ReminderTime> = dataStore.data.map { prefs ->
        ReminderTime(
            hour1 = prefs[REMINDER_HOUR1_KEY] ?: 9,
            minute1 = prefs[REMINDER_MINUTE1_KEY] ?: 0,
            hour2 = prefs[REMINDER_HOUR2_KEY] ?: 22,
            minute2 = prefs[REMINDER_MINUTE2_KEY] ?: 0
        )
    }

    suspend fun saveReminderEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[REMINDER_ENABLED_KEY] = enabled }
    }

    suspend fun saveReminderTime(slot: Int, hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            if (slot == 1) {
                prefs[REMINDER_HOUR1_KEY] = hour
                prefs[REMINDER_MINUTE1_KEY] = minute
            } else {
                prefs[REMINDER_HOUR2_KEY] = hour
                prefs[REMINDER_MINUTE2_KEY] = minute
            }
        }
    }

    // ── 顺序练习进度 ─────────────────────────────────────────────

    suspend fun getSequentialProgress(param: String): Int {
        val key = intPreferencesKey("seq_progress_${param.ifBlank { "all" }}")
        return dataStore.data.first()[key] ?: 0
    }

    suspend fun saveSequentialProgress(param: String, index: Int) {
        val key = intPreferencesKey("seq_progress_${param.ifBlank { "all" }}")
        dataStore.edit { prefs -> prefs[key] = index }
    }

    suspend fun clearSequentialProgress(param: String) {
        val key = intPreferencesKey("seq_progress_${param.ifBlank { "all" }}")
        dataStore.edit { prefs -> prefs.remove(key) }
    }

    // ── 顺序练习统计（仅顺序练习模式的数据）──────────────────────

    private fun seqStatKey(param: String, field: String): String {
        val suffix = param.ifBlank { "all" }
        return "seq_${field}_$suffix"
    }

    fun getSeqAnswered(param: String): Flow<Int> = dataStore.data.map { prefs ->
        prefs[intPreferencesKey(seqStatKey(param, "answered"))] ?: 0
    }

    fun getSeqCorrect(param: String): Flow<Int> = dataStore.data.map { prefs ->
        prefs[intPreferencesKey(seqStatKey(param, "correct"))] ?: 0
    }

    suspend fun incrementSeqAnswered(param: String) {
        val key = intPreferencesKey(seqStatKey(param, "answered"))
        dataStore.edit { prefs -> prefs[key] = (prefs[key] ?: 0) + 1 }
    }

    suspend fun incrementSeqCorrect(param: String) {
        val key = intPreferencesKey(seqStatKey(param, "correct"))
        dataStore.edit { prefs -> prefs[key] = (prefs[key] ?: 0) + 1 }
    }
}
