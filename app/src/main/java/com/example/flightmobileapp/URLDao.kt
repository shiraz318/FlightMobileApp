package com.example.flightmobileapp

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface URLDao {

    @Query("SELECT * from url_table ORDER BY time ASC")
    fun getAlphabetizedWords(): LiveData<List<URL>>

    @Query("SELECT COUNT(url) from url_table")
    fun getSize(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(url: URL)

    @Query("DELETE FROM url_table")
    suspend fun deleteAll()

}