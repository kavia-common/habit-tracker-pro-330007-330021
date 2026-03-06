package org.example.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.example.app.data.entity.CompletionEntity

@Dao
interface CompletionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CompletionEntity)

    @Query("DELETE FROM completions WHERE habitId = :habitId AND date = :date")
    suspend fun delete(habitId: String, date: String)

    @Query("DELETE FROM completions WHERE habitId = :habitId")
    suspend fun deleteForHabit(habitId: String)

    @Query("SELECT COUNT(*) FROM completions WHERE date = :date")
    suspend fun countDoneForDate(date: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM completions WHERE habitId = :habitId AND date = :date)")
    suspend fun isDoneForDate(habitId: String, date: String): Boolean

    @Query("SELECT habitId FROM completions WHERE date = :date")
    suspend fun getDoneHabitIdsForDate(date: String): List<String>

    @Query("SELECT COUNT(*) FROM completions WHERE habitId = :habitId")
    suspend fun countTotalCompletions(habitId: String): Int

    @Query("SELECT COUNT(*) FROM completions WHERE habitId = :habitId")
    suspend fun countDoneAllTime(habitId: String): Int

    @Query("SELECT COUNT(*) FROM completions WHERE habitId = :habitId")
    suspend fun getTotalCompletionDays(habitId: String): Int

    @Query("SELECT COUNT(*) FROM completions WHERE habitId = :habitId")
    suspend fun getDoneCompletionDays(habitId: String): Int

    suspend fun getCompletionCounts(habitId: String): Pair<Int, Int> {
        // For this app, each completion row represents a "done" day, so doneCount == totalDays for now.
        // The pair is kept to preserve a stable contract if partial completion is introduced later.
        val total = getTotalCompletionDays(habitId)
        val done = getDoneCompletionDays(habitId)
        return done to total
    }

    /**
     * Current streak calculation:
     * Count consecutive days ending at `today` (inclusive if done today, otherwise starting from yesterday),
     * using a recursive CTE over date strings. This keeps logic in one place (DB) and is easy to audit.
     */
    @Query(
        """
        WITH RECURSIVE streak(d) AS (
            SELECT :todayDate
            UNION ALL
            SELECT date(d, '-1 day')
            FROM streak
            WHERE EXISTS (
                SELECT 1 FROM completions c
                WHERE c.habitId = :habitId AND c.date = d
            )
        )
        SELECT COUNT(*) - 1 FROM streak
        """
    )
    suspend fun getCurrentStreakDays(habitId: String, todayDate: String): Int

    data class CompletionCountsRow(
        val doneCount: Int,
        val totalDays: Int
    )
}
