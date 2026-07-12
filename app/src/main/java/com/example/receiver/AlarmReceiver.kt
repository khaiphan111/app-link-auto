package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.AppDatabase
import com.example.data.HistoryLog
import com.example.scheduler.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val linkId = intent.getIntExtra("LINK_ID", -1)
        if (linkId == -1) return

        Log.d("AlarmReceiver", "Alarm received for link ID: $linkId")

        val database = AppDatabase.getDatabase(context)
        val linkDao = database.linkDao()
        val scheduler = AlarmScheduler(context)

        CoroutineScope(Dispatchers.IO).launch {
            val link = linkDao.getScheduledLinkById(linkId) ?: return@launch

            if (!link.isActive) {
                Log.d("AlarmReceiver", "Link ID $linkId is inactive. Skipping execution.")
                return@launch
            }

            // Check if today matches the repeat days
            val calendar = Calendar.getInstance()
            val calendarDay = calendar.get(Calendar.DAY_OF_WEEK)
            // Map Sunday to 8, Monday to 2, Tuesday to 3, etc. to align with our day system
            val todayDayValue = if (calendarDay == Calendar.SUNDAY) 8 else calendarDay

            val repeatDaysList = link.repeatDays.split(",").filter { it.isNotEmpty() }
            val isDaily = link.repeatDays == "daily" || repeatDaysList.size == 7
            val isOnce = !link.isIntervalMode && link.repeatDays.isEmpty()
            val isScheduledForToday = link.isIntervalMode || isDaily || isOnce || repeatDaysList.contains(todayDayValue.toString())

            if (isScheduledForToday) {
                Log.d("AlarmReceiver", "Triggering URL open: ${link.title} -> ${link.url}")
                
                var success = false
                try {
                    var formattedUrl = link.url.trim()
                    if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
                        formattedUrl = "https://$formattedUrl"
                    }

                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    success = true
                } catch (e: Exception) {
                    Log.e("AlarmReceiver", "Failed to launch browser directly from background receiver", e)
                }

                // Log execution to Database history
                linkDao.insertHistoryLog(
                    HistoryLog(
                        linkTitle = link.title,
                        url = link.url,
                        status = if (success) "SUCCESS" else "FAILED"
                    )
                )

                // Dispatch detailed head-up notification
                showNotification(context, link.title, link.url, link.id)

                // If scheduled only once, disable it now
                if (isOnce) {
                    linkDao.updateScheduledLink(link.copy(isActive = false))
                }
            } else {
                Log.d("AlarmReceiver", "Today ($todayDayValue) is not a scheduled repeat day for: ${link.title}. Skipping browser launch.")
            }

            // Reschedule the alarm for the next occurrence
            if (!isOnce && link.isActive) {
                scheduler.scheduleAlarm(link)
            }
        }
    }

    private fun showNotification(context: Context, title: String, url: String, id: Int) {
        val channelId = "link_opener_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lịch Hẹn Giờ Mở Link",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh thông báo khi tự động mở liên kết web"
            }
            notificationManager.createNotificationChannel(channel)
        }

        var formattedUrl = url.trim()
        if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
            formattedUrl = "https://$formattedUrl"
        }
        
        val openIntent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            id,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🌐 Đã mở liên kết theo lịch")
            .setContentText("$title - $url")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Hẹn giờ mở Link đã kích hoạt:\n$title\n\nLink: $url"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_menu_compass, "Mở link ngay", openPendingIntent)
            .build()

        notificationManager.notify(id, notification)
    }
}
