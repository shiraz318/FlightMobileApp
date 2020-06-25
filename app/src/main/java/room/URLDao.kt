package room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface URLDao {

    @Query("SELECT * from url_table ORDER BY position ASC")
    fun getUrlsOrderedByPosition(): LiveData<List<URLItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(url: URLItem)

    @Query("UPDATE url_table SET position = position + 1")
    suspend fun increaseAll()

    @Query("UPDATE url_table SET position = position + 1 WHERE position < :changedItemPosition")
    suspend fun updatePosition(changedItemPosition: Int)

    @Query("UPDATE url_table SET position = 0 WHERE url LIKE :url")
    suspend fun initPosition(url: String)

    @Query("DELETE FROM url_table")
    suspend fun deleteAll()

    @Query("DELETE FROM url_table WHERE position > 5")
    suspend fun deleteExtra()

    @Query("SELECT url from url_table WHERE position = :position")
    suspend fun getUrlByPosition(position: Int): String

    @Query("SELECT COUNT(1) FROM url_table WHERE url LIKE :url")
    suspend fun alreadyExists(url: String): Int

    @Query("SELECT position FROM url_table WHERE url LIKE :url")
    suspend fun getPositionByUrl(url: String): Int
}