package com.xiaogong.csestudy.data.repository

import com.xiaogong.csestudy.data.local.dao.QuestionDao
import com.xiaogong.csestudy.data.local.entity.QuestionEntity
import com.xiaogong.csestudy.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuestionRepository(private val dao: QuestionDao) {

    fun getBySubject(subject: Subject): Flow<List<Question>> =
        dao.getBySubject(subject.name).map { list -> list.map { it.toDomain() } }

    fun getByChapter(subject: Subject, chapter: String): Flow<List<Question>> =
        dao.getByChapter(subject.name, chapter).map { list -> list.map { it.toDomain() } }

    fun getChaptersBySubject(subject: Subject): Flow<List<String>> =
        dao.getChaptersBySubject(subject.name)

    fun getCountBySubject(subject: Subject): Flow<Int> =
        dao.getCountBySubject(subject.name)

    fun getTotalCount(): Flow<Int> = dao.getTotalCount()

    /** 该考试等级可练习科目的题目总数 */
    fun getCountForLevel(level: ExamLevel): Flow<Int> =
        dao.getCountInSubjects(subjectNames(level))

    fun getAll(): Flow<List<Question>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    /** 该考试等级可练习科目的全部题目，按 id 顺序 */
    fun getAllForLevel(level: ExamLevel): Flow<List<Question>> =
        dao.getBySubjects(subjectNames(level)).map { list -> list.map { it.toDomain() } }

    suspend fun getRandomBySubject(subject: Subject, limit: Int = 20): List<Question> =
        dao.getRandomBySubject(subject.name, limit).map { it.toDomain() }

    suspend fun getRandom(limit: Int = 20): List<Question> =
        dao.getRandom(limit).map { it.toDomain() }

    /** 在该考试等级可练习科目范围内随机抽题 */
    suspend fun getRandomForLevel(level: ExamLevel, limit: Int = 20): List<Question> =
        dao.getRandomInSubjects(subjectNames(level), limit).map { it.toDomain() }

    suspend fun getByIds(ids: List<Long>): List<Question> =
        dao.getByIds(ids).map { it.toDomain() }

    suspend fun getById(id: Long): Question? = dao.getById(id)?.toDomain()

    suspend fun insertAll(questions: List<Question>) =
        dao.insertAll(questions.map { it.toEntity() })

    suspend fun insert(question: Question) = dao.insert(question.toEntity())

    private fun subjectNames(level: ExamLevel) = Subject.forLevel(level).map { it.name }
}

private fun QuestionEntity.toDomain() = Question(
    id = id,
    level = ExamLevel.valueOf(level),
    subject = Subject.valueOf(subject),
    chapter = chapter,
    type = QuestionType.valueOf(type),
    question = question,
    options = options,
    answer = answer,
    analysis = analysis
)

private fun Question.toEntity() = QuestionEntity(
    id = id,
    level = level.name,
    subject = subject.name,
    chapter = chapter,
    type = type.name,
    question = question,
    options = options,
    answer = answer,
    analysis = analysis
)
