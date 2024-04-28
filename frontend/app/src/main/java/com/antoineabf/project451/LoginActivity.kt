package com.antoineabf.project451

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.antoineabf.project451.api.Authentication
import com.antoineabf.project451.api.CellDataService
import com.antoineabf.project451.api.model.Token
import com.antoineabf.project451.api.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private var usernameEditText: TextInputLayout? = null
    private var passwordEditText: TextInputLayout? = null
    private var submitButton: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        usernameEditText = findViewById(R.id.txtInptUsername)
        passwordEditText = findViewById(R.id.txtInptPassword)
        submitButton = findViewById(R.id.btnSubmit)
        submitButton?.setOnClickListener { view ->
            authentication()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun authentication(){
        val user = User()
        user.username = usernameEditText?.editText?.text.toString()
        user.password = passwordEditText?.editText?.text.toString()
        if (usernameEditText?.editText?.text?.isEmpty() == true || passwordEditText?.editText?.text?.isEmpty() == true) {
            Snackbar.make(
                submitButton as View, "Please provide all info!",
                Snackbar.LENGTH_LONG
            )
                .show()
            return
        }
        else {
            CellDataService.CellDataApi().authenticate(user).enqueue(object :
                Callback<Token> {
                override fun onFailure(call: Call<Token>, t: Throwable) {
                    Snackbar.make(
                        submitButton as View,
                        "Could not login to account.",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }

                //Saves the token, user type, and username
                override fun onResponse(
                    call: Call<Token>, response:
                    Response<Token>
                ) {
                    if (response.isSuccessful) {
                        Snackbar.make(
                            submitButton as View,
                            "Logged in",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        response.body()?.token?.let {
                            Authentication.saveToken(it)
                        }
                        onCompleted()
                    } else {
                        Snackbar.make(
                            submitButton as View,
                            "Failed login attempt",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
                }
            })
        }
    }

    private fun onCompleted(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}