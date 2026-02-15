package com.evomind.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "offline_cache")
data class OfflineCacheEntity(
    @PrimaryKey val key: String,
    val value: String,
    val timestamp: Long,
    val expiresAt: Long
)

@Dao
interface OfflineCacheDao {
    @Query("SELECT * FROM offline_cache WHERE key = :key AND expiresAt > :currentTime")
    suspend fun get(key: String, currentTime: Long): OfflineCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OfflineCacheEntity)

    @Query("DELETE FROM offline_cache WHERE expiresAt < :currentTime")
    suspend fun deleteExpired(currentTime: Long)

    @Query("DELETE FROM offline_cache WHERE key = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM offline_cache")
    suspend fun clearAll()
}

@Database(entities = [OfflineCacheEntity::class], version = 1)
abstract class OfflineCacheDatabase : RoomDatabase() {
    abstract fun offlineCacheDao(): OfflineCacheDao
}

class OfflineCacheManager(private val context: Context) {
    private val db = Room.databaseBuilder(
        context,
        OfflineCacheDatabase::class.java,
        "offline_cache.db"
    ).build()

    suspend fun <T> getOrFetch(
        key: String,
        maxAgeMillis: Long = 3600000,
        fetch: suspend () -> T,
        serialize: (T) -> String,
        deserialize: (String) -> T
    ): T {
        val currentTime = System.currentTimeMillis()
        val cached = db.offlineCacheDao().get(key, currentTime)
        
        return if (cached != null) {
            try {
                deserialize(cached.value)
            } catch (e: Exception) {
                val fresh = fetch()
                save(key, fresh, serialize, maxAgeMillis)
                fresh
            }
        } else {
            val fresh = fetch()
            save(key, fresh, serialize, maxAgeMillis)
            fresh
        }
    }

    private suspend fun <T> save(key: String, data: T, serialize: (T) -> String, maxAgeMillis: Long) {
        val entity = OfflineCacheEntity(
            key = key,
            value = serialize(data),
            timestamp = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + maxAgeMillis
        )
        db.offlineCacheDao().insert(entity)
    }

    suspend fun invalidate(key: String) {
        db.offlineCacheDao().delete(key)
    }

    suspend fun clearExpired() {
        db.offlineCacheDao().deleteExpired(System.currentTimeMillis())
    }

    suspend fun clearAll() {
        db.offlineCacheDao().clearAll()
    }
}
