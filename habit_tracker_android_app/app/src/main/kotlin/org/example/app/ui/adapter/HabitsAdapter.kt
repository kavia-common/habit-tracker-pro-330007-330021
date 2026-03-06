package org.example.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import org.example.app.R
import org.example.app.domain.model.Habit

class HabitsAdapter(
    private val onHabitClicked: (Habit) -> Unit,
    private val onToggleDoneToday: (Habit, Boolean) -> Unit,
    private val onDeleteHabit: (Habit) -> Unit
) : ListAdapter<Habit, HabitsAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean = oldItem == newItem
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: android.widget.TextView = itemView.findViewById(R.id.habitName)
        val desc: android.widget.TextView = itemView.findViewById(R.id.habitDescription)
        val streak: Chip = itemView.findViewById(R.id.streakChip)
        val done: MaterialCheckBox = itemView.findViewById(R.id.doneCheckbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val habit = getItem(position)

        holder.name.text = habit.name
        if (habit.description.isNullOrBlank()) {
            holder.desc.visibility = View.GONE
        } else {
            holder.desc.visibility = View.VISIBLE
            holder.desc.text = habit.description
        }

        holder.streak.text = "${habit.currentStreakDays}d"
        holder.done.setOnCheckedChangeListener(null)
        holder.done.isChecked = habit.doneToday
        holder.done.text = if (habit.doneToday) holder.itemView.context.getString(R.string.mark_undone)
        else holder.itemView.context.getString(R.string.mark_done)

        holder.itemView.setOnClickListener { onHabitClicked(habit) }

        holder.done.setOnCheckedChangeListener { _, isChecked ->
            onToggleDoneToday(habit, isChecked)
        }

        holder.itemView.setOnLongClickListener {
            onDeleteHabit(habit)
            true
        }
    }
}
