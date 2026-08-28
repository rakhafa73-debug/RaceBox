package com.racebox.app.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.racebox.app.R
import com.racebox.app.data.db.entity.Race
import com.racebox.app.databinding.ItemRaceBinding
import com.racebox.app.util.Formatters

class RaceAdapter(
    private val onClick: (Long) -> Unit
) : RecyclerView.Adapter<RaceAdapter.Holder>() {

    private val items = mutableListOf<Race>()

    fun setItems(newItems: List<Race>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemRaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position], onClick)
    }

    class Holder(
        private val binding: ItemRaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(race: Race, onClick: (Long) -> Unit) {
            val context = binding.root.context
            binding.tvDate.text = Formatters.date(race.startTime)
            val durationMillis = (race.endTime ?: race.startTime) - race.startTime
            binding.tvDuration.text = Formatters.duration(durationMillis)
            binding.tvDistance.text = context.getString(R.string.km_unit, Formatters.distance(race.totalDistanceKm))
            binding.tvAvg.text = context.getString(R.string.avg_speed_kmh_format, Formatters.speed(race.avgSpeedKmh))
            binding.root.setOnClickListener { onClick(race.id) }
        }
    }
}