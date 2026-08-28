package com.racebox.app.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.racebox.app.R
import com.racebox.app.RaceBoxApp
import com.racebox.app.databinding.FragmentLoginBinding
import com.racebox.app.repository.AuthResult
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text?.toString().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()
            login(username, password)
        }
    }

    private fun login(username: String, password: String) {
        binding.btnLogin.isEnabled = false
        binding.tvMessage.visibility = View.GONE
        val container = (requireActivity().application as RaceBoxApp).container
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = container.authRepository.login(username, password)) {
                is AuthResult.Success -> {
                    findNavController().navigate(R.id.action_login_to_dashboard)
                }
                is AuthResult.Error -> {
                    binding.tvMessage.text = result.message
                    binding.tvMessage.visibility = View.VISIBLE
                }
            }
            binding.btnLogin.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}