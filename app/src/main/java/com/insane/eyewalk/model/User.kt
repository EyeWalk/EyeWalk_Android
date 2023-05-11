package com.insane.eyewalk.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.insane.eyewalk.database.dto.UserDTO
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(

    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("active")
    val active: Boolean,
    @SerializedName("token")
    val token: Token,
    @SerializedName("contacts")
    val contacts: List<Contact>,
    @SerializedName("plan")
    val plan: Plan,

): Parcelable {
    fun toUserDTO(): UserDTO {
        return UserDTO(
            id = id,
            name = name,
            email = email,
            active = active
        )
    }
}