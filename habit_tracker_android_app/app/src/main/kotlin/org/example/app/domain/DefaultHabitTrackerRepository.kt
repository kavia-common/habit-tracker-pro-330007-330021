package org.example.app.domain

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.app.data.dao.CompletionDao
import org.example.app.data.dao.HabitDao
import org.example.app.data.entity.CompletionEntity
import org.example.app.data.entity.HabitEntity
import org.example.app.domain.model.Habit
import org.example.app.domain.model.HabitId
import org.example.app.domain.model.HabitStat
import org.example.app.domain.model.OverallStats
import org.example.app.domain.model.TodaySummary
import org.example.app.net.BackendClient
import org.example.app.util.DateProvider
import java.time.LocalDate
import java.util.UUID

class DefaultHabitTrackerRepository(
    private val habitDao: HabitDao,
    private val completionDao: CompletionDao,
    private val backendClient: BackendClient?,
    private val dateProvider: DateProvider
) : HabitTrackerRepository {

    private val tag = "HabitRepo"

    override suspend fun getHabitsLocal(): List<Habit> = withContext(Dispatchers.IO) {
        val today = dateProvider.today()
        val habits = habitDao.getAll()
        val doneSet = completionDao.getDoneHabitIdsForDate(today.toString()).toSet()
        habits.map { e ->
            val streak = completionDao.getCurrentStreakDays(e.id, today.toString())
            Habit(
                id = HabitId(e.id),
                name = e.name,
                description = e.description,
                doneToday = doneSet.contains(e.id),
                currentStreakDays = streak
            )
        }
    }

    override suspend fun getHabitLocal(habitId: HabitId): Habit? = withContext(Dispatchers.IO) {
        val today = dateProvider.today()
        val e = habitDao.getById(habitId.value) ?: return@withContext null
        val done = completionDao.isDoneForDate(habitId.value, today.toString())
        val streak = completionDao.getCurrentStreakDays(habitId.value, today.toString())
        Habit(HabitId(e.id), e.name, e.description, done, streak)
    }

    override suspend fun getTodaySummaryLocal(): TodaySummary = withContext(Dispatchers.IO) {
        val today = dateProvider.today().toString()
        val total = habitDao.count()
        val done = completionDao.countDoneForDate(today)
        val rate = if (total == 0) 0.0 else done.toDouble() / total.toDouble()
        TodaySummary(total, done, rate)
    }

    override suspend fun createHabit(name: String, description: String?): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val id = UUID.randomUUID().toString()
            habitDao.insert(HabitEntity(id = id, name = name, description = description))
            // Backend best-effort
            backendClient?.tryCreateHabit(id, name, description)
        }.onFailure { e ->
            Log.e(tag, "createHabit failed", e)
        }.map { }
    }

    override suspend fun updateHabit(habit: Habit): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            habitDao.update(HabitEntity(id = habit.id.value, name = habit.name, description = habit.description))
            backendClient?.tryUpdateHabit(habit.id.value, habit.name, habit.description)
        }.onFailure { e -> Log.e(tag, "updateHabit failed habitId=${habit.id.value}", e) }
            .map { }
    }

    override suspend fun deleteHabit(habitId: HabitId): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            completionDao.deleteForHabit(habitId.value)
            habitDao.deleteById(habitId.value)
            backendClient?.tryDeleteHabit(habitId.value)
        }.onFailure { e -> Log.e(tag, "deleteHabit failed habitId=${habitId.value}", e) }
            .map { }
    }

    override suspend fun setDoneToday(habitId: HabitId, done: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val today = dateProvider.today().toString()
        runCatching {
            if (done) {
                completionDao.upsert(CompletionEntity(habitId = habitId.value, date = today))
            } else {
                completionDao.delete(habitId.value, today)
            }
            backendClient?.trySetDoneToday(habitId.value, today, done)
        }.onFailure { e -> Log.e(tag, "setDoneToday failed habitId=${habitId.value} done=$done", e) }
            .map { }
    }

    override suspend fun syncFromBackend(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val client = backendClient ?: throw IllegalStateException("Backend not configured (no endpoints available).")
            // This will throw until backend implements endpoints; we keep it explicit to avoid silent divergence.
            client.trySyncIntoLocal(habitDao, completionDao)
        }.onFailure { e ->
            Log.w(tag, "syncFromBackend failed (keeping local)", e)
        }.map { }
    }

    override suspend fun getOverallStatsLocal(): OverallStats = withContext(Dispatchers.IO) {
        val today = dateProvider.today().toString()
        val total = habitDao.count()
        val doneToday = completionDao.countDoneForDate(today)
        val rate = if (total == 0) 0.0 else doneToday.toDouble() / total.toDouble()
        OverallStats(totalHabits = total, doneToday = doneToday, completionRate = rate)
    }

    override suspend fun getPerHabitStatsLocal(): List<HabitStat> = withContext(Dispatchers.IO) {
        val today = dateProvider.today()
        val habits = habitDao.getAll()
        habits.map { h ->
            val streak = completionDao.getCurrentStreakDays(h.id, today.toString())
            val (doneCount, totalDays) = completionDao.getCompletionCounts(h.id)
            val rate = if (totalDays == 0) 0.0 else doneCount.toDouble() / totalDays.toDouble()
            HabitStat(
                habitId = HabitId(h.id),
                name = h.name,
                currentStreakDays = streak,
                completionRate = rate
            )
        }.sortedBy { it.name.lowercase() }
    }
}
