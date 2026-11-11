package com.example.applimit

import android.app.AppOpsManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }
        status = TextView(this).apply { textSize = 16f }
        layout.addView(status)

        layout.addView(Button(this).apply {
            text = "১. Usage Access চালু করো"
            setOnClickListener { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
        })
        layout.addView(Button(this).apply {
            text = "২. Overlay পারমিশন দাও"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")))
            }
        })
        layout.addView(Button(this).apply {
            text = "৩. মনিটর চালু করো"
            setOnClickListener {
                if (hasUsageAccess() && Settings.canDrawOverlays(this@MainActivity)) {
                    startForegroundService(Intent(this@MainActivity, MonitorService::class.java))
                }
                refresh()
            }
        })
        setContentView(layout)
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        status.text = "Usage Access: ${if (hasUsageAccess()) "✅" else "❌"}\n" +
            "Overlay: ${if (Settings.canDrawOverlays(this)) "✅" else "❌"}\n"
    }

    private fun hasUsageAccess(): Boolean {
        val ops = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        return ops.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName
        ) == AppOpsManager.MODE_ALLOWED
    }
}
