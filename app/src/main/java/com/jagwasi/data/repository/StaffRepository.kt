package com.jagwasi.data.repository

import com.jagwasi.data.local.entity.Staff
import kotlinx.coroutines.flow.Flow

interface StaffRepository {
    suspend fun insertStaff(staff: Staff): Long
    suspend fun updateStaff(staff: Staff)
    suspend fun deleteStaff(staff: Staff)
    fun getActiveStaff(): Flow<List<Staff>>
    suspend fun getStaffById(id: Int): Staff?
    fun getStaffByRole(role: String): Flow<List<Staff>>
}
