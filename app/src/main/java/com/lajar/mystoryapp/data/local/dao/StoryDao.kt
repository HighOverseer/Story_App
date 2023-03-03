package com.lajar.mystoryapp.data.local.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lajar.mystoryapp.data.local.entity.Story

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStory(stories: List<Story>)

    @Query("SELECT * FROM story")
    fun getAllStoriesWithPaging(): PagingSource<Int, Story>

    @Query("SELECT * FROM story")
    fun getAllStories(): LiveData<List<Story>>

    @Query("DELETE FROM story")
    suspend fun deleteAll()
}