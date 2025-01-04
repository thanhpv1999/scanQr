package com.licious.sample.scannersample.ui.scanner

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.licious.sample.design.ui.base.BaseFragment
import com.licious.sample.scannersample.databinding.FragmentScannerBinding
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.licious.sample.scannersample.ui.scanner.viewmodels.ScannerViewModel
import com.licious.sample.scanner.ScannerViewState
import com.licious.sample.scannersample.R
import com.licious.sample.scannersample.ui.scanner.viewmodels.LoginViewModel
import com.licious.sample.scannersample.ui.scanner.viewmodels.MqttViewModel
import com.licious.sample.scannersample.ui.scanner.viewmodels.RealtimeDatabaseViewModel
import com.licious.sample.scannersample.ui.scanner.viewmodels.User
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 *  This Class will scan all qrcode and display it.
 */
@AndroidEntryPoint
class ScannerFragment : BaseFragment<FragmentScannerBinding>() {
    private val qrCodeViewModel: ScannerViewModel by viewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val realtimeDatabaseViewModel: RealtimeDatabaseViewModel by activityViewModels()
    private val mqttViewModel: MqttViewModel by activityViewModels()
    var textQr = "";


    private val vibrator: Vibrator by lazy {
        requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun getLogTag(): String = TAG

    override fun getViewBinding(): FragmentScannerBinding =
        FragmentScannerBinding.inflate(layoutInflater)

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        startAnimation()
        observeRealtimeDatabase()
        initMqtt()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initMqtt() {
        mqttViewModel.connect()

        mqttViewModel.isConnected.observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) {
                Toast.makeText(requireContext(), "MQTT Connected", Toast.LENGTH_SHORT).show()
                mqttViewModel.subscribe("${mqttViewModel.idHomeMqtt}/STATUS") // Thay topic theo nhu cầu
            } else {
                Toast.makeText(requireContext(), "MQTT Disconnected", Toast.LENGTH_SHORT).show()
            }
        }

        mqttViewModel.receivedMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), "Message received: $message", Toast.LENGTH_SHORT).show()
            val cleanMessage = message.trim()
            val parts = cleanMessage.split(":").map { it.trim() }

            if (":" in cleanMessage && parts.size == 2) {
                val username = parts[0]
                val email = parts[1]
                println("Username: $username, Email: $email")
                realtimeDatabaseViewModel.addUser(username, email)
            } else {
                println("Invalid message format: '$message'")
            }
        }
    }

    private fun observeRealtimeDatabase() {
        realtimeDatabaseViewModel.users.observe(viewLifecycleOwner) { users ->
            val sortedUsers = users?.sortedByDescending { it.timestamp } ?: emptyList()

            sortedUsers.forEach { user ->
                Log.d("SortedUsers", "Username: ${user.username}, Email: ${user.email}, Timestamp: ${user.timestamp}")
            }

            binding.recyclerViewDatabase.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = UserAdapter(sortedUsers)
            }
        }

        realtimeDatabaseViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onDestroyView() {
        vibrator.cancel()
        super.onDestroyView()
        mqttViewModel.disconnect()
    }

    fun generateRandomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    /**
     *  Initialise views and and handle click listeners here
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun initView() {
        loginViewModel.isLoggedIn.observe(viewLifecycleOwner, Observer { isLoggedIn ->
            if (!isLoggedIn) {
                Toast.makeText(requireContext(), "Invalid first", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.fragment_login)
            } else {
//                mqttViewModel.idHomeMqtt = "MQTT_2499"
                Toast.makeText(requireContext(), "done first", Toast.LENGTH_SHORT).show()
                qrCodeViewModel.startCamera(viewLifecycleOwner, requireContext(), binding.previewView, ::onResult);

                val toolbarView = layoutInflater.inflate(R.layout.layout_toolbar, binding.root as ViewGroup, false)
                (binding.root as ViewGroup).addView(toolbarView)
                val toolbar = toolbarView.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
                toolbar.setNavigationOnClickListener {
                    loginViewModel.logout()
                    findNavController().navigate(R.id.fragment_login)
                }
                val btnRight = toolbarView.findViewById<ImageButton>(R.id.btn_right)
                btnRight.setOnClickListener {
                    val topic = "${mqttViewModel.idHomeMqtt}/TEXTQR"
                    val message = generateRandomString(12)
                    textQr = message;
                    mqttViewModel.publish(topic, message)
                    Toast.makeText(requireContext(), "Right button clicked!", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * Success callback and error callback when barcode is successfully scanned. This method is also called while manually enter barcode
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun onResult(state: ScannerViewState, result: String?) {
        when(state)
        {
            ScannerViewState.Success -> {
                vibrateOnScan()
                Toast.makeText(requireContext(), "result=${result}", Toast.LENGTH_SHORT).show()
                val topic = "${mqttViewModel.idHomeMqtt}/REMOTE"
                if(textQr != ""){
                    if(textQr == result){
                        mqttViewModel.publish(topic, "remote: done")
                    }else{
                        mqttViewModel.publish(topic, "remote: fail")
                    }
                    textQr = ""
                }
            }
            ScannerViewState.Error -> {
                Toast.makeText(requireContext(), "error =${result}", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(requireContext(), "error =${result}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     *  Animation for the red bar.
     */
    private fun startAnimation() {
        val animation: Animation = AnimationUtils.loadAnimation(context, com.licious.sample.scanner.R.anim.barcode_animator)
        binding.llAnimation.startAnimation(animation)
    }

    /**
     *  Vibration mobile on Scan successful.
     */
    private fun vibrateOnScan() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        VIBRATE_DURATION,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(VIBRATE_DURATION)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "QrCodeReaderFragment"
        private const val VIBRATE_DURATION = 200L
    }
}

class UserAdapter(private val users: List<User>) : RecyclerView.Adapter<UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size
}

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
    private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
    private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)

    fun bind(user: User) {
        tvUsername.text = user.username
        tvEmail.text = user.email

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(user.timestamp))
        tvTimestamp.text = formattedDate
    }
}