package com.licious.sample.scannersample.ui.scanner.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*

class RealtimeDatabaseViewModel : ViewModel() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://scannerqr-638d6-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val usersReference: DatabaseReference = database.getReference("users")

    // LiveData để lưu danh sách người dùng
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    // LiveData để theo dõi lỗi (nếu có)
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    init {
        // Lắng nghe sự thay đổi trong Realtime Database
        usersReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let { userList.add(it) }
                }
                _users.value = userList
            }

            override fun onCancelled(error: DatabaseError) {
                _error.value = error.message
            }
        })
    }

    // Hàm để thêm người dùng mới
    fun addUser(username: String, email: String) {
        val newUser = mapOf(
            "username" to username,
            "email" to email,
            "timestamp" to ServerValue.TIMESTAMP
        )
        usersReference.push().setValue(newUser)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    _error.value = task.exception?.message ?: "Failed to add user"
                }
            }
    }

    // Hàm để xóa người dùng theo ID
    fun deleteUser(userId: String) {
        usersReference.child(userId).removeValue()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    _error.value = task.exception?.message ?: "Failed to delete user"
                }
            }
    }
}

// Data class đại diện cho User
data class User(
    val username: String = "",
    val email: String = "",
    val timestamp: Long = 0L
)
