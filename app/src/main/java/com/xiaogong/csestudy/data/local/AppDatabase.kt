package com.xiaogong.csestudy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.xiaogong.csestudy.data.local.converter.Converters
import com.xiaogong.csestudy.data.local.dao.*
import com.xiaogong.csestudy.data.local.entity.*

@Database(
    entities = [
        QuestionEntity::class,
        UserAnswerEntity::class,
        WrongQuestionEntity::class,
        FavoriteQuestionEntity::class,
        StudyRecordEntity::class,
        KnowledgeProgressEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun userAnswerDao(): UserAnswerDao
    abstract fun wrongQuestionDao(): WrongQuestionDao
    abstract fun favoriteQuestionDao(): FavoriteQuestionDao
    abstract fun studyRecordDao(): StudyRecordDao
    abstract fun knowledgeProgressDao(): KnowledgeProgressDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_answers ADD COLUMN quizMode TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE user_answers SET quizMode = 'SEQUENTIAL'")
            }
        }

        // 修正 v2 迁移中漏掉 UPDATE 的历史数据
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE user_answers SET quizMode = 'SEQUENTIAL' WHERE quizMode = ''")
            }
        }

        // 移除 questions 表的 difficulty / source / year / tags 四列。
        // 题库是纯种子数据，清空后由 CseApplication 从 assets 重新导入。
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS questions")
                db.execSQL(
                    """
                    CREATE TABLE questions (
                        id INTEGER NOT NULL PRIMARY KEY,
                        level TEXT NOT NULL,
                        subject TEXT NOT NULL,
                        chapter TEXT NOT NULL,
                        knowledgePoint TEXT NOT NULL,
                        type TEXT NOT NULL,
                        question TEXT NOT NULL,
                        options TEXT NOT NULL,
                        answer TEXT NOT NULL,
                        analysis TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cse_study.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build().also { INSTANCE = it }
            }
    }
}
