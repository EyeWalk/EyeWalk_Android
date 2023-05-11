package com.insane.eyewalk.service

import com.insane.eyewalk.database.dto.UserDTO
import com.insane.eyewalk.database.room.AppDataBase
class RoomService (var db: AppDataBase) {

    fun getUser(): List<UserDTO> {
        return db.userDao().getAll()
    }

}