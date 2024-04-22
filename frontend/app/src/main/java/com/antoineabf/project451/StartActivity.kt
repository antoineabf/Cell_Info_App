package com.antoineabf.project451

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.antoineabf.project451.api.Authentication

class StartActivity : AppCompatActivity() {

    private var registerBut: Button? = null
    private var loginBut: Button? = null
    private var guestBut: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.activity_start)

        registerBut = findViewById(R.id.registerButton)
        loginBut = findViewById(R.id.loginButton)
        guestBut = findViewById(R.id.guestButton)

        registerBut?.setOnClickListener { view ->
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        loginBut?.setOnClickListener { view ->
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        guestBut?.setOnClickListener { view ->
            Authentication.saveToken("guest")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}