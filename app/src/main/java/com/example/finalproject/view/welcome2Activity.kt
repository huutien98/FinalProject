package com.example.finalproject.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.R

class welcome2Activity : AppCompatActivity() {
    var login_btn: ImageButton? = null
    var register_btn: ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome2)
        login_btn = findViewById(R.id.login_btn)
        register_btn = findViewById(R.id.register_btn)
        login_btn?.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(this@welcome2Activity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        })
        register_btn?.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(this@welcome2Activity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        })
    }
}