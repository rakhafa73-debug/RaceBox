package com.racebox.app.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.racebox.app.R
import com.racebox.app.RaceBoxApp
import com.racebox.app.data.prefs.UserSession
import com.racebox.app.data.sync.SyncResult
import com.racebox.app.databinding.FragmentDashboardBinding
import com.racebox.app.di.AppContainer
import com.racebox.app.tracking.TrackingService
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val container = (requireActivity().application as RaceBoxApp).container
        val session = container.authRepository.currentUser()

        binding.tvWelcome.text = getString(R.string.dashboard_welcome, session?.username.orEmpty())
        updateStatus(container)

        binding.btnSyncNow.setOnClickListener { syncNow(container) }
        binding.btnStartRace.setOnClickListener { startRace(container, session) }
        binding.btnHistory.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_history)
        }
        binding.btnLogout.setOnClickListener {
            container.authRepository.logout()
            findNavController().popBackStack(R.id.loginFragment, false)
        }
    }

    override fun onResume() {
        super.onResume()
        val container = (requireActivity().application as RaceBoxApp).container
        updateStatus(container)
        binding.tvGpsWarning.visibility =
            if (container.gpsTracker.isProviderEnabled()) View.GONE else View.VISIBLE
    }

    private fun updateStatus(container: AppContainer) {
        binding.tvStatus.setText(
            if (container.raceRepository.isTracking()) R.string.status_recording else R.string.status_idle
        )
    }

    private fun startRace(container: AppContainer, session: UserSession?) {
        if (session == null) return
        if (!container.gpsTracker.isProviderEnabled()) {
            Toast.makeText(requireContext(), R.string.gps_warning, Toast.LENGTH_LONG).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val started = container.raceRepository.startRace(session.userId)
            if (started) {
                TrackingService.start(requireContext())
                updateStatus(container)
                findNavController().navigate(R.id.action_dashboard_to_tracking)
            }
        }
    }

    private fun syncNow(container: AppContainer) {
        viewLifecycleOwner.lifecycleScope.launch {
            val message = when (val result = container.syncRepository.syncNow()) {
                is SyncResult.Success -> getString(R.string.sync_success)
                SyncResult.NoData -> getString(R.string.nothing_to_sync)
                SyncResult.Skipped -> getString(R.string.syncing)
                is SyncResult.Error -> result.message
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}