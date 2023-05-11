package com.insane.eyewalk.interfaces

import com.insane.eyewalk.config.AppConfig
import com.insane.eyewalk.model.Token
import com.insane.eyewalk.model.input.UserAuthentication
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiInterface {

    @POST("/auth/authenticate")
    suspend fun authenticateUser(
        @Body userAuthentication: UserAuthentication
    ): Response<Token>

    companion object {
        fun create() : ApiInterface {
            val client = OkHttpClient.Builder().build()
            return Retrofit.Builder()
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(AppConfig.API.getHost())
                .build().create(ApiInterface::class.java)
        }
    }
}