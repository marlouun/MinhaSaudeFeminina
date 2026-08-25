package com.example.minhasaudefeminina.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        ProfileEntity::class,
        SymptomRecordEntity::class,
        ArticleEntity::class,
        ChatMessageEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun profileDao(): ProfileDao
    abstract fun symptomDao(): SymptomDao
    abstract fun articleDao(): ArticleDao
    abstract fun chatDao(): ChatDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE symptom_records ADD COLUMN endTimestamp INTEGER DEFAULT NULL")
            }
        }

        fun create(context: Context): AppDatabase = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "minha_saude_feminina.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }
}
