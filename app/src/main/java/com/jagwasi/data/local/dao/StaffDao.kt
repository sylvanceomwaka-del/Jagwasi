package com.jagwasi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jagwasi.data.local.entity.Staff
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Insert
    suspend fun insert(staff: Staff): Long

    @Update
    suspend fun update(staff: Staff)

    @Delete
    suspend fun delete(staff: Staff)

    @Query("SELECT * FROM staff WHERE is_active = 1")
    fun getActiveStaff(): Flow<List<Staff>>

    @Query("SELECT * FROM staff WHERE id = :id")
    suspend fun getStaffById(id: Int): Staff?

    @Query("SELECT * FROM staff WHERE role = :role")
    fun getStaffByRole(role: String): Flow<List<Staff>>
}
