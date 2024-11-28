package com.example.reimbifyapp.user.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.reimbifyapp.R
import com.example.reimbifyapp.databinding.ActivityAuthBinding
import com.example.reimbifyapp.user.viewmodel.LoginViewModel
import com.example.reimbifyapp.user.factory.UserViewModelFactory

class AuthActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel> {
        UserViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

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