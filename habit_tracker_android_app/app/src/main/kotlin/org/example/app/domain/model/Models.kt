package org.example.app.domain.model

@JvmInline
value class HabitId(val value: String)

data class Habit(
    val id: HabitId,
    val name: String,
    val description: String?,
    val doneToday: Boolean,
    val currentStreakDays: Int
)

data class TodaySummary(
    val totalCount: Int,
    val doneCount: Int,
    val completionRate: Double
)

data class OverallStats(
    val totalHabits: Int,
    val doneToday: Int,
    val completionRate: Double
)

data class HabitStat(
    val habitId: HabitId,
    val name: String,
    val currentStreakDays: Int,
    val completionRate: Double
)
