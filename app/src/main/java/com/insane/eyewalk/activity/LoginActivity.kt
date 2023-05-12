package com.insane.eyewalk.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import com.insane.eyewalk.database.dto.SettingDTO
import com.insane.eyewalk.database.room.AppDataBase
import com.insane.eyewalk.databinding.ActivityLoginBinding
import com.insane.eyewalk.model.Token
import com.insane.eyewalk.model.User
import com.insane.eyewalk.model.input.UserAuthentication
import com.insane.eyewalk.service.TokenService
import com.insane.eyewalk.service.UserService
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
        checkExistingUser()
        setUpClickListeners()
    }

    private fun setUpInitialData() {
        val setting = db.settingDao().getAll()
        if (setting.isEmpty()) {
            db.settingDao().insert(SettingDTO(1, switchVoice = true, switchRead = true))
        }
    }

    private fun checkExistingUser() {
        val users = db.userDao().getAll()
        if (users.isNotEmpty()) {
            binding.etEmailLogin.setText(users[0].email)
            val tokens = db.tokenDao().getAll()
            if (tokens.isNotEmpty()) {
                refreshToken(tokens[0].refreshToken)
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
                }  else Tools.Show.message(this, "Senha muito curta")
            } else Tools.Show.message(this, "Email inválido!")
        }
    }

    private fun refreshToken(refreshToken: String) {
        lifecycleScope.launch {
            try {
                loader(true)
                val token = TokenService.refreshToken(refreshToken)
                if (token.isSuccessful) {
                    token.body()?.let {
                        println(it.accessToken)
                        println(it.refreshToken)
                        db.tokenDao().truncateTable()
                        db.tokenDao().insert(token = it.toTokenDTO())
                        loader(false)
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
                        println(it.accessToken)
                        println(it.refreshToken)
                        db.tokenDao().truncateTable()
                        db.tokenDao().insert(token = it.toTokenDTO())
                        loader(false)
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
        this.finish();
    }

    private fun notAuthenticated() {
        Tools.Show.message(this, "Login ou senha inválido")
    }

    private fun errorAuthenticating() {
        Tools.Show.message(this, "Algo deu errado tente novamente")
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun loader(display: Boolean) {
        if (display) binding.rlLoginLoader.visibility = View.VISIBLE
        else binding.rlLoginLoader.visibility = View.GONE
    }

//      refreshToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBlbWFpbC5jb20iLCJpYXQiOjE2ODM3NDUyMzMsImV4cCI6MTY4MzgzMTYzM30.DyzYKpQfIqt9H8A2AUvldfBzFfwE8zu-A3VLmXdYNFk")
//        "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBlbWFpbC5jb20iLCJpYXQiOjE2ODM4Mjk0MzAsImV4cCI6MTY4MzkxNTgzMH0.KiSzeP9JI4JtF5THWgTHQVcP6NnlK8BcD5wan_Rt2lo",
//        "refresh_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBlbWFpbC5jb20iLCJpYXQiOjE2ODM4Mjk0MzAsImV4cCI6MTY4NDQzNDIzMH0.kHAWZ25yadpfZdAzJivP2mulAPLjPUUbcZgve8_KI7o"

}