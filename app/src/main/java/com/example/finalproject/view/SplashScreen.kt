package com.example.finalproject.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.R
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_splash)
        val user = FirebaseAuth.getInstance().currentUser
        Handler().postDelayed({
            val i: Intent = if (user != null) Intent(
                this@SplashScreen,
                UserListActivity::class.java
            ) else Intent(
                this@SplashScreen,
                welcome2Activity::class.java
            )
            startActivity(i)
            finish()
        }, 3000)
    }
}