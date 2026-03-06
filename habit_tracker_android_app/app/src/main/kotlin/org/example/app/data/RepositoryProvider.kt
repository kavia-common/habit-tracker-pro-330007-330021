package org.example.app.data

import android.content.Context
import org.example.app.domain.DefaultHabitTrackerRepository
import org.example.app.domain.HabitTrackerRepository
import org.example.app.net.BackendClient
import org.example.app.util.DateProvider
import org.example.app.util.SystemDateProvider

object RepositoryProvider {

    // PUBLIC_INTERFACE
    fun provide(context: Context, db: AppDatabase): HabitTrackerRepository {
        /**
         * Creates the canonical repository instance for the app.
         * Contract: returns a fully functional local-first repository; backend sync is best-effort and optional.
         */
        val backendClient: BackendClient? = BackendClient.createOrNull(context)
        val dateProvider: DateProvider = SystemDateProvider()
        return DefaultHabitTrackerRepository(
            habitDao = db.habitDao(),
            completionDao = db.completionDao(),
            backendClient = backendClient,
            dateProvider = dateProvider
        )
    }
}
