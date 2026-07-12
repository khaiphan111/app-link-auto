package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.AppDatabase
import com.example.scheduler.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device reboot detected. Re-scheduling active alarms...")
            
            val database = AppDatabase.getDatabase(context)
            val linkDao = database.linkDao()
            val scheduler = AlarmScheduler(context)

            CoroutineScope(Dispatchers.IO).launch {
                val activeLinks = linkDao.getActiveScheduledLinks()
                Log.d("BootReceiver", "Found ${activeLinks.size} active links to reschedule")
                for (link in activeLinks) {
                    scheduler.scheduleAlarm(link)
                }
            }
        }
    }
}
