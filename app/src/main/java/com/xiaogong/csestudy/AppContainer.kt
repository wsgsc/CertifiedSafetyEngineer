package com.xiaogong.csestudy

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.xiaogong.csestudy.data.local.AppDatabase
import com.xiaogong.csestudy.data.repository.QuestionRepository
import com.xiaogong.csestudy.data.repository.UserPreferencesRepository
import com.xiaogong.csestudy.data.repository.UserProgressRepository

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)
    val dataStore = context.dataStore

    val userPreferencesRepository = UserPreferencesRepository(context.dataStore)

    val questionRepository = QuestionRepository(database.questionDao())

    val userProgressRepository = UserProgressRepository(
        userAnswerDao = database.userAnswerDao(),
        wrongQuestionDao = database.wrongQuestionDao(),
        favoriteQuestionDao = database.favoriteQuestionDao(),
        studyRecordDao = database.studyRecordDao()
    )
}
