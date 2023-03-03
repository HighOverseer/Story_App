package com.lajar.mystoryapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.lajar.mystoryapp.ViewModel.ListViewModel
import com.lajar.mystoryapp.ViewModel.ViewModelFactory
import com.lajar.mystoryapp.databinding.ActivityListBinding

class ListActivity : AppCompatActivity() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_info")
    private lateinit var listViewModel: ListViewModel
    private lateinit var binding: ActivityListBinding
    private lateinit var navController:NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    companion object {
        const val ADD_ACTIVITY_RESULT = 100
        @StringRes
        private val TAB_TITLES = intArrayOf(
            R.string.stories,
            R.string.Map
        )
    }

    private val intentToCreateStory = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == ADD_ACTIVITY_RESULT) {
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listViewModel = obtainViewModel(this, dataStore)
        setInitialLayout()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_logout, menu)
        return super.onCreateOptionsMenu(menu)

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

    override fun onNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(binding.bnvList.visibility == View.GONE){
            binding.bnvList.visibility = View.VISIBLE
        }
    }

    private fun setInitialLayout() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bnvList.setupWithNavController(navController)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_list, R.id.nav_map, R.id.nav_about)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        /*setCurrentFragment(listFragment, false)

        binding.bnvList.setOnItemSelectedListener{menuItem ->
            val listFragmentInstance = supportFragmentManager.findFragmentByTag(ListFragment::class.java.simpleName)
            val mapFragmentInstance = supportFragmentManager.findFragmentByTag(MapFragment::class.java.simpleName)
            when(menuItem.itemId){
                R.id.menu_bnv_list -> {
                    if (listFragmentInstance!=null){
                        setCurrentFragment(listFragmentInstance, true)
                    }else setCurrentFragment(listFragment, false)
                }
                R.id.menu_bnv_map -> {
                    if (mapFragmentInstance != null){
                        setCurrentFragment(mapFragmentInstance, true)
                    }else setCurrentFragment(mapFragment, false)
                }
                R.id.menu_bnv_about -> Toast.makeText(this, "Not Yet Created!", Toast.LENGTH_SHORT).show()
            }
            true
        }*/
    }

/*    private fun setCurrentFragment(fragment:Fragment, getByTag:Boolean){
        supportFragmentManager.commit {
            when(fragment){
                is ListFragment -> {
                    if (getByTag){
                        replace(R.id.container, fragment)
                    }else replace(R.id.container, fragment, ListFragment::class.java.simpleName).addToBackStack(null)
                }
                is MapFragment -> {
                    if (getByTag){
                        replace(R.id.container, fragment)
                    }else replace(R.id.container, fragment, MapFragment::class.java.simpleName).addToBackStack(null)
                }
            }

        }
    }*/

    private fun toAddAct() {
        val intent = Intent(this, AddActivity::class.java)
        intentToCreateStory.launch(intent)
    }

    private fun obtainViewModel(
        activity: AppCompatActivity,
        dataStore: DataStore<Preferences>
    ): ListViewModel {
        val factory = ViewModelFactory.getInstance(activity.application, dataStore)
        return ViewModelProvider(activity, factory)[ListViewModel::class.java]
    }

    fun setBottomNavigationViewVisibility(isVisible:Boolean){
        binding.bnvList.isVisible = isVisible
    }

}