package com.example.finalproject.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.R
import com.example.finalproject.model.User
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var user_email_input: EditText? = null
    private var user_password_input: EditText? = null
    private var login_btn: ImageButton? = null
    private var primaryScreen: LinearLayout? = null
    private var secondaryOverlay: LinearLayout? = null
    private var forgot_password: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        primaryScreen = findViewById(R.id.primaryView)
        secondaryOverlay = findViewById(R.id.waitingOverlay)
        val keycheck = getSharedPreferences("Personal_keys", MODE_PRIVATE)
        if (keycheck.getString("privateKey", "").equals("", ignoreCase = true)) {

            Toast.makeText(this, "Can't Proceed with the login.\nPlease create a new account.", Toast.LENGTH_LONG).show()
            Handler().postDelayed({
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }, 3000)
        }
        val sharedpreferences = getSharedPreferences("personal_details", MODE_PRIVATE)
        val prev_email = sharedpreferences.getString("email_id", "")
        mAuth = FirebaseAuth.getInstance()
        forgot_password = findViewById(R.id.forgot_password)
        forgot_password?.setOnClickListener(View.OnClickListener { v: View? ->
            FirebaseAuth.getInstance().sendPasswordResetEmail(
                user_email_input?.text.toString()
            ).addOnSuccessListener(object : OnSuccessListener<Void?> {
                override fun onSuccess(p0: Void) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Password reset email sent.",
                        Toast.LENGTH_SHORT
                    )
                }
            })
        })
        user_email_input = findViewById(R.id.input_email)
        user_password_input = findViewById(R.id.login_password)
        login_btn = findViewById(R.id.Continue_btn)
        login_btn?.setOnClickListener {
            //
            val email_id = user_email_input?.text.toString()
            val password = user_password_input?.text.toString()

            if (prev_email != email_id) {
                user_email_input?.error = "Please Log in with previously used email."
                Toast.makeText(
                    this@LoginActivity,
                    "Please Log in with previously used email.",
                    Toast.LENGTH_SHORT
                )
            } else if (verifyCredentials(email_id, password)) {
                    primaryScreen?.alpha = 0.2f
                    secondaryOverlay?.visibility = View.VISIBLE
                    mAuth?.signInWithEmailAndPassword(email_id, password)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val database = FirebaseDatabase.getInstance()
                                val myRef = database.getReference("Users")
                                myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (user in snapshot.children) {
                                            val tempUser: User? = user.getValue(
                                                User::class.java
                                            )
                                            if (tempUser?.email_id.equals(email_id)) {
                                                val sharedpreferences = getSharedPreferences(
                                                    "personal_details",
                                                    MODE_PRIVATE
                                                )
                                                val editor = sharedpreferences.edit()
                                                editor.putString("name", tempUser?.name)
                                                editor.putString("username", tempUser?.username)
                                                editor.putString("email_id", email_id)
                                                val storage = FirebaseStorage.getInstance()
                                                val storageRef = storage.reference
                                                val imagesRef = storageRef.child("images/" + tempUser?.username.toString() + ".jpg"
                                                )
                                                imagesRef.getBytes(Long.MAX_VALUE)
                                                    .addOnSuccessListener { bytes: ByteArray? ->
                                                        val publicKeyRef = storageRef.child(
                                                            "Public_Keys/" + tempUser?.username
                                                                .toString() + ".key"
                                                        )
                                                        publicKeyRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes1: ByteArray? ->
                                                                val editor1 = keycheck.edit()
                                                                val encodedPublicKey = Base64.encodeToString(bytes1, Base64.DEFAULT)
                                                                editor1.putString("publicKey", encodedPublicKey)
                                                                editor1.commit()
                                                                val encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT)
                                                                editor.putString("image_data", encodedImage)
                                                                editor.commit()
                                                                val intent = Intent(this@LoginActivity, UserListActivity::class.java)
                                                                startActivity(intent)
                                                                finish()
                                                            }
                                                    }
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            } else Toast.makeText(
                                this@LoginActivity,
                                "Wrong email or password!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
        }
    }

    private fun verifyCredentials(email_id: String, password: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"
        val pat = Pattern.compile(emailRegex)
        return if (!pat.matcher(email_id)
                .matches()
        ) false else password.length in 6..15
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = mAuth!!.currentUser
    }
}