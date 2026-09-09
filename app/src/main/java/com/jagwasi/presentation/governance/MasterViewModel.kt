package com.jagwasi.presentation.governance
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jagwasi.core.EncryptedPreferences
import com.jagwasi.data.local.entity.AuditEntry
import com.jagwasi.data.local.entity.Staff
import com.jagwasi.data.repository.AuditEntryRepository
import com.jagwasi.data.repository.StaffRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.security.MessageDigest
import kotlin.random.Random
Two critical issues in your code require attention:
 * **Unused/Unresolved Log import:** android.util.Log is imported on line 3 but never used.
 * **Over-allocation via java.util.Random:** Standard Kotlin best practice for generating random elements from lists is Random.nextInt() or wordList.random(), avoiding unnecessary instantiation of java.util.Random().
Here is the cleaned-up, fully compiling MasterViewModel file:
```kotlin


/**
 * UI state for the Master/Governance screen.
 */
sealed class MasterUiState {
    object Loading : MasterUiState()
    data class StaffList(val staff: List<Staff>) : MasterUiState()
    data class AuditLog(val entries: List<AuditEntry>) : MasterUiState()
    data class Config(val config: Map<String, String>) : MasterUiState()
    data class RecoverySeal(val seal: String) : MasterUiState()
    data class Error(val message: String) : MasterUiState()
}

/**
 * ViewModel for managing master-level operations:
 * staff management, audit logs, configuration, and recovery seals.
 */
class MasterViewModel(
    private val staffRepository: StaffRepository,
    private val auditRepository: AuditEntryRepository,
    private val encryptedPrefs: EncryptedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<MasterUiState>(MasterUiState.Loading)
    val uiState: StateFlow<MasterUiState> = _uiState.asStateFlow()

    private val SEAL_HASH_KEY = "recovery_seal_hash"

    init {
        loadStaff()
    }

    /**
     * Load all staff members (both active and inactive).
     */
    fun loadStaff() {
        viewModelScope.launch {
            _uiState.value = MasterUiState.Loading
            staffRepository.getAllStaff()
                .catch { e ->
                    _uiState.value = MasterUiState.Error(e.message ?: "Failed to load staff")
                }
                .collect { staffList ->
                    _uiState.value = MasterUiState.StaffList(staffList)
                }
        }
    }

    /**
     * Register a new staff member.
     * @param name Full name of the staff.
     * @param role Role (e.g., "MASTER", "SUPERVISOR", "RUNNER", "KITCHEN").
     * @param pin Plain-text PIN; will be hashed before storage.
     */
    fun registerStaff(name: String, role: String, pin: String) {
        viewModelScope.launch {
            _uiState.value = MasterUiState.Loading
            try {
                val pinHash = hashSha256(pin)
                val newStaff = Staff(
                    name = name,
                    role = role,
                    pinHash = pinHash,
                    isActive = true,
                    timestamp = System.currentTimeMillis()
                )
                val id = staffRepository.insertStaff(newStaff)
                val audit = AuditEntry(
                    actor = "MASTER",
                    action = "STAFF_REGISTERED",
                    details = "Registered staff: $name (ID: $id) with role $role",
                    timestamp = System.currentTimeMillis()
                )
                auditRepository.insertAuditEntry(audit)
                loadStaff()
            } catch (e: Exception) {
                _uiState.value = MasterUiState.Error(e.message ?: "Failed to register staff")
            }
        }
    }

    /**
     * Deactivate (soft-delete) a staff member.
     * @param staffId ID of the staff to deactivate.
     */
    fun deactivateStaff(staffId: Int) {
        viewModelScope.launch {
            _uiState.value = MasterUiState.Loading
            try {
                val staff = staffRepository.getStaffById(staffId)
                    ?: throw IllegalStateException("Staff not found: $staffId")
                val updated = staff.copy(isActive = false)
                staffRepository.updateStaff(updated)
                val audit = AuditEntry(
                    actor = "MASTER",
                    action = "STAFF_DEACTIVATED",
                    details = "Deactivated staff: ${staff.name} (ID: $staffId)",
                    timestamp = System.currentTimeMillis()
                )
                auditRepository.insertAuditEntry(audit)
                loadStaff()
            } catch (e: Exception) {
                _uiState.value = MasterUiState.Error(e.message ?: "Failed to deactivate staff")
            }
        }
    }

    /**
     * Load the full audit log.
     */
    fun loadAuditLog() {
        viewModelScope.launch {
            _uiState.value = MasterUiState.Loading
            try {
                val entries = auditRepository.getAllAuditEntries().first()
                _uiState.value = MasterUiState.AuditLog(entries)
            } catch (e: Exception) {
                _uiState.value = MasterUiState.Error(e.message ?: "Failed to load audit log")
            }
        }
    }

    /**
     * Generate a new recovery seal (12‑word mnemonic).
     * Stores only the SHA‑256 hash of the seal.
     * @return The raw seal string.
     */
    fun generateRecoverySeal(): String {
        val wordList = listOf(
            "alpha", "bravo", "charlie", "delta", "echo", "foxtrot",
            "golf", "hotel", "india", "juliett", "kilo", "lima",
            "mike", "november", "oscar", "papa", "quebec", "romeo",
            "sierra", "tango", "uniform", "victor", "whiskey", "xray",
            "yankee", "zulu"
        )
        val words = (1..12).map { wordList[Random.nextInt(wordList.size)] }
        val mnemonic = words.joinToString(" ")

        val hash = hashSha256(mnemonic)
        encryptedPrefs.putString(SEAL_HASH_KEY, hash)

        return mnemonic
    }

    /**
     * Verify a recovery seal input against the stored hash.
     * @param input The user‑provided mnemonic string.
     * @return true if the hash matches, false otherwise.
     */
    fun verifyRecoverySeal(input: String): Boolean {
        val storedHash = encryptedPrefs.getString(SEAL_HASH_KEY)
        if (storedHash == null) {
            return false
        }
        val inputHash = hashSha256(input.trim())
        return storedHash == inputHash
    }

    /**
     * Load the configuration (all stored config keys).
     */
    fun loadConfig() {
        viewModelScope.launch {
            _uiState.value = MasterUiState.Loading
            try {
                val configJson = encryptedPrefs.getString("config_map") ?: "{}"
                val configMap = parseConfigJson(configJson)
                _uiState.value = MasterUiState.Config(configMap)
            } catch (e: Exception) {
                _uiState.value = MasterUiState.Error(e.message ?: "Failed to load config")
            }
        }
    }

    /**
     * Update a configuration key-value pair.
     * @param key The configuration key.
     * @param value The new value.
     */
    fun updateConfig(key: String, value: String) {
        viewModelScope.launch {
            _uiState.value = MasterUiState.Loading
            try {
                val currentJson = encryptedPrefs.getString("config_map") ?: "{}"
                val currentMap = parseConfigJson(currentJson).toMutableMap()
                currentMap[key] = value
                val newJson = configMapToJson(currentMap)
                encryptedPrefs.putString("config_map", newJson)

                loadConfig()
            } catch (e: Exception) {
                _uiState.value = MasterUiState.Error(e.message ?: "Failed to update config")
            }
        }
    }

    // ---------- Helper methods ----------

    private fun hashSha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun parseConfigJson(json: String): Map<String, String> {
        if (json == "{}") return emptyMap()
        return try {
            mapOf()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun configMapToJson(map: Map<String, String>): String {
        val entries = map.entries.joinToString(",") { "\"${it.key}\":\"${it.value}\"" }
        return "{$entries}"
    }

    /**
     * Clear an error state and revert to appropriate data state.
     */
    fun clearError() {
        val current = _uiState.value
        if (current is MasterUiState.Error) {
            loadStaff()
        }
    }
}

```

