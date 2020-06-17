package com.example.flightmobileapp

import androidx.lifecycle.LiveData
import kotlinx.coroutines.*


// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class URLRepository(private val urlDao: URLDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allWords: LiveData<List<URLItem>> = urlDao.getUrlsOrderedByPosition()

    suspend fun insert(url: URLItem) {
        urlDao.insert(url)
    }

    suspend fun increaseAll() {
        urlDao.increaseAll()
    }

    suspend fun deleteExtra() {
        urlDao.deleteExtra()
    }

    suspend fun getUrlByPosition(position: Int): String {
        return urlDao.getUrlByPosition(position)
    }

    suspend fun updatePosition(changedItemPosition: Int) {
        urlDao.updatePosition(changedItemPosition)
    }

    suspend fun initPosition(url: String) {
        urlDao.initPosition(url)
    }

    suspend fun alreadyExists(url: String): Int {
        return urlDao.alreadyExists(url)
    }

    suspend fun getPositionByUrl(url: String): Int {
        return urlDao.getPositionByUrl(url)
    }
}