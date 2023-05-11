package com.insane.eyewalk.database.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val CONTACT_TABLE_NAME = "tb_contact"

@Entity(tableName = CONTACT_TABLE_NAME)
data class ContactDTO(

    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo val name: String,
    @ColumnInfo val emergency: Boolean,

) {}
