package com.example.applimit

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BlockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }
        layout.addView(TextView(this).apply {
            text = "আজকের সময় শেষ ⏰\nএই অ্যাপ আজ আর চলবে না।"
            textSize = 22f
            gravity = Gravity.CENTER
        })
        layout.addView(Button(this).apply {
            text = "হোমে যাও"
            setOnClickListener { goHome() }
        })
        setContentView(layout)
    }

    private fun goHome() {
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() = goHome()
}
