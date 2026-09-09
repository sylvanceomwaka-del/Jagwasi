package com.jagwasi.domain.usecase

import com.jagwasi.data.local.entity.AuditEntry
import com.jagwasi.data.repository.AuditEntryRepository

class LogAuditUseCase(
    private val auditRepository: AuditEntryRepository
) {
    /**
     * Executes the audit entry creation.
     */
    suspend operator fun invoke(
        actor: String,
        action: String,
        details: String
    ): Result<Unit> {
        return try {
            val auditEntry = AuditEntry(
                actor = actor,
                action = action,
                details = details,
                timestamp = System.currentTimeMillis()
            )
            auditRepository.insertAuditEntry(auditEntry)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e.message ?: "Failed to log audit entry")
        }
    }
}

