package com.xiaogong.csestudy.data.local.dao

import androidx.room.*
import com.xiaogong.csestudy.data.local.entity.StudyRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: StudyRecordEntity)

    @Query("SELECT * FROM study_records WHERE dateKey = :dateKey")
    suspend fun getByDate(dateKey: String): StudyRecordEntity?

    @Query("SELECT * FROM study_records ORDER BY dateKey DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<StudyRecordEntity>>

    // 连续学习天数：按日期降序找连续的记录
    @Query("SELECT dateKey FROM study_records WHERE totalAnswered > 0 ORDER BY dateKey DESC")
    suspend fun getAllStudyDates(): List<String>

    // 累计打卡天数
    @Query("SELECT COUNT(*) FROM study_records WHERE totalAnswered > 0")
    fun getCheckInDayCount(): Flow<Int>
}
