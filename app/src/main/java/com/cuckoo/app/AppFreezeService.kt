package com.cuckoo.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import java.time.LocalTime

/**
 * When a schedule item is currently active, this service watches for foreground-app
 * changes and kicks the user back to the home screen if they open anything outside
 * a small allowlist (Cuckoo itself, the dialer, and the default launcher).
 */
class AppFreezeService : AccessibilityService() {

    private var lastBlockedPackage: String? = null
    private var lastBlockedAt = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val activeItem = currentActiveItem() ?: return // no task running, nothing to freeze
        if (isAllowed(pkg)) return

        // Debounce: don't spam "go home" for the same package repeatedly within a second.
        val now = System.currentTimeMillis()
        if (pkg == lastBlockedPackage && now - lastBlockedAt < 1000) return
        lastBlockedPackage = pkg
        lastBlockedAt = now

        performGlobalAction(GLOBAL_ACTION_HOME)
        Toast.makeText(
            this,
            "${appLabel(pkg)} is frozen during \"${activeItem.label}\"",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onInterrupt() { /* no-op */ }

    private fun isAllowed(pkg: String): Boolean {
        if (pkg == packageName) return true
        if (ALLOWLIST_PREFIXES.any { pkg.startsWith(it) }) return true
        if (pkg == defaultHomePackage() || pkg == defaultDialerPackage()) return true
        return AppWhitelistRepository.getWhitelist(applicationContext).contains(pkg)
    }

    private fun defaultHomePackage(): String? {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return packageManager.resolveActivity(intent, 0)?.activityInfo?.packageName
    }

    private fun defaultDialerPackage(): String? {
        return getSystemService(android.telecom.TelecomManager::class.java)?.defaultDialerPackage
    }

    private fun appLabel(pkg: String): String = try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    } catch (e: Exception) {
        pkg
    }

    private fun currentActiveItem(): ScheduleItem? {
        val now = LocalTime.now()
        val items = ScheduleRepository.getItems(applicationContext)
        return items.firstOrNull { item ->
            if (!item.enabled) return@firstOrNull false
            val start = LocalTime.of(item.hour, item.minute)
            val end = LocalTime.of(item.endHour, item.endMinute)
            if (end.isAfter(start)) now.isAfter(start) && now.isBefore(end)
            else now.isAfter(start) || now.isBefore(end) // overnight window
        }
    }

    companion object {
        // System UI, settings, and phone/emergency essentials always stay usable.
        private val ALLOWLIST_PREFIXES = listOf(
            "com.android.systemui",
            "com.android.settings",
            "com.android.server.telecom",
            "com.android.phone",
            "com.android.emergency"
        )

        fun isEnabled(context: android.content.Context): Boolean {
            val enabledServices = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val target = "${context.packageName}/${AppFreezeService::class.java.name}"
            return enabledServices.split(":").any { it.equals(target, ignoreCase = true) }
        }
    }
}
