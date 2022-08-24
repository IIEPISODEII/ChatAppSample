package com.example.chatappsample.presentation.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.example.chatappsample.R
import com.example.chatappsample.databinding.ActivityMainBinding
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    val viewModel: UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }
    private lateinit var mBinding: ActivityMainBinding
    private var currentUser: User? = null
    private val userListFragment = UserListFragment()
    private val mypageFragment = MypageFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // Initialize Viewmodel Databinding & Lifecycle Setting
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.viewModel = this@MainActivity.viewModel
        mBinding.lifecycleOwner = this@MainActivity

        mBinding.vpager2Main.adapter = PagerAdapter(this)
        viewModel.currentUser.observe(this) {
            if (currentUser?.profileImage != it?.profileImage) {
                viewModel.downloadProfileImage(it!!, object: OnFileDownloadListener {

                    override fun onSuccess(uri: Uri) {
                        mypageFragment.setOnGetUserProfile(object: MypageFragment.OnGetUserProfileListener {
                            override fun setOnGetUserProfileListener(imageView: ShapeableImageView) {
                                Glide
                                    .with(this@MainActivity)
                                    .load(uri)
                                    .into(imageView)
                            }
                        })
                    }

                    override fun onFailure(e: Exception) {

                    }
                })
            }

            currentUser = it!!
        }

        mBinding.bottomnaviMainNavigation.setOnItemSelectedListener(object: NavigationBarView.OnItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.menu_main_chatting_list -> {
                        mBinding.vpager2Main.currentItem = 0

                        return true
                    }
                    else -> {
                        mBinding.vpager2Main.currentItem = 1
                        return true
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_log_out -> {
                viewModel.signOut()
                viewModel.cancelAutoLogin()
                finish()
                val intent = Intent(this, LogInActivity::class.java).apply {
                    putExtra("AutoLogin", false)
                }
                startActivity(intent)
                return true
            }
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    inner class PagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position%itemCount) {
                0 -> { userListFragment }
                else -> { mypageFragment }
            }
        }

    }
}