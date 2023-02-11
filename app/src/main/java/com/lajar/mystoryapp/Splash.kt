package com.lajar.mystoryapp


import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.lajar.mystoryapp.ViewModel.SplashViewModel
import com.lajar.mystoryapp.ViewModel.ViewModelFactory
import com.lajar.mystoryapp.databinding.ActivitySplashBinding

class Splash : AppCompatActivity() {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_info")
    private lateinit var splashViewModel: SplashViewModel
    private lateinit var binding: ActivitySplashBinding

    companion object {
        private const val SHARED_ELEMENT = "Logo"
        private const val delayMillis = 2500L
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        splashViewModel = obtainViewModel(this, dataStore)
        setUpView()

        toListOrLoginAct()


    }

    private fun setUpView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun obtainViewModel(
        activity: AppCompatActivity,
        dataStore: DataStore<Preferences>
    ): SplashViewModel {
        val factory = ViewModelFactory.getInstance(dataStore)
        return ViewModelProvider(activity, factory)[SplashViewModel::class.java]
    }


    private fun toListOrLoginAct() {
        val optionsCompat = getOptionsCompat()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (splashViewModel.getToken().isNotEmpty() && splashViewModel.getToken()
                    .isNotBlank()
            ) {
                val intent = Intent(this@Splash, ListActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this@Splash, LoginActivity::class.java)
                startActivity(intent, optionsCompat.toBundle())
            }

        }, delayMillis)

    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    private fun getOptionsCompat(): ActivityOptionsCompat {
        return ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            Pair(binding.ivLogo, SHARED_ELEMENT)
        )
    }


}