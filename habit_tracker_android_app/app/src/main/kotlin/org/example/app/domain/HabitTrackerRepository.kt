package org.example.app.domain

import org.example.app.domain.model.Habit
import org.example.app.domain.model.HabitId
import org.example.app.domain.model.HabitStat
import org.example.app.domain.model.OverallStats
import org.example.app.domain.model.TodaySummary

interface HabitTrackerRepository {
    // PUBLIC_INTERFACE
    suspend fun getHabitsLocal(): List<Habit>
    /** Returns locally persisted list of habits with today's completion + streak precomputed. */

    // PUBLIC_INTERFACE
    suspend fun getHabitLocal(habitId: HabitId): Habit?
    /** Returns locally persisted habit or null if missing. */

    // PUBLIC_INTERFACE
    suspend fun getTodaySummaryLocal(): TodaySummary
    /** Returns today's summary from local DB. */

    // PUBLIC_INTERFACE
    suspend fun createHabit(name: String, description: String?): Result<Unit>
    /** Creates a habit locally and attempts backend create if configured. */

    // PUBLIC_INTERFACE
    suspend fun updateHabit(habit: Habit): Result<Unit>
    /** Updates a habit locally and attempts backend update if configured. */

    // PUBLIC_INTERFACE
    suspend fun deleteHabit(habitId: HabitId): Result<Unit>
    /** Deletes a habit locally and attempts backend delete if configured. */

    // PUBLIC_INTERFACE
    suspend fun setDoneToday(habitId: HabitId, done: Boolean): Result<Unit>
    /** Sets today's completion locally and attempts backend push if configured. */

    // PUBLIC_INTERFACE
    suspend fun syncFromBackend(): Result<Unit>
    /**
     * Pulls remote data and merges into local DB (best-effort).
     * If backend doesn't provide endpoints, this will safely no-op and return failure with context.
     */

    // PUBLIC_INTERFACE
    suspend fun getOverallStatsLocal(): OverallStats
    /** Overall stats from local DB. */

    // PUBLIC_INTERFACE
    suspend fun getPerHabitStatsLocal(): List<HabitStat>
    /** Per-habit stats from local DB. */
}
