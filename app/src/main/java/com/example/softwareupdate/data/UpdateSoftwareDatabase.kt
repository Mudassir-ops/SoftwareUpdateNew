package com.example.softwareupdate.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PackageInfoEntity::class], version = 1)
abstract class UpdateSoftwareDatabase : RoomDatabase() {
    abstract fun updatedAppDao(): UpdatedAppDao
}