package com.insane.eyewalk.database.dto

import androidx.room.ColumnInfo
import androidx.room.Entity

const val CONTACT_PHONE_TABLE_NAME = "tb_contact_phone"

@Entity(tableName = CONTACT_PHONE_TABLE_NAME, primaryKeys = ["id_contact", "id_phone"])
data class ContactPhoneDTO(

    @ColumnInfo val id_contact: Int,
    @ColumnInfo val id_phone: Int

)
