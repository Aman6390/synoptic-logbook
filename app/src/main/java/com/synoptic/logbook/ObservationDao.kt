package com.synoptic.logbook

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {

    /** Inserts or overwrites; returns the rowId of the saved record. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(observation: WeatherObservationEntity): Long

    /** Duplicate-guard lookup: exactly one record per date (ddMMyyyy) + UTC hour. */
    @Query("SELECT * FROM observations WHERE dateUtc = :dateUtc AND synopticHourUtc = :hour LIMIT 1")
    suspend fun findByDateAndHour(dateUtc: String, hour: String): WeatherObservationEntity?

    /** Deletes a single observation by its primary key. */
    @Query("DELETE FROM observations WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Chronological observation ordering (newest first). */
    @Query("""
        SELECT * FROM observations 
        ORDER BY (substr(dateUtc, 5, 4) || substr(dateUtc, 3, 2) || substr(dateUtc, 1, 2) || synopticHourUtc) DESC, id DESC
    """)
    fun observeAll(): Flow<List<WeatherObservationEntity>>
}
