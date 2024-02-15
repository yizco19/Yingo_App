package com.zy.proyecto_final.dao

import android.accounts.Account
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zy.proyecto_final.pojo.User

@Dao
interface UserDao {

    @Insert
    fun insertAll(vararg users: User)
    @Insert
    fun add(user: User)
    @Delete
    fun delete(user: User)
    @Update
    fun update(user: User)
    /*@Query("SELECT * FROM User WHERE id = :imagenId")
    suspend fun obtenerImagenPorId(imagenId: Long): User?*/

    @Query("SELECT * FROM user")
    fun getAll(): List<User>
    @Query("SELECT * FROM user WHERE username = :username")
     fun getUser(username: String): User
}