package com.example.chatappsample.presentation.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappsample.R
import com.example.chatappsample.databinding.ActivityMainBinding
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.presentation.view.adapter.MainUserAdapter
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    val viewModel: UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }
    private lateinit var mBinding: ActivityMainBinding

    private var userList = arrayListOf<User>()
    private var currentUserId = ""
    private lateinit var rvAdapter: MainUserAdapter

    private val rvUserList by lazy { this.findViewById<RecyclerView>(R.id.rv_main_user_recyclerview) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Viewmodel Databinding & Lifecycle Setting
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.viewModel = this@MainActivity.viewModel
        mBinding.lifecycleOwner = this@MainActivity

        // Initialize recyclerview
        viewModel.getCurrentUser()
        viewModel.getAllUsers()

        viewModel.currentUser.observe(this) {
            rvAdapter.currentUserId = it!!.uid
        }
        rvAdapter = MainUserAdapter(ctx = this@MainActivity, currentUserId = currentUserId, userList = userList)
        rvUserList.adapter = rvAdapter
        viewModel.allUsers.observe(this) {
            rvAdapter.userList = it
            rvAdapter.notifyDataSetChanged()
        }
        rvUserList.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_log_out -> {
                viewModel.signOut()
                finish()
                val intent = Intent(this, LogInActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }
}