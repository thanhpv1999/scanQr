package com.licious.sample.scannersample.ui.scanner.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    // Trạng thái đăng nhập
    private val _isLoggedIn = MutableLiveData(false)
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    // Hàm để cập nhật trạng thái đăng nhập
    fun login(username: String, password: String) {
        if (username == "admin" && password == "admin") {
            _isLoggedIn.value = true
        } else {
            _isLoggedIn.value = false
        }
    }

    // Hàm để đăng xuất
    fun logout() {
        _isLoggedIn.value = false
    }

    fun getLoginStatus(): Boolean {
        return _isLoggedIn.value ?: false
    }
}
