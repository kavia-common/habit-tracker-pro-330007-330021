package org.example.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import org.example.app.R
import org.example.app.data.AppDatabase
import org.example.app.data.RepositoryProvider
import org.example.app.domain.model.HabitId
import org.example.app.ui.vm.EditHabitViewModel
import org.example.app.ui.vm.EditHabitViewModelFactory
import org.example.app.util.UiMessages

class EditHabitActivity : ComponentActivity() {

    private lateinit var viewModel: EditHabitViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_habit)

        val habitId = intent.getStringExtra(EXTRA_HABIT_ID)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarEdit)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = if (habitId == null) getString(R.string.add_habit) else getString(R.string.edit_habit)

        val db = AppDatabase.getInstance(applicationContext)
        val repository = RepositoryProvider.provide(applicationContext, db)

        viewModel = ViewModelProvider(
            this,
            EditHabitViewModelFactory(repository)
        )[EditHabitViewModel::class.java]

        val inputName = findViewById<TextInputEditText>(R.id.inputName)
        val inputDesc = findViewById<TextInputEditText>(R.id.inputDescription)

        if (habitId != null) {
            viewModel.loadHabit(HabitId(habitId))
        }

        viewModel.form.observe(this) { form ->
            if (inputName.text?.toString() != form.name) inputName.setText(form.name)
            if (inputDesc.text?.toString() != form.description) inputDesc.setText(form.description)
        }

        findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            val name = inputName.text?.toString()?.trim().orEmpty()
            val desc = inputDesc.text?.toString()?.trim().orEmpty()
            viewModel.save(name, desc.ifBlank { null })
        }

        viewModel.finishEvent.observe(this) { shouldFinish ->
            if (shouldFinish == true) finish()
        }

        viewModel.oneShotMessage.observe(this) { message ->
            if (message != null) {
                UiMessages.toast(this, message)
                viewModel.consumeOneShotMessage()
            }
        }
    }

    companion object {
        private const val EXTRA_HABIT_ID = "extra_habit_id"

        // PUBLIC_INTERFACE
        fun startForCreate(context: Context) {
            /** Starts the habit create screen. */
            context.startActivity(Intent(context, EditHabitActivity::class.java))
        }

        // PUBLIC_INTERFACE
        fun startForEdit(context: Context, habitId: String) {
            /** Starts the habit edit screen for the given habit id. */
            context.startActivity(
                Intent(context, EditHabitActivity::class.java).putExtra(EXTRA_HABIT_ID, habitId)
            )
        }
    }
}
