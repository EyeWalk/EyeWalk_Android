package com.insane.eyewalk.interfaces

import com.insane.eyewalk.config.AppConfig
import com.insane.eyewalk.model.Contact
import com.insane.eyewalk.model.Token
import com.insane.eyewalk.model.User
import com.insane.eyewalk.model.input.ContactInput
import com.insane.eyewalk.model.input.UserAuthentication
import com.insane.eyewalk.model.input.UserRegisterInput
import okhttp3.OkHttpClient
import org.apache.http.HttpStatus
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiInterface {

    @POST("auth/authenticate")
    suspend fun authenticateUser(
        @Body userAuthentication: UserAuthentication
    ): Response<Token>

    @POST("auth/refresh-token")
    suspend fun refreshToken(
        @Header("Authorization") refreshToken: String
    ): Response<Token>

    @GET("user")
    suspend fun getUser(
        @Header("Authorization") token: String
    ): Response<User>

    @POST("auth/register")
    suspend fun registerUser(
        @Body userRegisterInput: UserRegisterInput
    ): Response<Token>

    @GET("auth/logout")
    suspend fun logoutUser(
        @Header("Authorization") token: String
    )

    @GET("contact")
    suspend fun getContacts(
        @Header("Authorization") token: String
    ): Response<List<Contact>>

    @POST("contact")
    suspend fun saveContact(
        @Header("Authorization") token: String,
        @Body contact: ContactInput
    ): Response<Contact>

    @DELETE("contact/{id}")
    suspend fun deleteContact(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    )

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