package com.example.chatappsample.presentation.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappsample.Application
import com.example.chatappsample.R
import com.example.chatappsample.model.User
import com.example.chatappsample.presentation.view.adapter.MainUserAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MainActivity: AppCompatActivity() {

    private lateinit var userList: ArrayList<User>
    private lateinit var rvAdapter: MainUserAdapter

    private val rvUserList by lazy { this.findViewById<RecyclerView>(R.id.rv_main_user_recyclerview) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userList = ArrayList<User>()
        Application.mFbDatabaseRef.child("user").addValueEventListener(object: ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                for (postSnapshot in snapshot.children) {
                    val currentUser = postSnapshot.getValue(User::class.java)
                    if (Application.mFirebaseAuth.currentUser?.uid != currentUser?.uid) userList.add(currentUser!!)
                }
                rvAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
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
                Application.mFirebaseAuth.signOut()
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