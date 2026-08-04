package com.cuckoo.app

import android.app.Application
import java.io.File

class CuckooApp : Application() {
    override fun onCreate() {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            try {
                val f = File(filesDir, "crash.txt")
                f.writeText(throwable.stackTraceToString())
            } catch (e: Exception) {}
            Runtime.getRuntime().exit(1)
        }
        super.onCreate()
    }
}
