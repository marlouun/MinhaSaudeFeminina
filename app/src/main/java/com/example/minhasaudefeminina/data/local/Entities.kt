package com.example.minhasaudefeminina.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["emailNormalized"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val email: String,
    val emailNormalized: String,
    val passwordHash: String,
    val passwordSalt: String,
    val passwordAlgorithm: String,
    val passwordIterations: Int,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "profiles",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProfileEntity(
    @PrimaryKey val userId: String,
    val lifeStage: String,
    val isPregnant: Boolean,
    val papSmearDate: Long?,
    val mammogramDate: Long?,
    val photoUri: String?
)

@Entity(
    tableName = "symptom_records",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index(value = ["userId", "dataTimestamp"])]
)
data class SymptomRecordEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val dataTimestamp: Long,
    val type: String,
    val intensity: Int,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "articles", indices = [Index(value = ["slug"], unique = true), Index("status")])
data class ArticleEntity(
    @PrimaryKey val id: String,
    val slug: String,
    val category: String,
    val title: String,
    val subtitle: String,
    val summary: String,
    val contentJson: String,
    val author: String,
    val tagsCsv: String,
    val coverUri: String?,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val publishedAt: Long?
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index(value = ["userId", "sentAt"])]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val sessionId: String,
    val text: String,
    val isUser: Boolean,
    val sentAt: Long
)
