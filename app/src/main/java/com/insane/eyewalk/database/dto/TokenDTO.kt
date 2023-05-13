package com.insane.eyewalk.database.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.insane.eyewalk.model.Token

const val TOKEN_TABLE_NAME = "tb_token"

@Entity(tableName = TOKEN_TABLE_NAME)
data class TokenDTO(

    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo var accessToken: String,
    @ColumnInfo var refreshToken: String

) {
    fun toToken(): Token {
        return Token(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
}
