package org.example.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import org.example.app.R
import org.example.app.data.AppDatabase
import org.example.app.data.RepositoryProvider
import org.example.app.ui.adapter.HabitStatsAdapter
import org.example.app.ui.vm.StatsViewModel
import org.example.app.ui.vm.StatsViewModelFactory
import org.example.app.util.toPercentInt

class StatsActivity : ComponentActivity() {

    private lateinit var viewModel: StatsViewModel
    private lateinit var adapter: HabitStatsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarStats)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = getString(R.string.stats_title)

        val db = AppDatabase.getInstance(applicationContext)
        val repository = RepositoryProvider.provide(applicationContext, db)

        viewModel = ViewModelProvider(
            this,
            StatsViewModelFactory(repository)
        )[StatsViewModel::class.java]

        val recycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.statsRecycler)
        adapter = HabitStatsAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val overallSubtitle = findViewById<android.widget.TextView>(R.id.overallSubtitle)

        viewModel.overall.observe(this) { overall ->
            val percent = overall.completionRate.toPercentInt()
            overallSubtitle.text = "Total habits: ${overall.totalHabits} • Today: ${overall.doneToday}/${overall.totalHabits} • $percent% avg"
        }

        viewModel.perHabit.observe(this) { list ->
            adapter.submitList(list)
        }

        viewModel.load()
    }
}
