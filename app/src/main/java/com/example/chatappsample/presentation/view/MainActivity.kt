package com.example.chatappsample.presentation.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.example.chatappsample.R
import com.example.chatappsample.databinding.ActivityMainBinding
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.domain.repository.UserRepository
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var userRepo: UserRepository

    val viewModel: UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }
    private lateinit var mBinding: ActivityMainBinding
    private var currentUserDomain: UserDomain? = null
    private val userListFragment = UserListFragment()
    private val chatroomListFragment = ChatroomListFragment()
    private val mypageFragment = MypageFragment()
    private val mainPagerAdapter = MainPagerAdapter(this)

    private val mainTabItemList = listOf(
        MainTabItem(userListFragment, "유저 목록", R.drawable.ic_baseline_people_24),
        MainTabItem(chatroomListFragment, "채팅 목록", R.drawable.ic_baseline_chat_bubble_24),
        MainTabItem(mypageFragment, "마이페이지", R.drawable.ic_baseline_person_24)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.IO) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.setCurrentUserIdAndFetchChatroomList(intent.getStringExtra(CURRENT_USER)!!, this)
            }
        }
        viewModel.fetchAllUsersList()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.viewModel = this@MainActivity.viewModel
        mBinding.lifecycleOwner = this@MainActivity

        viewModel.currentUserDomain.observe(this) {
            if (it != null && currentUserDomain?.profileImage != it.profileImage) {
                viewModel.downloadProfileImage(it, object: OnFileDownloadListener {

                    override fun onSuccess(byteArray: ByteArray) {
                        mypageFragment.setOnGetUserProfile(object: MypageFragment.OnGetUserProfileListener {
                            override fun setOnGetUserProfileListener(imageView: ShapeableImageView) {
                                Glide
                                    .with(this@MainActivity)
                                    .load(byteArray)
                                    .into(imageView)
                            }
                        })
                    }

                    override fun onFailure(e: Exception) {

                    }
                })
            }

            if (it != null) currentUserDomain = it
        }

        mBinding.viewModel!!.fetchCurrentUserInformation()
        mBinding.viewModel!!.fetchUserListFromExternalDB()

        mBinding.viewpager2Main.adapter = mainPagerAdapter

        TabLayoutMediator(mBinding.tablayoutMainTablayout, mBinding.viewpager2Main, TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            tab.text = mainTabItemList[position].tabString
            tab.icon = AppCompatResources.getDrawable(this, mainTabItemList[position].tabIconDrawableId)
        }).attach()
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
                val intent = Intent(this, SignInActivity::class.java).apply {
                    putExtra("AutoLogin", false)
                }
                startActivity(intent)
                return true
            }
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    inner class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = mainTabItemList.size

        override fun createFragment(position: Int): Fragment = mainTabItemList[position].fragment
    }

    /**
     * data class including components needed to present tab & viewpager2 in this activity.
     *
     * @property fragment Fragment to inflate by selecting tab
     * @property tabString Tab Title string
     * @property tabIconDrawableId  Tab Icon drawable id
     */
    data class MainTabItem(val fragment: Fragment, val tabString: String, val tabIconDrawableId: Int)

    companion object {
        const val CURRENT_USER = "current_user"
    }
}