package com.example.chatappsample.presentation.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappsample.Application
import com.example.chatappsample.R
import com.example.chatappsample.databinding.ActivityMainBinding
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.presentation.view.adapter.MainUserAdapter
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {

    lateinit var viewModel: UserViewModel
    private lateinit var mBinding: ActivityMainBinding

    private lateinit var userList: ArrayList<User>
    private lateinit var rvAdapter: MainUserAdapter

    private val rvUserList by lazy { this.findViewById<RecyclerView>(R.id.rv_main_user_recyclerview) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Viewmodel Databinding & Lifecycle Setting
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.viewModel = this@MainActivity.viewModel
        mBinding.lifecycleOwner = this@MainActivity


        // Initialize recyclerview
        userList = ArrayList<User>()
        userList = viewModel.getAllUsers {
            rvAdapter.notifyDataSetChanged()
        }
        rvAdapter = MainUserAdapter(ctx = this@MainActivity, userList = userList)
        rvUserList.adapter = rvAdapter
        rvUserList.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,  menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_log_out -> {
                viewModel.signOut {  }
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