package com.licious.sample.scannersample.ui.scanner.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _isLoggedIn.value = firebaseAuth.currentUser != null
    }

    init {
        auth.addAuthStateListener(authStateListener)
        _isLoggedIn.value = auth.currentUser != null
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Firebase tự động kích hoạt AuthStateListener, không cần cập nhật _isLoggedIn ở đây
                } else {
                    _isLoggedIn.value = false
                }
            }
    }

    fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Firebase tự động kích hoạt AuthStateListener, không cần cập nhật _isLoggedIn ở đây
                } else {
                    _isLoggedIn.value = false
                }
            }
    }

    fun logout() {
        auth.signOut()
        // Firebase tự động kích hoạt AuthStateListener, không cần cập nhật _isLoggedIn ở đây
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}
