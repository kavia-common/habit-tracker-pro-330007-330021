package org.example.app.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import org.example.app.R

object UiMessages {

    // PUBLIC_INTERFACE
    fun toast(context: Context, message: String) {
        /** Shows a short toast message. */
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // PUBLIC_INTERFACE
    fun confirmDeleteHabit(context: Context, onConfirm: () -> Unit) {
        /** Shows a delete confirmation dialog for a habit. */
        AlertDialog.Builder(context)
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setNegativeButton(R.string.cancel) { d, _ -> d.dismiss() }
            .setPositiveButton(R.string.delete_habit) { d, _ ->
                d.dismiss()
                onConfirm()
            }
            .show()
    }
}
