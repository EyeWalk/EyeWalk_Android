package com.insane.eyewalk.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Picture (

    @SerializedName("id")
    val id: Long,
    @SerializedName("filename")
    val filename: String,
    @SerializedName("extension")
    val extension: String,
    @SerializedName("created")
    val created: Date,

):Parcelable {

}