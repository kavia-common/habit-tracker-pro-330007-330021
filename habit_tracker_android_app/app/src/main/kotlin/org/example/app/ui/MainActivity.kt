package org.example.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.example.app.R
import org.example.app.data.AppDatabase
import org.example.app.data.RepositoryProvider
import org.example.app.domain.model.HabitId
import org.example.app.ui.adapter.HabitsAdapter
import org.example.app.ui.vm.HabitsViewModel
import org.example.app.ui.vm.HabitsViewModelFactory
import org.example.app.util.UiMessages
import org.example.app.util.toPercentInt

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: HabitsViewModel
    private lateinit var adapter: HabitsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_stats -> {
                    startActivity(Intent(this, StatsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        val db = AppDatabase.getInstance(applicationContext)
        val repository = RepositoryProvider.provide(applicationContext, db)

        viewModel = ViewModelProvider(
            this,
            HabitsViewModelFactory(repository)
        )[HabitsViewModel::class.java]

        val recycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.habitsRecycler)
        val emptyState = findViewById<TextView>(R.id.emptyState)
        val swipeRefresh = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)

        val progressSubtitle = findViewById<TextView>(R.id.todaySummarySubtitle)
        val progressBar = findViewById<LinearProgressIndicator>(R.id.todayProgress)

        adapter = HabitsAdapter(
            onHabitClicked = { habit ->
                EditHabitActivity.startForEdit(this, habit.id.value)
            },
            onToggleDoneToday = { habit, newDone ->
                viewModel.setDoneToday(habit.id, newDone)
            },
            onDeleteHabit = { habit ->
                UiMessages.confirmDeleteHabit(
                    context = this,
                    onConfirm = { viewModel.deleteHabit(habit.id) }
                )
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        swipeRefresh.setOnRefreshListener {
            viewModel.syncFromBackend()
        }

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            EditHabitActivity.startForCreate(this)
        }

        viewModel.habits.observe(this) { habits ->
            adapter.submitList(habits)
            emptyState.visibility = if (habits.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.todaySummary.observe(this) { summary ->
            val percent = summary.completionRate.toPercentInt()
            progressBar.progress = percent
            progressSubtitle.text = "${summary.doneCount}/${summary.totalCount} habits done • $percent%"
        }

        viewModel.loading.observe(this) { loading ->
            swipeRefresh.isRefreshing = loading
        }

        viewModel.oneShotMessage.observe(this) { message ->
            if (message != null) {
                UiMessages.toast(this, message)
                viewModel.consumeOneShotMessage()
            }
        }

        // Initial load: local first, then attempt backend sync.
        viewModel.load()
    }

    override fun onResume() {
        super.onResume()
        // Refresh list after returning from add/edit.
        viewModel.loadLocalOnly()
    }
}
