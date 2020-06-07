package com.example.flightmobileapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class  URLViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: URLRepository
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allUrls: LiveData<List<URL>>

    init {
        val urlsDao = URLRoomDatabase.getDatabase(application, viewModelScope).urlDao()
        repository = URLRepository(urlsDao)
        allUrls = repository.allWords
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(url: URL) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(url)
    }
}