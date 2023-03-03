package com.lajar.mystoryapp.ViewModel


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import com.lajar.mystoryapp.Adapter.ListStoriesAdapter
import com.lajar.mystoryapp.Helper.*
import com.lajar.mystoryapp.Helper.LiveDataTestUtil.getOrAwaitValue
import com.lajar.mystoryapp.data.StoryRepository
import com.lajar.mystoryapp.data.UserRepository
import com.lajar.mystoryapp.data.local.entity.Story
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ListViewModelTest{
    @get:Rule
    val instanceExecutorRule = InstantTaskExecutorRule()

    @get:Rule
      val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository
    @Mock
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var networkConnectivityObserver: NetworkConnectivityObserver

    @Test
    fun `when get Story Should Not Null and Return Data`() = runTest{
        val dummyStories = DataDummy.generatDummyStories()
        val data:PagingData<Story> = StoryPagingSource.snapshot(dummyStories)
        val expectedStories = MutableLiveData<PagingData<Story>>()
        expectedStories.value = data

        `when`(storyRepository.getStoriesWithPagination()).thenReturn(expectedStories)
        val listViewModel = ListViewModel(storyRepository, userRepository, networkConnectivityObserver)
        val actualStories = listViewModel.listStoriesWithPagination.getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoriesAdapter.DIFF_CALLBACK,
            updateCallback = StoryPagingSource.noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStories)

        assertNotNull(differ.snapshot())
        assertEquals(dummyStories.size, differ.snapshot().size)
        assertEquals(dummyStories[0].id, differ.snapshot()[0]?.id)
    }

    @Test
    fun `when Get Stories Empty Should Return No Data`()= runTest{
        val data:PagingData<Story> = PagingData.from(emptyList())
        val expectedStories = MutableLiveData<PagingData<Story>>()
        expectedStories.value = data
        `when`(storyRepository.getStoriesWithPagination()).thenReturn(expectedStories)

        val listViewModel = ListViewModel(storyRepository, userRepository, networkConnectivityObserver)
        val actualStories = listViewModel.listStoriesWithPagination.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoriesAdapter.DIFF_CALLBACK,
            updateCallback = StoryPagingSource.noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStories)

        assertEquals(0, differ.snapshot().size)

    }
}



