package com.jagwasi.data.repository

import com.jagwasi.data.local.entity.AuditEntry
import kotlinx.coroutines.flow.Flow

interface AuditEntryRepository {
    suspend fun insertAuditEntry(auditEntry: AuditEntry): Long
    fun getAllAuditEntries(): Flow<List<AuditEntry>>
    fun getAuditEntriesByActor(actor: String): Flow<List<AuditEntry>>
    fun getAuditEntriesByAction(action: String): Flow<List<AuditEntry>>
    fun getAuditEntriesBetween(start: Long, end: Long): Flow<List<AuditEntry>>
}
