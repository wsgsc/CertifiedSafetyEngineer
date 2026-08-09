package com.xiaogong.csestudy.data.local.dao

import androidx.room.*
import com.xiaogong.csestudy.data.local.entity.KnowledgeProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgeProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: KnowledgeProgressEntity)

    @Query("SELECT * FROM knowledge_progress WHERE knowledgeId = :id")
    suspend fun getById(id: String): KnowledgeProgressEntity?

    @Query("SELECT * FROM knowledge_progress WHERE subject = :subject ORDER BY CAST(correctCount AS REAL) / MAX(answerCount, 1) ASC")
    fun getBySubject(subject: String): Flow<List<KnowledgeProgressEntity>>

    // 薄弱知识点：答题数>=3 且正确率最低
    @Query("SELECT * FROM knowledge_progress WHERE answerCount >= 3 ORDER BY CAST(correctCount AS REAL) / answerCount ASC LIMIT :limit")
    fun getWeakPoints(limit: Int = 5): Flow<List<KnowledgeProgressEntity>>

    @Query("SELECT * FROM knowledge_progress WHERE answerCount >= 1 ORDER BY CAST(correctCount AS REAL) / answerCount ASC LIMIT :limit")
    suspend fun getWeakPointsSync(limit: Int = 5): List<KnowledgeProgressEntity>
}
