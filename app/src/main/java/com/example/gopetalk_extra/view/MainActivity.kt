package com.example.gopetalk_extra.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gopetalk_extra.R
import com.example.gopetalk_extra.data.storage.SessionManager
import com.example.gopetalk_extra.view.auth.LoginActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.walkie_talkie_activity)

        sessionManager = SessionManager(this)

        val logoutButton = findViewById<MaterialButton>(R.id.btn_logout)
        logoutButton.setOnClickListener {
            sessionManager.clearSession()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

}

