package com.insane.eyewalk.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.insane.eyewalk.database.dao.UserDAO
import com.insane.eyewalk.database.dto.UserDTO

const val DATABASE_NAME = "db_eyewalk"

@Database(entities = [
    UserDTO::class
    ], version = 1)

abstract class AppDataBase: RoomDatabase() {

    abstract fun userDao(): UserDAO

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