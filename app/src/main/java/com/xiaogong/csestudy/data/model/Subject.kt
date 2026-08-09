package com.xiaogong.csestudy.data.model

enum class Subject(val displayName: String, val levels: List<ExamLevel>) {
    LAW(
        "安全生产法律法规",
        listOf(ExamLevel.JUNIOR, ExamLevel.INTERMEDIATE)
    ),
    MANAGEMENT(
        "安全生产管理",
        listOf(ExamLevel.JUNIOR, ExamLevel.INTERMEDIATE)
    ),
    TECHNOLOGY(
        "安全生产技术基础",
        listOf(ExamLevel.JUNIOR, ExamLevel.INTERMEDIATE)
    ),
    CHEMICAL(
        "化工安全",
        listOf(ExamLevel.INTERMEDIATE)
    ),
    CONSTRUCTION(
        "建筑施工安全",
        listOf(ExamLevel.INTERMEDIATE)
    ),
    ROAD_TRANSPORT(
        "道路运输安全",
        listOf(ExamLevel.INTERMEDIATE)
    ),
    METAL_MINE(
        "金属非金属矿山安全",
        listOf(ExamLevel.INTERMEDIATE)
    ),
    METAL_SMELTING(
        "金属冶炼安全",
        listOf(ExamLevel.INTERMEDIATE)
    ),
    COAL_MINE(
        "煤矿安全",
        listOf(ExamLevel.INTERMEDIATE)
    ),
    OTHER(
        "其他安全",
        listOf(ExamLevel.INTERMEDIATE)
    );

    companion object {
        fun forLevel(level: ExamLevel) = entries.filter { level in it.levels }
    }
}
