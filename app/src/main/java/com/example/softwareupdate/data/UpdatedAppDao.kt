package com.example.softwareupdate.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UpdatedAppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdatedApp(packageInfoEntity: PackageInfoEntity)

    @Query("SELECT * FROM packageInfo_table")
    fun getAllUpdatedApp(): kotlinx.coroutines.flow.Flow<List<PackageInfoEntity>>

    @Query("SELECT COUNT(*) FROM packageInfo_table")
    suspend fun getRowCount(): Int

    @Query("delete  from packageInfo_table")
    suspend fun deleteAllTables()
}