package com.lajar.mystoryapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.lajar.mystoryapp.data.local.database.StoryDatabase
import com.lajar.mystoryapp.data.local.entity.RemoteKeys
import com.lajar.mystoryapp.data.local.entity.Story
import com.lajar.mystoryapp.data.remote.ApiService
import retrofit2.awaitResponse

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val database: StoryDatabase,
    private val apiService: ApiService
) : RemoteMediator<Int, Story>() {
    companion object {
        private const val INITIALIZE_PAGE_INDEX = 1
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Story>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeysClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIALIZE_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeysForFirstTime(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeysForLastTime(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }
        try {
            val responseDataResponse =
                apiService.getAllStories(page, state.config.pageSize).awaitResponse()
            val responseData: List<Story>
            val endOfPaginationReached: Boolean
            if (
                responseDataResponse.isSuccessful
                && responseDataResponse.body() != null
                && responseDataResponse.body()?.error == false
            ) {
                responseData = responseDataResponse.body()?.listStory ?: emptyList()
                endOfPaginationReached = responseData.isEmpty()
                database.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        database.remoteKeysDao().deleteAll()
                        database.storyDao().deleteAll()
                    }
                    val prevKey = if (page == INITIALIZE_PAGE_INDEX) null else page - 1
                    val nextKey = if (endOfPaginationReached) null else page + 1
                    val keys = if (responseData.isEmpty()) listOf(
                        RemoteKeys(
                            "runOutOfData",
                            prevKey,
                            nextKey
                        )
                    ) else responseData.map {
                        RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                    }
                    database.remoteKeysDao().insertAll(keys)
                    database.storyDao().insertStory(responseData)
                }
                return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            } else return MediatorResult.Error(Exception("ResponseData is Unsuccessful"))
        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }

    }

    private suspend fun getRemoteKeysForLastTime(state: PagingState<Int, Story>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { value ->
            database.remoteKeysDao().getRemoteKeyById(value.id)
        }

    }

    private suspend fun getRemoteKeysForFirstTime(state: PagingState<Int, Story>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { value ->
            database.remoteKeysDao().getRemoteKeyById(value.id)
        }
    }

    private suspend fun getRemoteKeysClosestToCurrentPosition(state: PagingState<Int, Story>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeysDao().getRemoteKeyById(id)
            }
        }
    }
}



