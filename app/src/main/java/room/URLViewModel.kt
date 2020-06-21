package room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class URLViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: URLRepository

    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allUrls: LiveData<List<URLItem>>

    init {
        val urlsDao = URLRoomDatabase.getDatabase(
            application,
            viewModelScope
        ).urlDao()
        repository = URLRepository(urlsDao)
        allUrls = repository.allWords
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(url: URLItem) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(url)
    }

    fun increaseAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.increaseAll()
    }

    fun deleteExtra() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteExtra()
    }

    fun getUrlByPosition(position: Int): String = runBlocking {
        repository.getUrlByPosition(position)
    }

    fun updatePosition(changedItemPosition: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.updatePosition(changedItemPosition)
    }

    fun initPosition(url: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.initPosition(url)
    }

    fun alreadyExists(url: String): Int = runBlocking {
        repository.alreadyExists(url)
    }

    fun getPositionByUrl(url: String): Int = runBlocking {
        repository.getPositionByUrl(url)
    }

}