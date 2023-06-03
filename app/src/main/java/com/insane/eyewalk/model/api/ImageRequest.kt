package com.insane.eyewalk.model.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageRequest (
    @SerializedName("requests")
    var requests: List<Image> = ArrayList()
): Parcelable

@Parcelize
data class Image (
    @SerializedName("image")
    var image: ImageContent = ImageContent(),
    @SerializedName("features")
    var features: List<FeatureType> = ArrayList()
): Parcelable

@Parcelize
data class ImageContent (
    @SerializedName("content")
    var content: String = "",
    @SerializedName("source")
    var source: ImageSource = ImageSource("")
):Parcelable

@Parcelize
data class ImageSource (
    @SerializedName("imageUri")
    var imageUri: String
): Parcelable

@Parcelize
data class FeatureType (
    @SerializedName("type")
    var type: String = ""
): Parcelable