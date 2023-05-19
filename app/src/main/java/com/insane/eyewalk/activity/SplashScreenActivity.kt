package com.insane.eyewalk.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.insane.eyewalk.R
import com.insane.eyewalk.config.Player
import java.util.Timer
import java.util.TimerTask

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Player(this, R.raw.logo).play()
        Timer().schedule(object : TimerTask() {
            override fun run() {
                startApp();
            }
        }, 2000)

    }

    private fun startApp() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        this.finish();
    }

}