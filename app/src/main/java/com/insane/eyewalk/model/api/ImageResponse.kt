package com.insane.eyewalk.model.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageResponse(
    @SerializedName("responses")
    var responses: List<ResponseList> = ArrayList()
):Parcelable

@Parcelize
data class ResponseList(
    @SerializedName("labelAnnotations")
    var labelAnnotations: List<Label> = ArrayList()
): Parcelable

@Parcelize
data class Label(
    @SerializedName("mid")
    var mid: String,
    @SerializedName("description")
    var description: String,
    @SerializedName("score")
    var score: Float,
    @SerializedName("topicality")
    var topicality: Float
): Parcelable