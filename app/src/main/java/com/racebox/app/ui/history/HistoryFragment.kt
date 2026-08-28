package com.racebox.app.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.racebox.app.R
import com.racebox.app.RaceBoxApp
import com.racebox.app.databinding.FragmentHistoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val adapter = RaceAdapter { raceId ->
        val bundle = Bundle().apply { putLong(ARG_RACE_ID, raceId) }
        findNavController().navigate(R.id.action_history_to_summary, bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        val container = (requireActivity().application as RaceBoxApp).container
        val session = container.authRepository.currentUser()
        if (session == null) return

        viewLifecycleOwner.lifecycleScope.launch {
            container.raceRepository.racesForUser(session.userId).collectLatest { races ->
                adapter.setItems(races)
                binding.tvEmpty.visibility = if (races.isEmpty()) View.VISIBLE else View.GONE
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