package com.insane.eyewalk.fragment

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.insane.eyewalk.activity.LoginActivity
import com.insane.eyewalk.database.room.AppDataBase
import com.insane.eyewalk.databinding.FragmentSettingBinding
import com.insane.eyewalk.service.RoomService
import com.insane.eyewalk.service.UserService
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
    private lateinit var db: AppDataBase
    private lateinit var roomService: RoomService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate layout
        binding = FragmentSettingBinding.inflate(inflater, container, false)

        db = AppDataBase.getDataBase(this.requireContext())
        roomService = RoomService(db)
        setUpViews()
        setUpCLickListeners()

        return binding.root
    }

    private fun setUpViews() {
        val user = roomService.getUser()
        val setting = roomService.getSetting()
        binding.tvName.text = user.name
        binding.tvEmail.text = user.email
        binding.tvPlan.text = user.planName
        binding.tvExpire.text = user.planEnd
        binding.switchVoice.isChecked = setting.switchVoice
        binding.switchRead.isChecked = setting.switchRead
        binding.switchMap.isChecked = setting.switchMap
        binding.tvLogout.paintFlags = binding.tvLogout.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun setUpCLickListeners() {
        // BUTTON LOGOUT
        binding.tvLogout.setOnClickListener { logout() }

        binding.switchVoice.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            roomService.getSetting().let {
                it.switchVoice = isChecked
                roomService.updateSetting(it)
            }
        })
        binding.switchRead.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            roomService.getSetting().let {
                it.switchRead = isChecked
                roomService.updateSetting(it)
            }
        })
        binding.switchMap.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            roomService.getSetting().let {
                it.switchMap = isChecked
                roomService.updateSetting(it)
            }
        })
    }

    private fun logout() {
        lifecycleScope.launch {
            loader(true)
            withTimeout(5_000) {
                UserService.logout(roomService.getToken().toToken())
            }
            loader(false)
            closeApp()
        }
    }

    private fun closeApp() {
        roomService.logoutUser()
        activity?.let{
            val intent = Intent (it, LoginActivity::class.java)
            it.startActivity(intent)
            it.finish()
        }
    }

    private fun loader(display: Boolean) {
        if (display) binding.rlSettingLoader.visibility = View.VISIBLE
        else binding.rlSettingLoader.visibility = View.GONE
    }

}