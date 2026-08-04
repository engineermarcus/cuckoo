package com.cuckoo.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {

    private lateinit var textStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        textStatus = findViewById(R.id.textAccessibilityStatus)

        findViewById<MaterialButton>(R.id.btnEnableAccessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        val enabled = AppFreezeService.isEnabled(this)
        textStatus.text = if (enabled) "Currently enabled" else "Currently disabled"
        textStatus.setTextColor(androidx.core.content.ContextCompat.getColor(this, if (enabled) R.color.cuckoo_success else R.color.cuckoo_error))
    }
}
