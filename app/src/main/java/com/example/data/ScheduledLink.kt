package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_links")
data class ScheduledLink(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val hour: Int,
    val minute: Int,
    val repeatDays: String, // Comma separated days of week "2,3,4,5,6,7,8" (2=Monday, ..., 8=Sunday) or "daily"
    val isActive: Boolean = true,
    val groupName: String = "Chung",
    val isIntervalMode: Boolean = false,
    val intervalMinutes: Int = 0
) {
    fun getRepeatDaysText(): String {
        if (isIntervalMode) {
            return "Chu kỳ: $intervalMinutes phút"
        }
        if (repeatDays == "daily" || repeatDays.split(",").filter { it.isNotEmpty() }.size == 7) {
            return "Hàng ngày"
        }
        if (repeatDays.isEmpty()) {
            return "Một lần"
        }
        val dayList = repeatDays.split(",").mapNotNull { it.toIntOrNull() }.sorted()
        if (dayList.isEmpty()) return "Một lần"
        
        // If weekdays (T2 - T6)
        val weekdays = listOf(2, 3, 4, 5, 6)
        if (dayList == weekdays) {
            return "T2 - T6"
        }
        // If weekend (T7, CN)
        val weekends = listOf(7, 8)
        if (dayList == weekends) {
            return "Cuối tuần"
        }

        return dayList.joinToString(", ") { day ->
            when (day) {
                2 -> "T2"
                3 -> "T3"
                4 -> "T4"
                5 -> "T5"
                6 -> "T6"
                7 -> "T7"
                8 -> "CN"
                else -> ""
            }
        }.trim().removeSurrounding(", ")
    }
}
