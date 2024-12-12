package com.example.reimbifyapp.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.reimbifyapp.R
import com.example.reimbifyapp.auth.factory.UserViewModelFactory
import com.example.reimbifyapp.auth.ui.AuthActivity
import com.example.reimbifyapp.auth.viewmodel.LoginViewModel
import com.example.reimbifyapp.data.preferences.SettingPreferences
import com.example.reimbifyapp.databinding.ActivityMainUserBinding
import com.example.reimbifyapp.user.factory.SettingViewModelFactory
import com.example.reimbifyapp.user.viewmodel.SettingViewModel
import com.example.reimbifyapp.utils.TokenUtils.isTokenValid
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "settings")

class MainActivityUser : AppCompatActivity() {

    private lateinit var binding: ActivityMainUserBinding
    private lateinit var navController: NavController
    private lateinit var userViewModel: LoginViewModel
    private lateinit var settingViewModel: SettingViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val sessionCheckInterval: Long = 5 * 60 * 1000
    private var isThemeChanging = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = SettingPreferences.getInstance(dataStore)
        val settingViewModelFactory = SettingViewModelFactory(pref)
        settingViewModel = ViewModelProvider(this, settingViewModelFactory)[SettingViewModel::class.java]

        settingViewModel.getThemeSettings().observe(this) { isDarkModeActive ->
            val nightMode = if (isDarkModeActive) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
                isThemeChanging = true
                AppCompatDelegate.setDefaultNightMode(nightMode)
            }
        }

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        super.onCreate(savedInstanceState)

        binding = ActivityMainUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main_user) as NavHostFragment
        navController = navHostFragment.navController

        handleInitialFragmentNavigation(savedInstanceState)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard,
                R.id.navigation_history,
                R.id.navigation_add_request,
                R.id.navigation_profile,
                R.id.navigation_setting
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = destination.label

            val topLevelDestinations = appBarConfiguration.topLevelDestinations
            val showBackButton = destination.id !in topLevelDestinations
            supportActionBar?.setDisplayHomeAsUpEnabled(showBackButton)
        }

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory.getInstance(applicationContext)
        )[LoginViewModel::class.java]

        startSessionValidation()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()

        if (isThemeChanging) {
            isThemeChanging = false
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun handleInitialFragmentNavigation(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val openFragment = intent.getStringExtra("open_fragment")
            Log.d("OPEN FRAGMENT", "Initial fragment: $openFragment")

            when (openFragment) {
                "dashboard" -> navigateToDashboard()
                "setting" -> navigateToSetting()
            }

            intent.removeExtra("open_fragment")
        }
    }

    private fun navigateToDashboard() {
        try {
            if (navController.currentDestination?.id != R.id.navigation_dashboard) {
                navController.popBackStack()
                navController.navigate(R.id.navigation_dashboard)
            }
        } catch (e: Exception) {
            Log.e("Navigation", "Error navigating to dashboard", e)
        }
    }

    private fun navigateToSetting() {
        try {
            if (navController.currentDestination?.id != R.id.navigation_setting) {
                navController.popBackStack()
                navController.navigate(R.id.navigation_setting)
            }
        } catch (e: Exception) {
            Log.e("Navigation", "Error navigating to settings", e)
        }
    }

    private fun startSessionValidation() {
        lifecycleScope.launch {
            while (true) {
                validateSession()
                delay(sessionCheckInterval)
            }
        }
    }

    private fun validateSession() {
        lifecycleScope.launch {
            val userSession = userViewModel.getSession().first()
            if (!isTokenValid(userSession.token)) {
                onSessionInvalid()
            }
        }
    }

    private fun onSessionInvalid() {
        Toast.makeText(this, "Session expired. Redirecting to login.", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}