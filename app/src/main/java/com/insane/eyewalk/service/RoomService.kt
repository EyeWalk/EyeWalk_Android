package com.insane.eyewalk.service

import com.insane.eyewalk.database.dto.SettingDTO
import com.insane.eyewalk.database.dto.TokenDTO
import com.insane.eyewalk.database.dto.UserDTO
import com.insane.eyewalk.database.room.AppDataBase

class RoomService (var db: AppDataBase) {

    fun getUser(): UserDTO {
        return db.userDao().getUser()
    }

    fun existUser(): Boolean {
        return db.userDao().getAll().isNotEmpty()
    }

    fun existToken(): Boolean {
        return db.tokenDao().getAll().isNotEmpty()
    }

    fun getSetting(): SettingDTO {
        return db.settingDao().getSetting()
    }

    fun updateSetting(setting: SettingDTO) {
        db.settingDao().truncateTable()
        db.settingDao().insert(setting)
    }

    fun logoutUser() {
        db.tokenDao().truncateTable()
    }

    fun updateUser(user: UserDTO) {
        db.userDao().truncateTable()
        db.userDao().insert(user)
    }

    fun getToken(): TokenDTO {
        return db.tokenDao().getToken()
    }

    fun updateToken(token: TokenDTO) {
        db.tokenDao().truncateTable()
        db.tokenDao().insert(token)
    }

}