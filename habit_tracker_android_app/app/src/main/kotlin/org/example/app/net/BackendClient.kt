package org.example.app.net

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.example.app.data.dao.CompletionDao
import org.example.app.data.dao.HabitDao
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class BackendClient private constructor(
    private val api: BackendService
) {
    private val tag = "BackendClient"

    suspend fun tryCreateHabit(id: String, name: String, description: String?) {
        // Currently no endpoint; keeping explicit for future integration.
        Log.i(tag, "tryCreateHabit noop (backend endpoints not implemented): id=$id")
    }

    suspend fun tryUpdateHabit(id: String, name: String, description: String?) {
        Log.i(tag, "tryUpdateHabit noop (backend endpoints not implemented): id=$id")
    }

    suspend fun tryDeleteHabit(id: String) {
        Log.i(tag, "tryDeleteHabit noop (backend endpoints not implemented): id=$id")
    }

    suspend fun trySetDoneToday(id: String, date: String, done: Boolean) {
        Log.i(tag, "trySetDoneToday noop (backend endpoints not implemented): id=$id date=$date done=$done")
    }

    suspend fun trySyncIntoLocal(habitDao: HabitDao, completionDao: CompletionDao) {
        // We can at least check health.
        api.health()
        throw IllegalStateException("Backend OpenAPI only exposes GET /. Habit endpoints not available yet.")
    }

    companion object {
        /**
         * Creates a backend client if a base URL is configured.
         * Since no env vars are declared for this container, this currently returns a client pointing at the
         * known dev URL host if reachable. If not, returns null.
         */
        // PUBLIC_INTERFACE
        fun createOrNull(context: Context): BackendClient? {
            /** Creates an optional backend client. Returns null if not configured. */
            // No container env available; keep null by default to avoid hard-coding deployment URLs.
            return null
        }

        fun createWithBaseUrl(baseUrl: String): BackendClient {
            val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            val http = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(http)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return BackendClient(retrofit.create(BackendService::class.java))
        }
    }
}
