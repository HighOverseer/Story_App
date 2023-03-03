package com.lajar.mystoryapp.Helper

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.lajar.mystoryapp.data.local.entity.Story

class StoryPagingSource:PagingSource<Int, List<Story>>() {
    companion object{
        fun snapshot(stories:List<Story>):PagingData<Story>{
            return PagingData.from(stories)
        }

        val noopListUpdateCallback = object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {}

            override fun onRemoved(position: Int, count: Int) {}

            override fun onMoved(fromPosition: Int, toPosition: Int) {}

            override fun onChanged(position: Int, count: Int, payload: Any?) {}
        }
    }

    override fun getRefreshKey(state: PagingState<Int, List<Story>>): Int{
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, List<Story>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}

