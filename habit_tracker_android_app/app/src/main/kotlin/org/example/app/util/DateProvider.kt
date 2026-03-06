package org.example.app.util

import java.time.LocalDate

interface DateProvider {
    // PUBLIC_INTERFACE
    fun today(): LocalDate
    /** Returns device-local current date (no time). */
}

class SystemDateProvider : DateProvider {
    override fun today(): LocalDate = LocalDate.now()
}
