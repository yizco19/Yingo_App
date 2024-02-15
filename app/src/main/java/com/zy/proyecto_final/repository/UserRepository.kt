package com.zy.proyecto_final.repository

import android.content.Context
import com.zy.proyecto_final.AppDatabase
import com.zy.proyecto_final.dao.UserDao
import com.zy.proyecto_final.pojo.User

class UserRepository (var context: Context){
    private  var _userDAO: UserDao

    init{
        val database= AppDatabase.getInstance(context)
        _userDAO=database.userDao()

    }
    fun getAll(): MutableList<User>? {
        TODO("Not yet implemented")
    }

    fun add(user: User) :Long? {
        if(user.username?.let { _userDAO.getUser(it) } ==null){
            _userDAO.add(user)
            return user.id
        }
        return null

    }

    fun getUser(account: String): Any {
        return _userDAO.getUser(account)

    }
}