package com.xiaogong.csestudy.data.repository

import com.xiaogong.csestudy.data.local.dao.*
import com.xiaogong.csestudy.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class UserProgressRepository(
    private val userAnswerDao: UserAnswerDao,
    private val wrongQuestionDao: WrongQuestionDao,
    private val favoriteQuestionDao: FavoriteQuestionDao,
    private val studyRecordDao: StudyRecordDao,
    private val knowledgeProgressDao: KnowledgeProgressDao
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ── 答题记录 ────────────────────────────────────────────────

    suspend fun recordAnswer(
        questionId: Long,
        userAnswer: String,
        isCorrect: Boolean,
        subject: String,
        chapter: String,
        knowledgePoint: String,
        quizMode: String = ""
    ) {
        userAnswerDao.insert(UserAnswerEntity(
            questionId = questionId,
            userAnswer = userAnswer,
            isCorrect = isCorrect,
            quizMode = quizMode
        ))

        if (!isCorrect) {
            wrongQuestionDao.upsertWrong(questionId)
        }

        updateTodayRecord(isCorrect)
        updateKnowledgeProgress(subject, chapter, knowledgePoint, isCorrect)
    }

    private suspend fun updateTodayRecord(isCorrect: Boolean) {
        val today = dateFormat.format(Date())
        val existing = studyRecordDao.getByDate(today)
        val updated = existing?.copy(
            totalAnswered = existing.totalAnswered + 1,
            correctCount = existing.correctCount + (if (isCorrect) 1 else 0)
        ) ?: StudyRecordEntity(
            dateKey = today,
            totalAnswered = 1,
            correctCount = if (isCorrect) 1 else 0
        )
        studyRecordDao.upsert(updated)
    }

    private suspend fun updateKnowledgeProgress(
        subject: String, chapter: String, knowledgePoint: String, isCorrect: Boolean
    ) {
        val id = "${subject}_${chapter}_${knowledgePoint}"
        val existing = knowledgeProgressDao.getById(id)
        val updated = existing?.copy(
            answerCount = existing.answerCount + 1,
            correctCount = existing.correctCount + (if (isCorrect) 1 else 0),
            lastPracticeAt = System.currentTimeMillis()
        ) ?: KnowledgeProgressEntity(
            knowledgeId = id,
            subject = subject,
            chapter = chapter,
            knowledgePoint = knowledgePoint,
            answerCount = 1,
            correctCount = if (isCorrect) 1 else 0,
            lastPracticeAt = System.currentTimeMillis()
        )
        knowledgeProgressDao.upsert(updated)
    }

    // ── 错题 ────────────────────────────────────────────────────

    fun getWrongQuestions(): Flow<List<WrongQuestionEntity>> = wrongQuestionDao.getActive()
    fun getWrongCount(): Flow<Int> = wrongQuestionDao.getActiveCount()
    suspend fun getWrongQuestionIds(): List<Long> = wrongQuestionDao.getActiveIds()
    suspend fun setMastered(questionId: Long) = wrongQuestionDao.setMastered(questionId, true)
    suspend fun deleteWrong(questionId: Long) = wrongQuestionDao.delete(questionId)

    // ── 收藏 ────────────────────────────────────────────────────

    fun getFavoriteCount(): Flow<Int> = favoriteQuestionDao.getCount()
    fun isFavorite(questionId: Long): Flow<Boolean> = favoriteQuestionDao.isFavorite(questionId)
    suspend fun getFavoriteIds(): List<Long> = favoriteQuestionDao.getAllIds()

    /** 收藏 id 集合，随数据库变化实时推送 */
    fun getFavoriteIdsFlow(): Flow<Set<Long>> =
        favoriteQuestionDao.getAll().map { list -> list.map { it.questionId }.toSet() }

    suspend fun toggleFavorite(questionId: Long) {
        if (favoriteQuestionDao.getAllIds().contains(questionId)) {
            favoriteQuestionDao.delete(questionId)
        } else {
            favoriteQuestionDao.insert(FavoriteQuestionEntity(questionId))
        }
    }

    // ── 统计 ────────────────────────────────────────────────────

    fun getTotalAnswered(): Flow<Int> = userAnswerDao.getTotalCount()
    fun getTotalCorrect(): Flow<Int> = userAnswerDao.getCorrectCount()

    /** 已刷过的题目数（同一题重复作答只算一次） */
    fun getAnsweredQuestionCount(): Flow<Int> = userAnswerDao.getAnsweredQuestionCount()

    /** 每题最近一次作答，key = questionId */
    suspend fun getLatestAnswerMap(): Map<Long, UserAnswerEntity> =
        userAnswerDao.getLatestAnswers().associateBy { it.questionId }

    fun getTodayAnswered(): Flow<Int> {
        val startOfDay = getTodayStartMillis()
        return userAnswerDao.getTodayCount(startOfDay)
    }

    fun getTodayCorrect(): Flow<Int> {
        val startOfDay = getTodayStartMillis()
        return userAnswerDao.getTodayCorrectCount(startOfDay)
    }

    fun getWeakKnowledgePoints(limit: Int = 5): Flow<List<KnowledgeProgressEntity>> =
        knowledgeProgressDao.getWeakPoints(limit)

    suspend fun getStreakDays(): Int {
        val dates = studyRecordDao.getAllStudyDates()
        if (dates.isEmpty()) return 0
        var streak = 0
        val cal = Calendar.getInstance()
        for (i in dates.indices) {
            val expected = dateFormat.format(cal.time)
            if (dates[i] == expected) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    fun getWeeklyStats(): Flow<List<StudyRecordEntity>> = studyRecordDao.getRecent(7)

    /** 累计打卡天数（有答题记录的天数） */
    fun getCheckInDays(): Flow<Int> = studyRecordDao.getCheckInDayCount()

    private fun getTodayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
