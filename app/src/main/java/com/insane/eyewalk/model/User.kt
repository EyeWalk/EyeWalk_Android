package com.insane.eyewalk.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.insane.eyewalk.database.dto.UserDTO
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class User (

    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("active")
    val active: Boolean,
    @SerializedName("plan")
    val plan: Plan,
    @SerializedName("planStart")
    val planStart: String,
    @SerializedName("planEnd")
    val planEnd: String,
    @SerializedName("contacts")
    val contacts: List<Contact>,
    @SerializedName("created")
    val created: String,
    @SerializedName("lastVisit")
    val lastVisit: String

): Parcelable {
    fun toUserDTO(): UserDTO {
        return UserDTO(
            id = id,
            name = name,
            email = email,
            active = active,
            planId = plan.id,
            created = created,
            lastVisit = lastVisit
        )
    }
}