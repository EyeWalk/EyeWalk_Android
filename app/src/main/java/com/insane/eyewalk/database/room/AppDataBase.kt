package com.insane.eyewalk.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.insane.eyewalk.database.dao.SettingDAO
import com.insane.eyewalk.database.dao.TokenDAO
import com.insane.eyewalk.database.dao.UserDAO
import com.insane.eyewalk.database.dto.SettingDTO
import com.insane.eyewalk.database.dto.TokenDTO
import com.insane.eyewalk.database.dto.UserDTO

const val DATABASE_NAME = "db_eyewalk"

@Database(entities = [
    UserDTO::class,
    TokenDTO::class,
    SettingDTO::class
    ], version = 4)

abstract class AppDataBase: RoomDatabase() {

    abstract fun userDao(): UserDAO
    abstract fun tokenDao(): TokenDAO
    abstract fun settingDao(): SettingDAO

    companion object {
        private var INSTANCE: AppDataBase? = null
        fun getDataBase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context,
                    AppDataBase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

}