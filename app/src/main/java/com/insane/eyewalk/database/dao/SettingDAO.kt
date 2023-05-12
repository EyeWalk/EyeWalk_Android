package com.insane.eyewalk.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.insane.eyewalk.database.dto.SETTING_TABLE_NAME
import com.insane.eyewalk.database.dto.SettingDTO

@Dao
interface SettingDAO {

    @Query("SELECT * FROM $SETTING_TABLE_NAME ORDER BY id ASC")
    fun getAll(): List<SettingDTO>

    @Query("SELECT * FROM $SETTING_TABLE_NAME LIMIT 1")
    fun getSetting(): SettingDTO

    @Query("SELECT * FROM $SETTING_TABLE_NAME WHERE id = :id")
    fun getById(id: Int): SettingDTO

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(setting: SettingDTO)

    @Query("DELETE FROM $SETTING_TABLE_NAME WHERE id = :id")
    fun removeById(id: Int)

    @Query("DELETE FROM $SETTING_TABLE_NAME")
    fun truncateTable()

}