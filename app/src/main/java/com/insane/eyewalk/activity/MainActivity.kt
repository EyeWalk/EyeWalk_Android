package com.insane.eyewalk.activity

import android.graphics.Camera
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.insane.eyewalk.R
import com.insane.eyewalk.adapter.ViewPagerAdapter
import com.insane.eyewalk.database.room.AppDataBase
import com.insane.eyewalk.databinding.ActivityMainBinding
import com.insane.eyewalk.fragment.CameraFragment
import com.insane.eyewalk.fragment.ContactFragment
import com.insane.eyewalk.fragment.GuideFragment
import com.insane.eyewalk.fragment.SettingFragment
import com.insane.eyewalk.model.Token
import com.insane.eyewalk.service.RoomService
import com.insane.eyewalk.service.UserService
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDataBase.getDataBase(this)

        intent.getStringExtra("token").toString().let {
            loadUser(Token(it,it))
        }
        setUpTabs()

    }

    private fun setUpTabs() {
        // SET UP ADAPTER
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(GuideFragment(), resources.getString(R.string.tabOneTitle))
        adapter.addFragment(CameraFragment(), resources.getString(R.string.tabTwoTitle))
        adapter.addFragment(ContactFragment(), resources.getString(R.string.tabThreeTitle))
        adapter.addFragment(SettingFragment(), resources.getString(R.string.tabFourTitle))
        // SET UP VIEWPAGER ADAPTER
        binding.viewPager.adapter = adapter
        // SET UP TABS WITH VIEWPAGER
        binding.tabs.setupWithViewPager(binding.viewPager)
        binding.tabs.getTabAt(3)!!.setIcon(R.drawable.ic_setting)

        val vPager = binding.viewPager

        vPager.addOnPageChangeListener(object : OnPageChangeListener {
            // This method will be invoked when a new page becomes selected.
            override fun onPageSelected(position: Int) {
                if (position == 1) {
                }
            }

            // This method will be invoked when the current page is scrolled
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            override fun onPageScrollStateChanged(state: Int) {
                // Code goes here
            }
        })
    }

    private fun loadUser(token: Token) {
        lifecycleScope.launch {
            try {
                val user = UserService.getUser(token)
                if (user.isSuccessful) {
                    user.body()?.let {
                        RoomService(db).updateUser(it.toUserDTO())
                    }
                } else {
                    System.err.println("Erro ao carregar os dados do usuário")
                    finish()
                }
            } catch (e: Exception) {
                System.err.println(e.message)
                finish()
            }
        }
    }

}