package com.insane.eyewalk.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.insane.eyewalk.R
import com.insane.eyewalk.adapter.ViewPagerAdapter
import com.insane.eyewalk.database.room.AppDataBase
import com.insane.eyewalk.databinding.ActivityMainBinding
import com.insane.eyewalk.fragment.CameraFragment
import com.insane.eyewalk.fragment.ContactFragment
import com.insane.eyewalk.fragment.GuideFragment
import com.insane.eyewalk.fragment.SettingFragment
import com.insane.eyewalk.model.Token
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
        val token = intent.getStringExtra("token").toString()
        println(token)
        setUpTabs()
        loadUser(Token(accessToken = token, refreshToken = token))

    }

    private fun setUpTabs() {
        // SET UP ADAPTER
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(GuideFragment(), "Guia")
        adapter.addFragment(CameraFragment(), "Identificador")
        adapter.addFragment(ContactFragment(), "Contatos")
        adapter.addFragment(SettingFragment(), "")
        // SET UP VIEWPAGER ADAPTER
        binding.viewPager.adapter = adapter
        // SET UP TABS WITH VIEWPAGER
        binding.tabs.setupWithViewPager(binding.viewPager)
        binding.tabs.getTabAt(3)!!.setIcon(R.drawable.ic_setting)
    }

    private fun loadUser(token: Token) {
        lifecycleScope.launch {
            try {
                loader(true)
                println(token.accessToken)
                val user = UserService.getUser(token)
                if (user.isSuccessful) {
                    user.body()?.let {
                        db.userDao().truncateTable()
                        db.userDao().insert(it.toUserDTO())
                        loader(false)
                        println(it.contacts.size)
                        println(it.contacts[0])
                    }
                } else {
                    loader(false)
                    System.err.println("Erro ao carregar os dados do usuário")
                    finish()
                }
            } catch (e: Exception) {
                loader(false)
                System.err.println(e.message)
                finish()
            }
        }
    }

    private fun loader(display: Boolean) {
        if (display) binding.rlMainLoader.visibility = View.VISIBLE
        else binding.rlMainLoader.visibility = View.GONE
    }

}