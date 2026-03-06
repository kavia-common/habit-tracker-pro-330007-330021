package org.example.app.data.entity

import androidx.room.Entity

@Entity(
    tableName = "completions",
    primaryKeys = ["habitId", "date"]
)
data class CompletionEntity(
    val habitId: String,
    /**
     * ISO-8601 date string (yyyy-MM-dd).
     * Invariant: Always stored in local device date (no timezone conversions).
     */
    val date: String
)
