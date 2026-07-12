package com.example.scheduler

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.ScheduledLink
import com.example.receiver.AlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarm(link: ScheduledLink) {
        if (!link.isActive) {
            cancelAlarm(link)
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("LINK_ID", link.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            link.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = if (link.isIntervalMode) {
            System.currentTimeMillis() + link.intervalMinutes * 60 * 1000L
        } else {
            calculateNextTriggerTime(link.hour, link.minute)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d("AlarmScheduler", "Scheduled exact alarm for link ID ${link.id} (${link.title})")
        } catch (e: SecurityException) {
            // Fallback to inexact alarm if permission is missing
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.e("AlarmScheduler", "SecurityException scheduling exact alarm, falling back to standard set()", e)
        }
    }

    fun cancelAlarm(link: ScheduledLink) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            link.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("AlarmScheduler", "Cancelled alarm for link ID ${link.id}")
        }
    }

    private fun calculateNextTriggerTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the calculated time is in the past, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }
}
