package com.xiaogong.csestudy.data.local.dao

import androidx.room.*
import com.xiaogong.csestudy.data.local.entity.WrongQuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WrongQuestionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: WrongQuestionEntity)

    // 答错时递增错误次数并更新时间
    @Query("""
        INSERT OR REPLACE INTO wrong_questions (questionId, wrongCount, lastWrongAt, isMastered)
        VALUES (:questionId,
            COALESCE((SELECT wrongCount FROM wrong_questions WHERE questionId = :questionId), 0) + 1,
            :timestamp, 0)
    """)
    suspend fun upsertWrong(questionId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE wrong_questions SET isMastered = :mastered WHERE questionId = :questionId")
    suspend fun setMastered(questionId: Long, mastered: Boolean)

    @Query("DELETE FROM wrong_questions WHERE questionId = :questionId")
    suspend fun delete(questionId: Long)

    @Query("SELECT * FROM wrong_questions WHERE isMastered = 0 ORDER BY lastWrongAt DESC")
    fun getActive(): Flow<List<WrongQuestionEntity>>

    // 错题数/错题 id 均限定在指定科目范围内（初级仅法规+管理）
    @Query("""
        SELECT COUNT(*) FROM wrong_questions w
        JOIN questions q ON q.id = w.questionId
        WHERE q.subject IN (:subjects) AND w.isMastered = 0
    """)
    fun getActiveCountInSubjects(subjects: List<String>): Flow<Int>

    @Query("""
        SELECT w.questionId FROM wrong_questions w
        JOIN questions q ON q.id = w.questionId
        WHERE q.subject IN (:subjects) AND w.isMastered = 0
        ORDER BY w.lastWrongAt DESC
    """)
    suspend fun getActiveIdsInSubjects(subjects: List<String>): List<Long>

    @Query("SELECT EXISTS(SELECT 1 FROM wrong_questions WHERE questionId = :questionId AND isMastered = 0)")
    fun isWrong(questionId: Long): Flow<Boolean>
}
