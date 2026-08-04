package com.cuckoo.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class ManageAppsActivity : AppCompatActivity() {

    private lateinit var apps: List<AppEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val existingWhitelist = AppWhitelistRepository.getWhitelist(this)

        val pm = packageManager
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = pm.queryIntentActivities(launchIntent, PackageManager.MATCH_ALL)

        apps = resolved
            .map { it.activityInfo.packageName }
            .distinct()
            .filter { it != packageName }
            .map { pkg ->
                val info = pm.getApplicationInfo(pkg, 0)
                AppEntry(
                    packageName = pkg,
                    label = pm.getApplicationLabel(info).toString(),
                    icon = pm.getApplicationIcon(info),
                    checked = existingWhitelist.contains(pkg)
                )
            }
            .sortedBy { it.label.lowercase() }

        val adapter = InstalledAppsAdapter(apps)
        findViewById<RecyclerView>(R.id.recyclerApps).apply {
            layoutManager = LinearLayoutManager(this@ManageAppsActivity)
            this.adapter = adapter
        }

        findViewById<MaterialButton>(R.id.btnContinue).apply {
            text = "Save"
            setOnClickListener {
                val chosen = apps.filter { it.checked }.map { it.packageName }.toSet()
                AppWhitelistRepository.saveWhitelist(this@ManageAppsActivity, chosen)
                finish()
            }
        }
    }
}
