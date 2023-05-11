package com.insane.eyewalk.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.insane.eyewalk.database.dto.USER_TABLE_NAME
import com.insane.eyewalk.database.dto.UserDTO

@Dao
interface UserDAO {

    @Query("SELECT * FROM $USER_TABLE_NAME ORDER BY id ASC")
    fun getAll(): List<UserDTO>

    @Query("SELECT * FROM $USER_TABLE_NAME WHERE id = :userId")
    fun getById(userId: Int): UserDTO

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: UserDTO)

    @Query("DELETE FROM $USER_TABLE_NAME WHERE id = :userId")
    fun removeById(userId: Int)

    @Query("UPDATE $USER_TABLE_NAME SET active = :active WHERE id = :userId")
    fun updateStatus(userId: Int, active: Boolean)

    @Query("DELETE FROM $USER_TABLE_NAME")
    fun truncateTable()

}