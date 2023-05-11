package com.insane.eyewalk.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact (

    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("emergency")
    val emergency: Boolean,
    @SerializedName("phones")
    val phones: List<Phone>,
    @SerializedName("emails")
    val emails: List<Email>,
    @SerializedName("pictures")
    val pictures: List<Picture>,

):Parcelable {

}