package com.insane.eyewalk.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.insane.eyewalk.R
import com.insane.eyewalk.config.Constants
import com.insane.eyewalk.database.dto.SettingDTO
import com.insane.eyewalk.database.room.AppDataBase
import com.insane.eyewalk.databinding.ActivityLoginBinding
import com.insane.eyewalk.model.Token
import com.insane.eyewalk.model.input.UserAuthentication
import com.insane.eyewalk.service.RoomService
import com.insane.eyewalk.service.TokenService
import com.insane.eyewalk.utils.Tools
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: AppDataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDataBase.getDataBase(this)

        setUpInitialData()
        setUpClickListeners()

        if (checkPermissions()) checkExistingUser()
    }

    private fun checkPermissions(): Boolean {
        var permission = 0
        if (mapPermissionGranted()) {
            println("Map permission granted!")
            permission++
        } else {
            ActivityCompat.requestPermissions(
                this, Constants.MAP_REQUIRED_PERMISSIONS, Constants.MAP_REQUEST_CODE_PERMISSION)
        }
        if (cameraPermissionGranted()) {
            println("Camera permission granted!")
            permission++
        } else {
            ActivityCompat.requestPermissions(
                this, Constants.CAMERA_REQUIRED_PERMISSIONS, Constants.CAMERA_REQUEST_CODE_PERMISSION)
        }
        return permission == 2
    }

    private fun mapPermissionGranted() = Constants.MAP_REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun cameraPermissionGranted() = Constants.CAMERA_REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun setUpInitialData() {
        val setting = db.settingDao().getAll()
        if (setting.isEmpty()) {
            db.settingDao().insert(SettingDTO(1, switchVoice = true, switchRead = true, switchMap = true))
        }
    }

    private fun checkExistingUser() {
        RoomService(db).let { room ->
            if (room.existUser()) {
                room.getUser().let { user -> binding.etEmailLogin.setText(user.email) }
                if (room.existToken()) { autoLogin(room.getToken().refreshToken) }
            }
        }
    }

    private fun setUpClickListeners() {
        // BUTTON LOGIN
        binding.btnLogin.setOnClickListener {
            hideKeyboard(binding.root)
            val email = binding.etEmailLogin.text.toString()
            val password = binding.etPasswordLogin.text.toString()
            if (email.length >= 5) {
                if (password.length >= 5) {
                    login(email, password)
                }  else Tools.Show.message(this, resources.getString(R.string.passTooShort))
            } else Tools.Show.message(this, resources.getString(R.string.invalidEmail))
        }
    }

    private fun autoLogin(refreshToken: String) {
        lifecycleScope.launch {
            try {
                loader(true)
                val token = TokenService.refreshToken(refreshToken)
                if (token.isSuccessful) {
                    token.body()?.let {
                        RoomService(db).updateToken(it.toTokenDTO())
                        startApp(it)
                    }
                } else {
                    loader(false)
                }
            } catch (e: Exception) {
                loader(false)
            }
        }
    }

    private fun login(email: String, password: String) {
        lifecycleScope.launch {
            try {
                loader(true)
                val token = TokenService.getToken(UserAuthentication(email, password))
                if (token.isSuccessful) {
                    token.body()?.let {
                        RoomService(db).updateToken(it.toTokenDTO())
                        startApp(it)
                    }
                } else {
                    loader(false)
                    notAuthenticated()
                }
            } catch (e: Exception) {
                loader(false)
                errorAuthenticating()
            }
        }
    }

    private fun startApp(token: Token) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("token", token.accessToken)
        startActivity(intent)
        this.finish()
    }

    private fun notAuthenticated() {
        Tools.Show.message(this, resources.getString(R.string.notAuthenticated))
    }

    private fun errorAuthenticating() {
        Tools.Show.message(this, resources.getString(R.string.somethingWrong))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun loader(display: Boolean) {
        if (display) binding.rlLoginLoader.visibility = View.VISIBLE
        else binding.rlLoginLoader.visibility = View.GONE
    }

}