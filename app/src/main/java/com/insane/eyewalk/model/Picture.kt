package com.insane.eyewalk.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Picture (

    @SerializedName("id")
    val id: Long,
    @SerializedName("filename")
    val filename: String,
    @SerializedName("extension")
    val extension: String

):Parcelable {

}