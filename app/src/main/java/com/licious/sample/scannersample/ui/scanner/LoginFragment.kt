package com.licious.sample.scannersample.ui.scanner

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.licious.sample.design.ui.base.BaseFragment
import com.licious.sample.scannersample.R
import com.licious.sample.scannersample.databinding.FragmentLoginBinding
import com.licious.sample.scannersample.ui.scanner.viewmodels.LoginViewModel
import com.licious.sample.scannersample.ui.scanner.viewmodels.MqttViewModel

class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val mqttViewModel: MqttViewModel by activityViewModels()

    override fun getLogTag(): String = "LoginFragment"
    val idHome = ""

    override fun getViewBinding(): FragmentLoginBinding =
        FragmentLoginBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val idHome =""
        mqttViewModel.idHomeMqtt = idHome
        mqttViewModel.user = ""
        loginViewModel.logout()
        binding.btnLogin.setOnClickListener {
            val email = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            val idHome = binding.etIdHome.text.toString()

            if (email.isEmpty() || password.isEmpty() || idHome.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginViewModel.login(email, password)

            // Lắng nghe trạng thái đăng nhập
            loginViewModel.isLoggedIn.observe(viewLifecycleOwner, Observer { isLoggedIn ->
                if (isLoggedIn) {
                    Toast.makeText(requireContext(), "login done", Toast.LENGTH_SHORT).show()
                    mqttViewModel.idHomeMqtt = "MQTT_$idHome"
                    mqttViewModel.user = email.split("@")[0]
                    findNavController().navigate(R.id.fragment_scanner)
                } else {
                    Toast.makeText(requireContext(), "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
