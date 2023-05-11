package com.insane.eyewalk.service

import com.insane.eyewalk.interfaces.ApiInterface
import com.insane.eyewalk.model.Token
import com.insane.eyewalk.model.input.UserAuthentication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

object TokenService {

    private val apiService = ApiInterface.create()

    suspend fun getToken(userAuthentication: UserAuthentication): Response<Token> = withContext(Dispatchers.IO) {
        apiService.authenticateUser(userAuthentication)
    }

}