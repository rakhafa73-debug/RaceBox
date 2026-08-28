package com.racebox.app.ui.summary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.racebox.app.databinding.ItemLapBinding
import com.racebox.app.util.Formatters
import com.racebox.app.R

class LapAdapter : RecyclerView.Adapter<LapAdapter.Holder>() {

    private val items = mutableListOf<LapSummary>()

    fun setItems(newItems: List<LapSummary>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemLapBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    class Holder(
        private val binding: ItemLapBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(summary: LapSummary) {
            val context = binding.root.context
            binding.tvLap.text = context.getString(R.string.lap_short, summary.number)
            binding.tvLapDistance.text = context.getString(R.string.km_unit, Formatters.distance(summary.distanceKm))
            binding.tvLapAvg.text = context.getString(R.string.avg_speed_kmh_format, Formatters.speed(summary.avgSpeedKmh))
            binding.tvLapMax.text = Formatters.speed(summary.maxSpeedKmh)
            binding.tvLapDuration.text = Formatters.duration(summary.durationMillis)
        }
    }
}