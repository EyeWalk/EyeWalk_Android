package com.insane.eyewalk.service

import com.insane.eyewalk.BuildConfig
import com.insane.eyewalk.interfaces.GoogleApiInterface
import com.insane.eyewalk.model.api.ImageRequest
import com.insane.eyewalk.model.api.ImageResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

object GoogleService {

    private val googleApiService = GoogleApiInterface.create()

    suspend fun analyzeImage(imageRequest: ImageRequest): Response<ImageResponse> = withContext(Dispatchers.IO) {
        googleApiService.analyzeImage(key = BuildConfig.API_KEY, imageRequest = imageRequest)
    }

}