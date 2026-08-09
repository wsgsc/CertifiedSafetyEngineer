package com.xiaogong.csestudy.data.local.dao

import androidx.room.*
import com.xiaogong.csestudy.data.local.entity.UserAnswerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAnswerDao {
    @Insert
    suspend fun insert(answer: UserAnswerEntity)

    // 以下统计均关联 questions 表，只统计指定科目范围内的作答（初级仅法规+管理）
    @Query("""
        SELECT COUNT(*) FROM user_answers a
        JOIN questions q ON q.id = a.questionId
        WHERE q.subject IN (:subjects)
    """)
    fun getTotalCountInSubjects(subjects: List<String>): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM user_answers a
        JOIN questions q ON q.id = a.questionId
        WHERE q.subject IN (:subjects) AND a.isCorrect = 1
    """)
    fun getCorrectCountInSubjects(subjects: List<String>): Flow<Int>

    @Query("""
        SELECT COUNT(DISTINCT a.questionId) FROM user_answers a
        JOIN questions q ON q.id = a.questionId
        WHERE q.subject IN (:subjects)
    """)
    fun getAnsweredQuestionCountInSubjects(subjects: List<String>): Flow<Int>

    // 每题最近一次作答（用于恢复历史答题记录）
    @Query("""
        SELECT * FROM user_answers
        WHERE id IN (SELECT MAX(id) FROM user_answers GROUP BY questionId)
    """)
    suspend fun getLatestAnswers(): List<UserAnswerEntity>

    @Query("""
        SELECT COUNT(*) FROM user_answers a
        JOIN questions q ON q.id = a.questionId
        WHERE q.subject IN (:subjects) AND a.answeredAt >= :startOfDay
    """)
    fun getTodayCountInSubjects(subjects: List<String>, startOfDay: Long): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM user_answers a
        JOIN questions q ON q.id = a.questionId
        WHERE q.subject IN (:subjects) AND a.answeredAt >= :startOfDay AND a.isCorrect = 1
    """)
    fun getTodayCorrectCountInSubjects(subjects: List<String>, startOfDay: Long): Flow<Int>

    @Query("SELECT * FROM user_answers WHERE questionId = :questionId ORDER BY answeredAt DESC LIMIT 1")
    suspend fun getLatestForQuestion(questionId: Long): UserAnswerEntity?

    @Query("SELECT COUNT(*) FROM user_answers WHERE quizMode = :mode")
    fun getCountByMode(mode: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_answers WHERE quizMode = :mode AND isCorrect = 1")
    fun getCorrectCountByMode(mode: String): Flow<Int>
}
