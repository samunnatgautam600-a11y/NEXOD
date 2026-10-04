package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM saved_topics ORDER BY timestamp DESC")
    fun getAllSavedTopics(): Flow<List<SavedTopicEntity>>

    @Query("SELECT * FROM saved_topics WHERE id = :id")
    suspend fun getTopicById(id: Long): SavedTopicEntity?

    @Query("SELECT * FROM saved_topics WHERE LOWER(query) = LOWER(:query) LIMIT 1")
    suspend fun getTopicByQuery(query: String): SavedTopicEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM saved_topics WHERE LOWER(query) = LOWER(:query))")
    fun isTopicSaved(query: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: SavedTopicEntity): Long

    @Query("DELETE FROM saved_topics WHERE id = :id")
    suspend fun deleteTopicById(id: Long)

    @Query("DELETE FROM saved_topics WHERE LOWER(query) = LOWER(:query)")
    suspend fun deleteTopicByQuery(query: String)

    @Query("SELECT COUNT(*) FROM saved_topics")
    fun getSavedCount(): Flow<Int>
}
