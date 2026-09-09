package com.jagwasi.data.repository

import com.jagwasi.data.local.dao.AuditEntryDao
import com.jagwasi.data.local.entity.AuditEntry
import kotlinx.coroutines.flow.Flow

class AuditEntryRepositoryImpl(
    private val auditEntryDao: AuditEntryDao
) : AuditEntryRepository {

    override suspend fun insertAuditEntry(auditEntry: AuditEntry): Long {
        return auditEntryDao.insert(auditEntry)
    }

    override fun getAllAuditEntries(): Flow<List<AuditEntry>> {
        return auditEntryDao.getAllAuditEntries()
    }

    override fun getAuditEntriesByActor(actor: String): Flow<List<AuditEntry>> {
        return auditEntryDao.getAuditEntriesByActor(actor)
    }

    override fun getAuditEntriesByAction(action: String): Flow<List<AuditEntry>> {
        return auditEntryDao.getAuditEntriesByAction(action)
    }

    override fun getAuditEntriesBetween(start: Long, end: Long): Flow<List<AuditEntry>> {
        return auditEntryDao.getAuditEntriesBetween(start, end)
    }
}
