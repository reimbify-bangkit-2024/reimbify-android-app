package com.example.reimbifyapp.admin

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import com.example.reimbifyapp.databinding.ActivityMainAdminBinding
import com.example.reimbifyapp.user.factory.SettingViewModelFactory
import com.example.reimbifyapp.user.viewmodel.SettingViewModel
import com.example.reimbifyapp.utils.TokenUtils.isTokenValid
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivityAdmin : AppCompatActivity() {

    private lateinit var binding: ActivityMainAdminBinding
    private lateinit var navController: NavController
    private lateinit var userViewModel: LoginViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val Context.dataStore by preferencesDataStore(name = "settings")

    private val sessionCheckInterval: Long = 5 * 60 * 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref = SettingPreferences.getInstance(dataStore)
        val settingViewModelFactory = SettingViewModelFactory(pref)
        val settingViewModel = ViewModelProvider(this, settingViewModelFactory)[SettingViewModel::class.java]

        binding = ActivityMainAdminBinding.inflate(layoutInflater)
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

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main_admin)
        if (navHostFragment == null || navHostFragment !is NavHostFragment) {
            Log.d("ERROR","NavHostFragment not found or incorrectly set up in the layout.")
            throw IllegalStateException("NavHostFragment not found or incorrectly set up in the layout.")
        }
        navController = navHostFragment.navController

//        val openFragment = intent.getStringExtra("open_fragment")
//        if (openFragment == "dashboard") {
//            navController.navigate(R.id.navigation_dashboard)
//        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard,
                R.id.navigation_to_review,
                R.id.navigation_reviewed,
                R.id.navigation_list_user,
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

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.toolbar_menu, menu)
//        val menuItem = menu.findItem(R.id.action_notifications)
//        val iconDrawable = menuItem.icon
//        if (iconDrawable != null) {
//            val color = getColorFromAttr(android.R.attr.colorSecondary)
//            iconDrawable.setTint(color)
//        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
//            R.id.action_notifications -> {
//                Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
//                true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun getColorFromAttr(attr: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
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