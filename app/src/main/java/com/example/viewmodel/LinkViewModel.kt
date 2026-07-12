package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HistoryLog
import com.example.data.LinkRepository
import com.example.data.ScheduledLink
import com.example.scheduler.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LinkViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LinkRepository
    private val scheduler: AlarmScheduler

    val scheduledLinks: StateFlow<List<ScheduledLink>>
    val historyLogs: StateFlow<List<HistoryLog>>

    init {
        val linkDao = AppDatabase.getDatabase(application).linkDao()
        repository = LinkRepository(linkDao)
        scheduler = AlarmScheduler(application)

        scheduledLinks = repository.allScheduledLinks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        historyLogs = repository.allHistoryLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun addScheduledLink(title: String, url: String, hour: Int, minute: Int, repeatDays: String, groupName: String, isIntervalMode: Boolean = false, intervalMinutes: Int = 0) {
        viewModelScope.launch {
            val link = ScheduledLink(
                title = title,
                url = url,
                hour = hour,
                minute = minute,
                repeatDays = repeatDays,
                groupName = if (groupName.isBlank()) "Chung" else groupName.trim(),
                isActive = true,
                isIntervalMode = isIntervalMode,
                intervalMinutes = intervalMinutes
            )
            val generatedId = repository.insertScheduledLink(link)
            val savedLink = link.copy(id = generatedId.toInt())
            scheduler.scheduleAlarm(savedLink)
        }
    }

    fun toggleScheduledLink(link: ScheduledLink) {
        viewModelScope.launch {
            val updatedLink = link.copy(isActive = !link.isActive)
            repository.updateScheduledLink(updatedLink)
            if (updatedLink.isActive) {
                scheduler.scheduleAlarm(updatedLink)
            } else {
                scheduler.cancelAlarm(updatedLink)
            }
        }
    }

    fun updateScheduledLink(link: ScheduledLink) {
        viewModelScope.launch {
            repository.updateScheduledLink(link)
            if (link.isActive) {
                scheduler.scheduleAlarm(link)
            } else {
                scheduler.cancelAlarm(link)
            }
        }
    }

    fun deleteScheduledLink(link: ScheduledLink) {
        viewModelScope.launch {
            scheduler.cancelAlarm(link)
            repository.deleteScheduledLink(link)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistoryLogs()
        }
    }
}
