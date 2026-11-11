package com.example.applimit

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import java.util.Calendar

class MonitorService : Service() {

    companion object {
        const val LIMIT_MS = 60 * 60 * 1000L  // ১ ঘণ্টা
        const val CHANNEL_ID = "monitor"
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var usm: UsageStatsManager
    private var launcherPkg: String? = null

    private val tick = object : Runnable {
        override fun run() {
            check()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        launcherPkg = packageManager.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0
        )?.activityInfo?.packageName
        startForeground(1, buildNotification())
        handler.post(tick)
    }

    private fun check() {
        val pkg = foregroundPackage() ?: return
        if (isExempt(pkg)) return
        if (usedTodayMs(pkg) >= LIMIT_MS) {
            startActivity(
                Intent(this, BlockActivity::class.java)
                    .putExtra("pkg", pkg)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    // এই মুহূর্তে কোন অ্যাপ সামনে আছে
    private fun foregroundPackage(): String? {
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(now - 10_000, now)
        val e = UsageEvents.Event()
        var last: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            if (e.eventType == UsageEvents.Event.ACTIVITY_RESUMED) last = e.packageName
        }
        return last
    }

    // আজকের (মধ্যরাত থেকে) মোট ব্যবহার
    private fun usedTodayMs(pkg: String): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val stats = usm.queryAndAggregateUsageStats(cal.timeInMillis, System.currentTimeMillis())
        return stats[pkg]?.totalTimeInForeground ?: 0L
    }

    // যেগুলো ব্লক হবে না: নিজের অ্যাপ, লঞ্চার, সিস্টেম UI, ডায়ালার
    private fun isExempt(pkg: String) =
        pkg == packageName || pkg == launcherPkg ||
        pkg == "com.android.systemui" || pkg.contains("dialer") || pkg.contains("emergency")

    private fun buildNotification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Monitor", NotificationManager.IMPORTANCE_LOW)
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("App Limiter চালু আছে")
            .setContentText("প্রতিটি অ্যাপের দৈনিক লিমিট: ১ ঘণ্টা")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .build()
    }

    override fun onStartCommand(i: Intent?, f: Int, id: Int) = START_STICKY
    override fun onDestroy() { handler.removeCallbacks(tick); super.onDestroy() }
    override fun onBind(i: Intent?): IBinder? = null
}
