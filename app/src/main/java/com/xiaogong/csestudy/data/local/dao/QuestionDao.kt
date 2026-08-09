package com.xiaogong.csestudy.data.local.dao

import androidx.room.*
import com.xiaogong.csestudy.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: QuestionEntity)

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getById(id: Long): QuestionEntity?

    @Query("SELECT * FROM questions WHERE subject = :subject ORDER BY id")
    fun getBySubject(subject: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE subject = :subject AND chapter = :chapter ORDER BY id")
    fun getByChapter(subject: String, chapter: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE knowledgePoint = :kp ORDER BY id")
    fun getByKnowledgePoint(kp: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE level = :level ORDER BY id")
    fun getByLevel(level: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE subject = :subject ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomBySubject(subject: String, limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandom(limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE subject IN (:subjects) ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomInSubjects(subjects: List<String>, limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE subject IN (:subjects) ORDER BY id")
    fun getBySubjects(subjects: List<String>): Flow<List<QuestionEntity>>

    @Query("SELECT DISTINCT chapter FROM questions WHERE subject = :subject ORDER BY chapter")
    fun getChaptersBySubject(subject: String): Flow<List<String>>

    @Query("SELECT DISTINCT knowledgePoint FROM questions WHERE subject = :subject AND chapter = :chapter ORDER BY knowledgePoint")
    fun getKnowledgePoints(subject: String, chapter: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM questions WHERE subject = :subject")
    fun getCountBySubject(subject: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE subject IN (:subjects)")
    fun getCountInSubjects(subjects: List<String>): Flow<Int>

    @Query("SELECT * FROM questions ORDER BY id")
    fun getAll(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<QuestionEntity>
}
