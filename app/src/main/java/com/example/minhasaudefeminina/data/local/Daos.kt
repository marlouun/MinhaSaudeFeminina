package com.example.minhasaudefeminina.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE emailNormalized = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE userId = :userId LIMIT 1")
    fun observe(userId: String): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE userId = :userId LIMIT 1")
    suspend fun get(userId: String): ProfileEntity?

    @Upsert
    suspend fun upsert(profile: ProfileEntity)
}

@Dao
interface SymptomDao {
    @Query("SELECT * FROM symptom_records WHERE userId = :userId ORDER BY dataTimestamp DESC, createdAt DESC")
    fun observeForUser(userId: String): Flow<List<SymptomRecordEntity>>

    @Query("SELECT * FROM symptom_records WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getById(id: String, userId: String): SymptomRecordEntity?

    @Upsert
    suspend fun upsert(record: SymptomRecordEntity)

    @Query("DELETE FROM symptom_records WHERE id = :id AND userId = :userId")
    suspend fun deleteById(id: String, userId: String)
}

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE status = 'PUBLICADO' ORDER BY publishedAt DESC, updatedAt DESC")
    fun observePublished(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ArticleEntity?

    @Query("SELECT COUNT(*) FROM articles")
    suspend fun count(): Int

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles")
    suspend fun deleteAll()
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY sentAt ASC")
    fun observeForUser(userId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_messages WHERE userId = :userId")
    suspend fun countForUser(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE userId = :userId")
    suspend fun clearForUser(userId: String)
}
