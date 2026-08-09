package com.xiaogong.csestudy

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.xiaogong.csestudy.util.DailyReminderWorker
import com.xiaogong.csestudy.util.JsonImporter

class CseApplication : Application() {
    lateinit var container: AppContainer
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applicationScope.launch {
            loadQuestionsIfNeeded()
            scheduleReminderIfEnabled()
        }
    }

    private suspend fun scheduleReminderIfEnabled() {
        if (container.userPreferencesRepository.reminderEnabledFlow.first()) {
            val t = container.userPreferencesRepository.reminderTimeFlow.first()
            DailyReminderWorker.scheduleBoth(this, t.hour1, t.minute1, t.hour2, t.minute2)
        }
    }

    private suspend fun loadQuestionsIfNeeded() {
        try {
            val json = assets.open("questions.json").bufferedReader().use { it.readText() }
            val questions = JsonImporter.parseQuestions(json)
            if (questions.isEmpty()) return
            val dbCount = container.questionRepository.getTotalCount().first()
            if (dbCount < questions.size) {
                container.questionRepository.insertAll(questions)
            }
        } catch (_: Exception) {}
    }

    private suspend fun loadQuestionsFromAssets() {
        try {
            val json = assets.open("questions.json").bufferedReader().use { it.readText() }
            val questions = JsonImporter.parseQuestions(json)
            if (questions.isNotEmpty()) {
                container.questionRepository.insertAll(questions)
            }
        } catch (_: Exception) {}
    }
}
