package com.example.flightmobileapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


// Annotates class to be a Room Database with a table (entity) of the URL class
@Database(entities = arrayOf(URL::class), version = 1, exportSchema = false)
abstract class URLRoomDatabase : RoomDatabase() {

    abstract fun urlDao(): URLDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: URLRoomDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): URLRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    URLRoomDatabase::class.java,
                    "url_database"
                ).addCallback(WordDatabaseCallback(scope)).build()
                INSTANCE = instance
                return instance
            }
        }

    }

    private class WordDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.urlDao())
                }
            }
        }

        suspend fun populateDatabase(urlDao: URLDao) {
            // Delete all content here.
            //urlDao.deleteAll()
            urlDao.getAlphabetizedWords()
        }
    }

}