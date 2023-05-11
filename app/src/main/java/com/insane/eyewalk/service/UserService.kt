package com.insane.eyewalk.service

import com.insane.eyewalk.interfaces.ApiInterface
import com.insane.eyewalk.model.Token
import com.insane.eyewalk.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

object UserService {

    private val apiService = ApiInterface.create()

    suspend fun getUser(token: Token): Response<User> = withContext(Dispatchers.IO) {
        apiService.getUser(token = "Bearer ${token.accessToken}")
    }

}