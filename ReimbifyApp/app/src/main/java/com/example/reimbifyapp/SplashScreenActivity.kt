package com.example.reimbifyapp

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.reimbifyapp.auth.ui.AuthActivity
import com.example.reimbifyapp.databinding.ActivitySplashScreenBinding

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        playWelcomeMusic()
        animateCoins()
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }, 6100) // 3 seconds delay
    }

    private fun playWelcomeMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.simple_clean_logo_13775)
        mediaPlayer.start()
        mediaPlayer.isLooping = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }

    private fun animateCoins() {
        val centerX = binding.ivLogo.x + binding.ivLogo.width / 2f
        val centerY = binding.ivLogo.y + binding.ivLogo.height / 2f
        val radius = binding.ivLogo.width * 0.7f
        val coins = listOf(
            binding.ivCoin1,
            binding.ivCoin2,
            binding.ivCoin3,
            binding.ivCoin4
        )

        coins.forEachIndexed { index, coin ->

            val angle = (index * 90).toFloat() // Each coin placed at 90-degree intervals
            val x = centerX + radius * Math.cos(Math.toRadians(angle.toDouble())).toFloat()
            val y = centerY + radius * Math.sin(Math.toRadians(angle.toDouble())).toFloat()
            coin.x = x
            coin.y = y
        }

        coins.forEach { coin ->
            val rotation = ObjectAnimator.ofFloat(coin, "rotation", 0f, 360f)
            rotation.duration = 3000
            rotation.repeatCount = ObjectAnimator.INFINITE
            rotation.interpolator = AccelerateDecelerateInterpolator()
            rotation.start()
        }
    }
}
