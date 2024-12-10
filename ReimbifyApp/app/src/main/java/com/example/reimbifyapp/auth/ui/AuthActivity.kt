package com.example.reimbifyapp.auth.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.ActivityAuthBinding
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.auth.factory.UserViewModelFactory
import com.example.reimbifyapp.data.preferences.SettingPreferences
import com.example.reimbifyapp.user.factory.SettingViewModelFactory
import com.example.reimbifyapp.user.viewmodel.SettingViewModel

class AuthActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel> {
        UserViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityAuthBinding
    private val Context.dataStore by preferencesDataStore(name = "settings")

    private val sessionCheckInterval: Long = 5 * 60 * 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref = SettingPreferences.getInstance(dataStore)
        val settingViewModelFactory = SettingViewModelFactory(pref)
        val settingViewModel = ViewModelProvider(this, settingViewModelFactory)[SettingViewModel::class.java]

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingViewModel.getThemeSettings().observe(this) { isDarkModeActive ->
            val nightMode = if (isDarkModeActive) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
                AppCompatDelegate.setDefaultNightMode(nightMode)
            }
        }

        supportActionBar?.hide()

        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.let {
            it as NavHostFragment
        }?.navController

        if (navController != null) {
            setupActionBarWithNavController(navController)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.let {
            it as NavHostFragment
        }?.navController

        return navController?.navigateUp() ?: super.onSupportNavigateUp()
    }
}