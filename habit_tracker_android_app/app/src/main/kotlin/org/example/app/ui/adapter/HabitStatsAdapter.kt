package org.example.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.example.app.R
import org.example.app.domain.model.HabitStat
import org.example.app.util.toPercentInt

class HabitStatsAdapter : ListAdapter<HabitStat, HabitStatsAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<HabitStat>() {
        override fun areItemsTheSame(oldItem: HabitStat, newItem: HabitStat): Boolean = oldItem.habitId == newItem.habitId
        override fun areContentsTheSame(oldItem: HabitStat, newItem: HabitStat): Boolean = oldItem == newItem
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: android.widget.TextView = itemView.findViewById(R.id.statHabitName)
        val subtitle: android.widget.TextView = itemView.findViewById(R.id.statSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_habit_stat, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val stat = getItem(position)
        holder.name.text = stat.name
        val pct = stat.completionRate.toPercentInt()
        holder.subtitle.text = "Completion rate: $pct% • Streak: ${stat.currentStreakDays} days"
    }
}
