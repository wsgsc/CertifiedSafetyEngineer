package com.xiaogong.csestudy.data.model

enum class QuestionType(val displayName: String) {
    SINGLE_CHOICE("单项选择题"),
    MULTIPLE_CHOICE("多项选择题"),
    TRUE_FALSE("判断题"),
    CASE_ANALYSIS("案例分析题")
}
