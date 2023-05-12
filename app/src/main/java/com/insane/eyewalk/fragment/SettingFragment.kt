package com.insane.eyewalk.fragment

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import com.insane.eyewalk.activity.LoginActivity
import com.insane.eyewalk.database.room.AppDataBase
import com.insane.eyewalk.databinding.FragmentSettingBinding


class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
    private lateinit var db: AppDataBase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate layout
        binding = FragmentSettingBinding.inflate(inflater, container, false)

        db = AppDataBase.getDataBase(this.requireContext())
        setUpViews()
        setUpCLickListeners()

        return binding.root
    }

    private fun setUpViews() {
        val user = db.userDao().getUser()
        binding.tvName.text = user.name
        binding.tvEmail.text = user.email
        binding.tvPlan.text = user.planName
        binding.tvExpire.text = user.planEnd
        binding.switchVoice.isChecked = db.settingDao().getSetting().switchVoice
        binding.switchRead.isChecked = db.settingDao().getSetting().switchRead
        binding.tvLogout.paintFlags = binding.tvLogout.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun setUpCLickListeners() {
        // BUTTON LOGOUT
        binding.tvLogout.setOnClickListener { logout() }
        val setting = db.settingDao().getSetting()

        binding.switchVoice.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            setting.switchVoice = isChecked
            db.settingDao().truncateTable()
            db.settingDao().insert(setting)
        })
        binding.switchRead.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            setting.switchRead = isChecked
            db.settingDao().truncateTable()
            db.settingDao().insert(setting)
        })
    }

    private fun logout() {
        db.tokenDao().truncateTable()
        activity?.let{
            val intent = Intent (it, LoginActivity::class.java)
            it.startActivity(intent)
            this.requireActivity().finish()
        }
    }

}