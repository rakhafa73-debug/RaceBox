package com.racebox.app.ui.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.racebox.app.R
import com.racebox.app.RaceBoxApp
import com.racebox.app.data.sync.SyncResult
import com.racebox.app.databinding.FragmentSummaryBinding
import com.racebox.app.util.Formatters
import com.racebox.app.util.ViewModelFactory
import kotlinx.coroutines.launch

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    private val raceId by lazy { requireArguments().getLong(ARG_RACE_ID) }

    private val viewModel: SummaryViewModel by viewModels {
        ViewModelFactory {
            val container = (requireActivity().application as RaceBoxApp).container
            SummaryViewModel(container.raceRepository, container.crypto)
        }
    }

    private val lapAdapter = LapAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvLaps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLaps.adapter = lapAdapter

        binding.btnExportCsv.setOnClickListener { export { it.exportCsv(raceId) } }
        binding.btnExportJson.setOnClickListener { export { it.exportJson(raceId) } }
        binding.btnShare.setOnClickListener { share() }
        binding.btnSyncNow.setOnClickListener { syncNow() }

        observeUi()
        viewModel.load(raceId)
    }

    private fun observeUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ui.collect { ui -> render(ui) }
            }
        }
    }

    private fun render(ui: SummaryUiState?) {
        val state = ui ?: return
        binding.tvDate.text = Formatters.date(state.startTime)
        binding.tvSyncStatus.text = getString(
            if (state.isSynced) R.string.synced else R.string.not_synced
        )
        binding.tvDistance.text = getString(R.string.km_unit, Formatters.distance(state.distanceKm))
        binding.tvAvgSpeed.text = Formatters.speed(state.avgSpeedKmh)
        binding.tvMaxSpeed.text = Formatters.speed(state.maxSpeedKmh)
        binding.tvDuration.text = Formatters.duration(state.durationMillis)
        binding.tvLapsCount.text = getString(R.string.laps_count_format, state.lapCount)
        binding.speedChart.data = state.speedSeries
        binding.trackMap.data = state.heatPoints
        lapAdapter.setItems(state.laps)
    }

    private fun export(block: suspend (com.racebox.app.data.export.RaceExporter) -> android.net.Uri?) {
        viewLifecycleOwner.lifecycleScope.launch {
            val container = (requireActivity().application as RaceBoxApp).container
            val uri = block(container.raceExporter)
            if (uri != null) {
                container.raceExporter.share(requireContext(), uri)
            } else {
                Toast.makeText(requireContext(), R.string.no_chart_data, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun share() {
        viewLifecycleOwner.lifecycleScope.launch {
            val container = (requireActivity().application as RaceBoxApp).container
            val uri = container.raceExporter.exportJson(raceId)
            if (uri != null) {
                container.raceExporter.share(requireContext(), uri)
            }
        }
    }

    private fun syncNow() {
        viewLifecycleOwner.lifecycleScope.launch {
            val container = (requireActivity().application as RaceBoxApp).container
            binding.tvSyncStatus.setText(R.string.syncing)
            val result = container.syncRepository.syncNow()
            val message = when (result) {
                is SyncResult.Success -> getString(R.string.sync_success_format, result.raceCount)
                SyncResult.NoData -> getString(R.string.nothing_to_sync)
                SyncResult.Skipped -> getString(R.string.syncing)
                is SyncResult.Error -> result.message
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            if (result is SyncResult.Success) {
                viewModel.reload(raceId)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_RACE_ID = "raceId"
    }
}