package com.example.data

import kotlinx.coroutines.flow.Flow

class LinkRepository(private val linkDao: LinkDao) {
    val allScheduledLinks: Flow<List<ScheduledLink>> = linkDao.getAllScheduledLinks()
    val allHistoryLogs: Flow<List<HistoryLog>> = linkDao.getAllHistoryLogs()

    suspend fun getScheduledLinkById(id: Int): ScheduledLink? {
        return linkDao.getScheduledLinkById(id)
    }

    suspend fun getActiveScheduledLinks(): List<ScheduledLink> {
        return linkDao.getActiveScheduledLinks()
    }

    suspend fun insertScheduledLink(link: ScheduledLink): Long {
        return linkDao.insertScheduledLink(link)
    }

    suspend fun updateScheduledLink(link: ScheduledLink) {
        linkDao.updateScheduledLink(link)
    }

    suspend fun deleteScheduledLink(link: ScheduledLink) {
        linkDao.deleteScheduledLink(link)
    }

    suspend fun deleteScheduledLinkById(id: Int) {
        linkDao.deleteScheduledLinkById(id)
    }

    suspend fun insertHistoryLog(log: HistoryLog): Long {
        return linkDao.insertHistoryLog(log)
    }

    suspend fun clearHistoryLogs() {
        linkDao.clearHistoryLogs()
    }
}
