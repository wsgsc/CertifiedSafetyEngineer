package com.xiaogong.csestudy.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xiaogong.csestudy.data.model.*

object JsonImporter {

    private val gson = Gson()

    /** 解析单题 JSON 字符串 */
    fun parseQuestion(json: String): Question? = try {
        val raw = gson.fromJson(json, RawQuestion::class.java)
        raw.toDomain()
    } catch (e: Exception) {
        null
    }

    /** 解析题目数组 JSON 字符串 */
    fun parseQuestions(json: String): List<Question> = try {
        val type = object : TypeToken<List<RawQuestion>>() {}.type
        val list: List<RawQuestion> = gson.fromJson(json, type)
        list.mapNotNull { it.toDomain() }
    } catch (e: Exception) {
        emptyList()
    }

    // 与 JSON 结构对应的中间类
    private data class RawQuestion(
        val id: Long = 0,
        val level: String = "",
        val subject: String = "",
        val chapter: String = "",
        val knowledgePoint: String = "",
        val type: String = "",
        val question: String = "",
        val options: List<String> = emptyList(),
        val answer: String = "",
        val analysis: String = ""
    ) {
        fun toDomain(): Question? = try {
            Question(
                id = id,
                level = ExamLevel.valueOf(levelMapping[level] ?: level),
                subject = Subject.valueOf(subjectMapping[subject] ?: subject),
                chapter = chapter,
                knowledgePoint = knowledgePoint,
                type = QuestionType.valueOf(typeMapping[type] ?: type),
                question = question,
                options = options,
                answer = answer,
                analysis = analysis
            )
        } catch (e: Exception) {
            null
        }
    }

    private val typeMapping = mapOf(
        "单选题" to QuestionType.SINGLE_CHOICE.name,
        "单项选择题" to QuestionType.SINGLE_CHOICE.name,
        "多选题" to QuestionType.MULTIPLE_CHOICE.name,
        "多项选择题" to QuestionType.MULTIPLE_CHOICE.name,
        "判断题" to QuestionType.TRUE_FALSE.name,
        "案例分析题" to QuestionType.CASE_ANALYSIS.name
    )

    // 支持中文名 → enum name 的映射，方便 JSON 直接用中文
    private val levelMapping = mapOf(
        "初级" to ExamLevel.JUNIOR.name,
        "中级" to ExamLevel.INTERMEDIATE.name
    )

    private val subjectMapping = mapOf(
        "安全生产法律法规" to Subject.LAW.name,
        "安全生产管理" to Subject.MANAGEMENT.name,
        "安全生产技术基础" to Subject.TECHNOLOGY.name,
        "化工安全" to Subject.CHEMICAL.name,
        "建筑施工安全" to Subject.CONSTRUCTION.name,
        "道路运输安全" to Subject.ROAD_TRANSPORT.name,
        "金属非金属矿山安全" to Subject.METAL_MINE.name,
        "金属冶炼安全" to Subject.METAL_SMELTING.name,
        "煤矿安全" to Subject.COAL_MINE.name,
        "其他安全" to Subject.OTHER.name
    )
}
