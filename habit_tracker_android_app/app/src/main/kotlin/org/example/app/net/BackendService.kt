package org.example.app.net

import retrofit2.http.GET

interface BackendService {
    @GET("/")
    suspend fun health(): Map<String, Any?>
}
