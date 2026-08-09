package com.xiaogong.csestudy.data.model

data class Question(
    val id: Long,
    val level: ExamLevel,
    val subject: Subject,
    val chapter: String,
    val knowledgePoint: String,
    val type: QuestionType,
    val question: String,
    val options: List<String>,   // 判断题用 ["正确", "错误"]，案例分析题为空
    val answer: String,          // 单选: "A"，多选: "AC"，判断: "正确"/"错误"
    val analysis: String,
    val difficulty: Int = 2,     // 1-5
    val source: String = "",
    val year: Int = 0,
    val tags: List<String> = emptyList()
)
