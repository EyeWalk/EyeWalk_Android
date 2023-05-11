package com.insane.eyewalk.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import com.insane.eyewalk.databinding.ActivityLoginBinding
import com.insane.eyewalk.model.input.UserAuthentication
import com.insane.eyewalk.service.TokenService
import com.insane.eyewalk.utils.Tools
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    private fun login(email: String, password: String) {
        lifecycleScope.launch {
            try {
                binding.rlLoginLoader.visibility = View.VISIBLE
                val token = TokenService.getToken(UserAuthentication(email, password))
                if (token.isSuccessful) {
                    token.body()?.let {
                        println(it.accessToken)
                        println(it.refreshToken)
                        loader(true)
                        startApp()
                    }
                } else {
                    notAuthenticated()
                }
            } catch (e: Exception) {
                errorAuthenticating()
            }
        }
    }

    private fun loader(display: Boolean) {
        if (display) binding.rlLoginLoader.visibility = View.VISIBLE
        else binding.rlLoginLoader.visibility = View.GONE
    }

    private fun notAuthenticated() {
        loader(false)
        Tools.Show.message(this, "Login ou senha inválido")
    }

    private fun errorAuthenticating() {
        loader(false)
        Tools.Show.message(this, "Algo deu errado tente novamente")
    }

    private fun startApp() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        this.finish();
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}