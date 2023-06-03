package com.insane.eyewalk.service

import com.insane.eyewalk.interfaces.ApiInterface
import com.insane.eyewalk.model.Contact
import com.insane.eyewalk.model.Token
import com.insane.eyewalk.model.User
import com.insane.eyewalk.model.input.ContactInput
import com.insane.eyewalk.model.input.UserRegisterInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.http.HttpStatus
import retrofit2.Response

object UserService {

    private val apiService = ApiInterface.create()

    suspend fun getUser(token: Token): Response<User> = withContext(Dispatchers.IO) {
        apiService.getUser(token = "Bearer ${token.accessToken}")
    }

    suspend fun registerUser(userRegisterInput: UserRegisterInput) = withContext(Dispatchers.IO) {
        apiService.registerUser(userRegisterInput)
    }

    suspend fun logout(token: Token) = withContext(Dispatchers.IO) {
        apiService.logoutUser(token = "Bearer ${token.accessToken}")
    }

    suspend fun getContacts(token: Token): Response<List<Contact>> = withContext(Dispatchers.IO) {
        apiService.getContacts(token = "Bearer ${token.accessToken}")
    }

    suspend fun saveContact(token: Token, contact: ContactInput): Response<Contact> = withContext(Dispatchers.IO) {
        apiService.saveContact(token = "Bearer ${token.accessToken}",contact)
    }

    suspend fun deleteContact(token: Token, contact: Contact) = withContext(Dispatchers.IO) {
        apiService.deleteContact(token = "Bearer ${token.accessToken}", contact.id)
    }

}