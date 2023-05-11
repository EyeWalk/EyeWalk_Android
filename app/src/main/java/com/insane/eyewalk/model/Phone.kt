package com.insane.eyewalk.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Phone (

    @SerializedName("id")
    val id: Long,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("type")
    val type: String,

): Parcelable {

}