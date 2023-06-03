package com.insane.eyewalk.interfaces

import com.insane.eyewalk.model.api.ImageRequest
import com.insane.eyewalk.model.api.ImageResponse
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface GoogleApiInterface {

 @POST("./images:annotate")
    suspend fun analyzeImage(
        @Header("Content-Type") content: String = "application/json",
        @Query("key") key: String,
        @Body imageRequest: ImageRequest
    ): Response<ImageResponse>

    companion object {
        fun create() : GoogleApiInterface {
            val client = OkHttpClient.Builder().build()
            return Retrofit.Builder()
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://vision.googleapis.com/v1/")
                .build().create(GoogleApiInterface::class.java)
        }
    }
}