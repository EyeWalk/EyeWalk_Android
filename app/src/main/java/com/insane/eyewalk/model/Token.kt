package com.insane.eyewalk.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.insane.eyewalk.database.dto.TokenDTO
import com.insane.eyewalk.database.dto.UserDTO
import kotlinx.parcelize.Parcelize

@Parcelize
data class Token (

    @SerializedName("access_token")
    var accessToken: String,
    @SerializedName("refresh_token")
    var refreshToken: String

): Parcelable {
    fun toTokenDTO(): TokenDTO {
        return TokenDTO(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
}