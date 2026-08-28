package com.racebox.app.ui.tracking

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
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.racebox.app.R
import com.racebox.app.RaceBoxApp
import com.racebox.app.databinding.FragmentTrackingBinding
import com.racebox.app.domain.race.TrackingState
import com.racebox.app.tracking.TrackingService
import com.racebox.app.util.Formatters
import com.racebox.app.util.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TrackingFragment : Fragment() {

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TrackingViewModel by viewModels {
        ViewModelFactory {
            TrackingViewModel((requireActivity().application as RaceBoxApp).container.raceRepository)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()

        binding.btnBeginLap.setOnClickListener { viewModel.beginLap() }
        binding.btnEndLap.setOnClickListener { viewModel.endLap() }
        binding.btnStopRace.setOnClickListener { confirmStopRace() }
    }

    override fun onResume() {
        super.onResume()
        val container = (requireActivity().application as RaceBoxApp).container
        binding.tvGpsWarning.visibility =
            if (container.gpsTracker.isProviderEnabled()) View.GONE else View.VISIBLE
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state -> render(state) }
            }
        }
    }

    private fun render(state: TrackingState?) {
        val current = state ?: return
        binding.tvSpeed.text = Formatters.speed(current.speedKmh)
        binding.tvDistance.text = Formatters.distance(current.distanceKm)
        binding.tvTime.text = Formatters.duration(current.elapsedMillis)
        binding.tvMaxSpeed.text = Formatters.speed(current.maxSpeedKmh)
        if (current.lapNumber > 0) {
            binding.tvLapNumber.text = current.lapNumber.toString()
            binding.tvLapStatus.text = getString(R.string.lap_active, current.lapNumber)
        } else {
            binding.tvLapNumber.text = "-"
            binding.tvLapStatus.text = getString(R.string.lap_not_active)
        }
    }

    private fun confirmStopRace() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.stop_race_question)
            .setMessage(R.string.stop_race_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val container = (requireActivity().application as RaceBoxApp).container
                    container.raceRepository.stopRace()
                    TrackingService.stop(requireContext())
                    Toast.makeText(requireContext(), R.string.notification_race_saved_text, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_tracking_to_dashboard)
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}