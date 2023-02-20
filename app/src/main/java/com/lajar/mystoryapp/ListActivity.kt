package com.lajar.mystoryapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.lajar.mystoryapp.Adapter.SectionsPagerAdapter
import com.lajar.mystoryapp.ViewModel.ListViewModel
import com.lajar.mystoryapp.ViewModel.ViewModelFactory
import com.lajar.mystoryapp.databinding.ActivityListBinding

class ListActivity : AppCompatActivity() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_info")
    private lateinit var listViewModel: ListViewModel
    private lateinit var binding: ActivityListBinding
/*    private val adapter: ListStoriesAdapter =
        ListStoriesAdapter(listOf(), object : ListStoriesAdapter.OnItemGetClicked {
            override fun onClick(story: Story, sharedElementTransition: ActivityOptionsCompat) {
                toDetailAct(story, sharedElementTransition)
            }
        })*/

    companion object {
        const val ADD_ACTIVITY_RESULT = 100

        @StringRes
        private val TAB_TITLES = intArrayOf(
            R.string.List,
            R.string.Map
        )
    }

    private val intentToCreateStory = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == ADD_ACTIVITY_RESULT) {
            listViewModel.getStories()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listViewModel = obtainViewModel(this, dataStore)
        setInitialLayout()
        /*listViewModel.listStories.observe(this) { result ->
            checkResult(result)
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_logout, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onResume() {
        super.onResume()

        /*adapter.isAnItemHasBeenClicked = false*/
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                listViewModel.deleteToken()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.change_language -> {
                val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(intent)
            }
            R.id.add -> toAddAct()

        }

        return super.onOptionsItemSelected(item)

    }

    private fun setInitialLayout(){
        binding.apply {
            val sectionsPagerAdapter = SectionsPagerAdapter(this@ListActivity)
            viewPager.adapter = sectionsPagerAdapter
            TabLayoutMediator(tablList, viewPager){tab, position ->
                tab.text = getString(TAB_TITLES[position])
            }.attach()
        }
    }

   /* private fun checkResult(result: Result<List<Story>>) {
        binding.apply {
            when (result) {
                is Result.Loading -> {
                    tvListNoData.visibility = View.GONE
                    pbList.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    pbList.visibility = View.GONE
                    tvListNoData.visibility = View.GONE
                    val listStories = result.data
                    if (listStories.isEmpty()) {
                        tvListNoData.visibility = View.VISIBLE
                    } else {
                        setLayout(listStories)
                    }

                }
                is Result.Error -> {
                    pbList.visibility = View.GONE
                    val messageEvent = result.error
                    showToast(messageEvent)
                    tvListNoData.visibility = View.VISIBLE
                }
            }
        }
    }*/

    /*private fun setLayout(stories: List<Story>) {
        binding.apply {
            adapter.updateList(stories)
            rvListStory.adapter = adapter
            rvListStory.layoutManager = LinearLayoutManager(this@ListActivity)
            rvListStory.setHasFixedSize(true)
        }
    }*/

    /*private fun toDetailAct(story: Story, sharedElementTransition: ActivityOptionsCompat) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_STORY, story)
        startActivity(intent, sharedElementTransition.toBundle())
    }*/

    private fun toAddAct() {
        val intent = Intent(this, AddActivity::class.java)
        intentToCreateStory.launch(intent)
    }

    private fun obtainViewModel(
        activity: AppCompatActivity,
        dataStore: DataStore<Preferences>
    ): ListViewModel {
        val factory = ViewModelFactory.getInstance(dataStore)
        return ViewModelProvider(activity, factory)[ListViewModel::class.java]
    }


    fun setUserInputEnabledViewPager(isUserInputEnabled:Boolean){
        binding.viewPager.isUserInputEnabled = isUserInputEnabled
    }

    /*private fun showToast(messageEvent: Event<String>) {
        val message = messageEvent.getContentIfNotHandled()
        if (message != null) {
            Toast.makeText(this@ListActivity, message, Toast.LENGTH_SHORT).show()
        }
    }*/
}