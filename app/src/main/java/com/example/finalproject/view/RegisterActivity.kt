package com.example.finalproject.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {
    var mAuth: FirebaseAuth? = null
    var name_input: EditText? = null
    var username_input: EditText? = null
    var email_input: EditText? = null
    var password_input: EditText? = null
    var password2_input: EditText? = null
    var usernames: MutableList<*>? = null
    var register_btn: ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_register)
        email_input = findViewById(R.id.input_email)
        name_input = findViewById(R.id.input_name)
        password2_input = findViewById(R.id.input_password2)
        password_input = findViewById(R.id.input_password)
        username_input = findViewById(R.id.username_input)
        usernames = ArrayList<Any?>()
        register_btn = findViewById(R.id.RegisterButton)
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Users")
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (user in snapshot.children) {
                    (usernames as ArrayList<Any?>).add(user.key)
                }
                Toast.makeText(
                    this@RegisterActivity,
                    "Please fill all the columns!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        register_btn?.setOnClickListener(View.OnClickListener { v: View? -> if (verifyCredentials()) createUser() })
    }

    private fun verifyCredentials(): Boolean {
        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"
        val pat = Pattern.compile(emailRegex)
        if (!pat.matcher(email_input?.text.toString()).matches()) {
            email_input?.error = "Invalid Email"
            return false
        }
        //email,name,username, password,password2
        if (usernames?.contains(username_input?.text.toString()) == true) {
            username_input?.error = "Username already taken!"
            return false
        }
        if (name_input?.text.toString().isEmpty() || name_input?.text.toString().length > 32 || username_input?.text.toString()
                .isEmpty() || password_input?.text.toString()
                .isEmpty()
        ) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password_input?.text.toString() != password2_input?.text.toString()) {
            password2_input?.error = "Passwords do not match!"
            return false
        }
        return true
    }

    private fun createUser() {
        mAuth?.createUserWithEmailAndPassword(
            email_input?.text.toString(),
            password_input?.text.toString()
        )?.addOnCompleteListener(
            this
        ) {
            val intent = Intent(this@RegisterActivity, Register2::class.java)
            intent.putExtra("name", name_input?.text.toString())
            intent.putExtra("username", username_input?.text.toString())
            intent.putExtra("email_id", email_input?.text.toString())
            startActivity(intent)
            finish()
        }
    }
}