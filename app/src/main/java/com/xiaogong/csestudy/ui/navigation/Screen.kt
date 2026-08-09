package com.xiaogong.csestudy.ui.navigation

sealed class Screen(val route: String) {
    // 启动路由
    object UserProfileSetup : Screen("user_profile_setup")
    object LevelSelection : Screen("level_selection")

    // 底部导航
    object Home : Screen("home")
    object Quiz : Screen("quiz")
    object Profile : Screen("profile")

    // 刷题-章节练习：选择科目
    object ChapterSubjectSelect : Screen("chapter_subject_select")

    // 章节练习-选择章节
    object SubjectDetail : Screen("subject_detail/{subjectName}") {
        fun createRoute(subjectName: String) = "subject_detail/$subjectName"
    }

    // 捐赠
    object Donate : Screen("donate")

    // 学习资料
    object MaterialList : Screen("material_list")
    object PdfReader : Screen("pdf_reader/{assetName}") {
        fun createRoute(assetName: String) = "pdf_reader/${encode(assetName)}"
    }

    // 刷题子页面
    object QuizPlay : Screen("quiz_play/{mode}/{param}") {
        fun createRoute(mode: QuizMode, param: String = "") = "quiz_play/${mode.name}/${encode(param)}"
    }
    object QuizResult : Screen("quiz_result/{correct}/{total}") {
        fun createRoute(correct: Int, total: Int) = "quiz_result/$correct/$total"
    }
}

enum class QuizMode {
    SEQUENTIAL,    // 顺序练习
    RANDOM,        // 随机练习
    CHAPTER,       // 章节练习 (param = "subject|chapter")
    KNOWLEDGE,     // 知识点练习 (param = knowledgePoint)
    WRONG,         // 错题练习
    FAVORITE,      // 收藏练习
    REDO           // 单题重做 (param = questionId)
}

private fun encode(s: String) = java.net.URLEncoder.encode(s, "UTF-8")
