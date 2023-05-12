package com.insane.eyewalk.database.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.insane.eyewalk.database.room.AppDataBase
import com.insane.eyewalk.model.Token
import com.insane.eyewalk.model.User
import java.time.LocalDate

const val USER_TABLE_NAME = "tb_user"

@Entity(tableName = USER_TABLE_NAME)
data class UserDTO(

    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo val name: String,
    @ColumnInfo val email: String,
    @ColumnInfo val active: Boolean,
    @ColumnInfo val planId: Long,
    @ColumnInfo val planName: String,
    @ColumnInfo val planStart: String,
    @ColumnInfo val planEnd: String,
    @ColumnInfo val created: String,
    @ColumnInfo val lastVisit: String

) {
//    fun toUser(db: AppDataBase): User {
//        return User(
//            id = id,
//            name = name,
//            email = email,
//            active = active,
//            token = token,
//            contacts = contacts,
//            plan = plan
//        )
//    }
}