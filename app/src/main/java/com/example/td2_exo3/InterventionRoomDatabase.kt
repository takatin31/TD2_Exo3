package com.example.td2_exo3


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Intervention::class), version = 1, exportSchema = false)
abstract class InterventionRoomDatabase : RoomDatabase() {

    abstract fun interventionDao(): InterventionDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: InterventionRoomDatabase? = null

        fun getDatabase(context: Context): InterventionRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InterventionRoomDatabase::class.java,
                    "intervention_database2"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}