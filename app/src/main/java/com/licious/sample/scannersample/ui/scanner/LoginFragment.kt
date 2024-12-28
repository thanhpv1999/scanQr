package com.licious.sample.scannersample.ui.scanner

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.licious.sample.design.ui.base.BaseFragment
import com.licious.sample.scannersample.R
import com.licious.sample.scannersample.databinding.FragmentLoginBinding
import com.licious.sample.scannersample.ui.scanner.viewmodels.LoginViewModel

class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    private val loginViewModel: LoginViewModel by viewModels()
    override fun getLogTag(): String = "LoginFragment"

    override fun getViewBinding(): FragmentLoginBinding =
        FragmentLoginBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel.logout()
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            // Lắng nghe trạng thái đăng nhập
            loginViewModel.isLoggedIn.observe(viewLifecycleOwner, Observer { isLoggedIn ->
                if (isLoggedIn) {
                    // Điều hướng tới ScannerFragment khi đăng nhập thành công
                    findNavController().navigate(R.id.fragment_scanner)
                } else {
                    Toast.makeText(requireContext(), "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            })

            // Xử lý khi nhấn nút Login
            binding.btnLogin.setOnClickListener {
                val username = binding.etUsername.text.toString()
                val password = binding.etPassword.text.toString()
                loginViewModel.login(username, password)
            }
        }
    }
}
