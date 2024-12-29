package com.licious.sample.scannersample.ui.scanner

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
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
import com.licious.sample.scannersample.ui.scanner.viewmodels.RealtimeDatabaseViewModel
import com.licious.sample.scannersample.ui.scanner.viewmodels.User
import dagger.hilt.android.AndroidEntryPoint

/**
 *  This Class will scan all qrcode and display it.
 */
@AndroidEntryPoint
class ScannerFragment : BaseFragment<FragmentScannerBinding>() {
    private val qrCodeViewModel: ScannerViewModel by viewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val realtimeDatabaseViewModel: RealtimeDatabaseViewModel by activityViewModels()

    private val vibrator: Vibrator by lazy {
        requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun getLogTag(): String = TAG

    override fun getViewBinding(): FragmentScannerBinding =
        FragmentScannerBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        startAnimation()
        observeRealtimeDatabase()
    }

    private fun observeRealtimeDatabase() {
        realtimeDatabaseViewModel.users.observe(viewLifecycleOwner) { users ->
            binding.recyclerViewDatabase.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = UserAdapter(users ?: emptyList()) // Gắn Adapter ngay cả khi danh sách rỗng
            }
        }

        realtimeDatabaseViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        vibrator.cancel()
        super.onDestroyView()
    }

    /**
     *  Initialise views and and handle click listeners here
     */
    private fun initView() {
        loginViewModel.isLoggedIn.observe(viewLifecycleOwner, Observer { isLoggedIn ->
            if (!isLoggedIn) {
                Toast.makeText(requireContext(), "Invalid first", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.fragment_login)
            } else {
                Toast.makeText(requireContext(), "done first", Toast.LENGTH_SHORT).show()
                qrCodeViewModel.startCamera(viewLifecycleOwner, requireContext(), binding.previewView, ::onResult);
                realtimeDatabaseViewModel.addUser("thanh", "van")

                val toolbarView = layoutInflater.inflate(R.layout.layout_toolbar, binding.root as ViewGroup, false)
                (binding.root as ViewGroup).addView(toolbarView)
                val toolbar = toolbarView.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
                toolbar.setNavigationOnClickListener {
                    loginViewModel.logout()
                    findNavController().navigate(R.id.fragment_login)
                }
            }
        })
    }

    /**
     * Success callback and error callback when barcode is successfully scanned. This method is also called while manually enter barcode
     */
    private fun onResult(state: ScannerViewState, result: String?) {
        when(state)
        {
            ScannerViewState.Success -> {
                vibrateOnScan()
                Toast.makeText(requireContext(), "result=${result}", Toast.LENGTH_SHORT).show()
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
        tvTimestamp.text = System.currentTimeMillis().toString()
    }
}