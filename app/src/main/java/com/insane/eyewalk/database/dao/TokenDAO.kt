package com.insane.eyewalk.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.insane.eyewalk.database.dto.TOKEN_TABLE_NAME
import com.insane.eyewalk.database.dto.TokenDTO

@Dao
interface TokenDAO {

    @Query("SELECT * FROM $TOKEN_TABLE_NAME ORDER BY id ASC")
    fun getAll(): List<TokenDTO>

    @Query("SELECT * FROM $TOKEN_TABLE_NAME WHERE id = :id")
    fun getById(id: Int): TokenDTO

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(token: TokenDTO)

    @Query("DELETE FROM $TOKEN_TABLE_NAME WHERE id = :id")
    fun removeById(id: Int)

    @Query("DELETE FROM $TOKEN_TABLE_NAME")
    fun truncateTable()

}