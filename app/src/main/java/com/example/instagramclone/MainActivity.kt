package com.example.instagramclone

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.instagramclone.Fragments.HomeFragment
import com.example.instagramclone.Fragments.SerachFragment
import com.example.instagramclone.Fragments.NotificationsFragment
import com.example.instagramclone.Fragments.ProfileFragment

class MainActivity : AppCompatActivity() {




    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
             fragmentMovement(HomeFragment())
                return@OnNavigationItemSelectedListener true

            }
            R.id.nav_search -> {
                fragmentMovement(SerachFragment())
                return@OnNavigationItemSelectedListener true

            }
            R.id.nav_add_post -> {

                item.isChecked=false
                startActivity(Intent(this@MainActivity,AddNewPostActivity::class.java))

                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_notifications -> {
                fragmentMovement(NotificationsFragment())
                return@OnNavigationItemSelectedListener true

            }
            R.id.nav_profile -> {
                fragmentMovement(ProfileFragment())
                return@OnNavigationItemSelectedListener true

            }
        }



        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val navView: BottomNavigationView = findViewById(R.id.nav_view)


        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        fragmentMovement(HomeFragment())
        




    }
    private fun fragmentMovement(fragment: Fragment)
    {

        val fragmentTrans=supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragment_container,fragment)
        fragmentTrans.commit()

    }
}