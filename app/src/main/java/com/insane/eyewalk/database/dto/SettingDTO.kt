package com.insane.eyewalk.database.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val SETTING_TABLE_NAME = "tb_setting"

@Entity(tableName = SETTING_TABLE_NAME)
data class SettingDTO(

    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo var switchVoice: Boolean,
    @ColumnInfo var switchRead: Boolean

    )
