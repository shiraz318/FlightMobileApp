package com.example.flightmobileapp
import androidx.lifecycle.LiveData


// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class URLRepository(private val urlDao: URLDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allWords: LiveData<List<URL>> = urlDao.getAlphabetizedWords()

    suspend fun insert(url: URL) {
        urlDao.insert(url)
    }
}