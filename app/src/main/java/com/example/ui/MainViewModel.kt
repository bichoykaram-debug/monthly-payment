package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: FinanceRepository) : ViewModel() {

    init {
        // Initialize database with seed structures if empty
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // Navigation and Page Management
    private val _currentPage = MutableStateFlow("google_login") // Start with the login screen style
    val currentPage: StateFlow<String> = _currentPage.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Google Sign-In States
    private val _isGoogleLoggedIn = MutableStateFlow(false)
    val isGoogleLoggedIn: StateFlow<Boolean> = _isGoogleLoggedIn.asStateFlow()

    private val _googleAccountName = MutableStateFlow("")
    val googleAccountName: StateFlow<String> = _googleAccountName.asStateFlow()

    private val _googleAccountEmail = MutableStateFlow("")
    val googleAccountEmail: StateFlow<String> = _googleAccountEmail.asStateFlow()

    // State flows from database
    val accounts = repository.accountsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transactions = repository.transactionsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val budgets = repository.budgetsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val goals = repository.goalsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val reminders = repository.remindersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val commitments = repository.commitmentsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recurring = repository.recurringFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val calendarNotes = repository.calendarNotesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val editHistory = repository.editHistoriesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val userSettings = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val settingsHistory = repository.settingsHistoriesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val txnSnapshots = repository.txnSnapshotsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filter and Search States
    val searchQuery = MutableStateFlow("")
    val selectedTypeFilter = MutableStateFlow("") // "", "income", "expense", "transfer"
    val selectedCatFilter = MutableStateFlow("") // "" or specific category name
    val selectedAccFilter = MutableStateFlow<Long?>(null) // null or specific account ID

    // Watch for settings changes to keep dark theme synchronized
    init {
        viewModelScope.launch {
            userSettings.collect { settings ->
                if (settings != null) {
                    _isDarkTheme.value = (settings.theme == "dark")
                    _googleAccountName.value = settings.name
                    _googleAccountEmail.value = settings.email
                }
            }
        }
    }

    // Navigation trigger
    fun navigateTo(page: String) {
        _currentPage.value = page
    }

    // Toggle theme
    fun toggleDarkTheme() {
        val nextTheme = if (_isDarkTheme.value) "light" else "dark"
        viewModelScope.launch {
            val settings = repository.getSettings() ?: return@launch
            repository.insertSettings(settings.copy(theme = nextTheme))
            logEdit("edit", "المظهر", "تغيير المظهر إلى ${if (nextTheme == "dark") "الداكن" else "الفاتح"}")
        }
    }

    // Simulated Google Login Auth Action
    fun performGoogleSignIn(name: String, email: String) {
        viewModelScope.launch {
            _googleAccountName.value = name
            _googleAccountEmail.value = email
            _isGoogleLoggedIn.value = true

            // Sync user settings with Google Sign-In details
            val settings = repository.getSettings()
            if (settings != null) {
                repository.insertSettings(settings.copy(name = name, email = email))
            } else {
                repository.insertSettings(
                    UserSettings(
                        name = name,
                        email = email,
                        phone = "+20 100 000 0000",
                        currency = "EGP",
                        language = "ar",
                        theme = "light",
                        notifBills = true,
                        notifBudget = true,
                        notifGoals = true,
                        notifSummary = true
                    )
                )
            }
            logEdit("add", "تسجيل الدخول", "دخول ناجح بحساب Google ($email)")
            _currentPage.value = "dashboard"
        }
    }

    // Log Out Action
    fun logout() {
        _isGoogleLoggedIn.value = false
        _currentPage.value = "google_login"
    }

    // Log user actions into the History logs
    fun logEdit(action: String, entity: String, desc: String) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            repository.insertEditHistory(
                EditHistory(
                    id = timestamp,
                    ts = timestamp,
                    action = action,
                    entity = entity,
                    desc = desc
                )
            )
        }
    }

    // Account Actions
    fun saveAccount(id: Long?, name: String, type: String, ob: Double, color: String, icon: String) {
        viewModelScope.launch {
            val accountId = id ?: System.currentTimeMillis()
            val account = Account(accountId, name, ob, type, color, icon, true)
            repository.insertAccount(account)
            logEdit(if (id == null) "add" else "edit", "حساب", "تم حفظ حساب $name برصيد $ob")
        }
    }

    fun toggleAccountActive(id: Long, active: Boolean) {
        viewModelScope.launch {
            val acc = repository.getAllAccounts().find { it.id == id } ?: return@launch
            repository.insertAccount(acc.copy(active = active))
            logEdit("edit", "حساب", "${if (active) "تفعيل" else "إيقاف"} الحساب ${acc.name}")
        }
    }

    fun updateAccountOpeningBalance(id: Long, ob: Double) {
        viewModelScope.launch {
            val acc = repository.getAllAccounts().find { it.id == id } ?: return@launch
            repository.insertAccount(acc.copy(ob = ob))
            logEdit("edit", "حساب", "تعديل الرصيد الافتتاحي للحساب ${acc.name} إلى $ob")
        }
    }

    fun deleteAccount(id: Long, name: String) {
        viewModelScope.launch {
            repository.deleteAccountById(id)
            logEdit("delete", "حساب", "حذف حساب $name")
        }
    }

    fun resetAllAccountBalances() {
        viewModelScope.launch {
            val accs = repository.getAllAccounts()
            accs.forEach { acc ->
                repository.insertAccount(acc.copy(ob = 0.0))
            }
            logEdit("edit", "الحسابات", "تصفير الأرصدة الافتتاحية للمحافظ والحسابات")
        }
    }

    // Transaction Actions
    fun saveTransaction(id: Long?, type: String, name: String, cat: String, amt: Double, date: String, accId: Long, toAccId: Long?, note: String) {
        viewModelScope.launch {
            val txnId = id ?: System.currentTimeMillis()
            val txnName = if (note.trim().isNotEmpty()) note.trim() else cat
            val transaction = Transaction(txnId, type, txnName, cat, amt, date, accId, toAccId, note)
            repository.insertTransaction(transaction)
            logEdit(if (id == null) "add" else "transaction", "عملية مالية", "$txnName بقيمة $amt")
        }
    }

    fun deleteTransaction(id: Long, name: String) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
            logEdit("delete", "عملية مالية", "حذف عملية $name")
        }
    }

    // Budget Actions
    fun saveBudget(id: Long?, cat: String, limit: Double, color: String) {
        viewModelScope.launch {
            val budgetId = id ?: System.currentTimeMillis()
            repository.insertBudget(Budget(budgetId, cat, limit, color))
            logEdit(if (id == null) "add" else "edit", "ميزانية", "حفظ ميزانية $cat بحد شهري $limit")
        }
    }

    fun deleteBudget(id: Long, cat: String) {
        viewModelScope.launch {
            repository.deleteBudgetById(id)
            logEdit("delete", "ميزانية", "حذف ميزانية $cat")
        }
    }

    // Goal Actions
    fun saveGoal(id: Long?, name: String, icon: String, target: Double, current: Double, color: String) {
        viewModelScope.launch {
            val goalId = id ?: System.currentTimeMillis()
            repository.insertGoal(Goal(goalId, name, icon, target, current, color))
            logEdit(if (id == null) "add" else "edit", "هدف مالي", "حفظ هدف $name بقيمة مستهدفة $target")
        }
    }

    fun deleteGoal(id: Long, name: String) {
        viewModelScope.launch {
            repository.deleteGoalById(id)
            logEdit("delete", "هدف مالي", "حذف هدف $name")
        }
    }

    // Reminder Actions
    fun saveReminder(id: Long?, name: String, icon: String, amt: Double, due: String) {
        viewModelScope.launch {
            val reminderId = id ?: System.currentTimeMillis()
            repository.insertReminder(Reminder(reminderId, name, icon, amt, due))
            logEdit(if (id == null) "add" else "edit", "تذكير", "حفظ تذكير تصفية $name بقيمة $amt تستحق في $due")
        }
    }

    fun deleteReminder(id: Long, name: String) {
        viewModelScope.launch {
            repository.deleteReminderById(id)
            logEdit("delete", "تذكير", "حذف تذكير $name")
        }
    }

    // Commitment Actions
    fun saveCommitment(id: Long?, name: String, icon: String, amt: Double, due: String, freq: String, acc: String) {
        viewModelScope.launch {
            val commitmentId = id ?: System.currentTimeMillis()
            repository.insertCommitment(Commitment(commitmentId, name, icon, amt, due, freq, acc))
            logEdit(if (id == null) "add" else "edit", "التزام مستقبل", "حفظ التزام بقيمة $amt يستحق في $due")
        }
    }

    fun deleteCommitment(id: Long, name: String) {
        viewModelScope.launch {
            repository.deleteCommitmentById(id)
            logEdit("delete", "التزام", "حذف التزام $name")
        }
    }

    // Recurring Actions
    fun saveRecurring(id: Long?, name: String, type: String, amt: Double, freq: String, day: Int, acc: String) {
        viewModelScope.launch {
            val recId = id ?: System.currentTimeMillis()
            repository.insertRecurring(Recurring(recId, name, type, amt, freq, day, acc, true))
            logEdit(if (id == null) "add" else "edit", "عملية متكررة", "حفظ عملية متكررة $name بقيمة $amt")
        }
    }

    fun toggleRecurringActive(id: Long, on: Boolean) {
        viewModelScope.launch {
            val rec = repository.getAllRecurrings().find { it.id == id } ?: return@launch
            repository.insertRecurring(rec.copy(on = on))
            logEdit("edit", "عملية متكررة", "${if (on) "تفعيل" else "تعطيل"} المعاملة المتكررة ${rec.name}")
        }
    }

    fun deleteRecurring(id: Long, name: String) {
        viewModelScope.launch {
            repository.deleteRecurringById(id)
            logEdit("delete", "عملية متكررة", "حذف عملية متكررة $name")
        }
    }

    // Calendar Note Actions
    fun saveCalendarNote(date: String, note: String) {
        viewModelScope.launch {
            if (note.trim().isEmpty()) {
                repository.deleteCalendarNoteByDate(date)
                logEdit("delete", "التقويم", "حذف ملاحظة date=$date")
            } else {
                repository.insertCalendarNote(CalendarNote(date, note.trim()))
                logEdit("edit", "التقويم", "حفظ ملاحظة لليوم $date")
            }
        }
    }

    fun deleteCalendarNote(date: String) {
        viewModelScope.launch {
            repository.deleteCalendarNoteByDate(date)
            logEdit("delete", "التقويم", "حذف ملاحظة date=$date")
        }
    }

    // Settings Configuration Actions
    fun saveSettings(name: String, email: String, phone: String, currency: String, language: String, theme: String, notifBills: Boolean, notifBudget: Boolean, notifGoals: Boolean, notifSummary: Boolean) {
        viewModelScope.launch {
            val prevSettings = repository.getSettings()
            if (prevSettings != null) {
                // Save current state into settings history
                val jsonSnapshot = """{"name":"${prevSettings.name}","email":"${prevSettings.email}","phone":"${prevSettings.phone}","currency":"${prevSettings.currency}","language":"${prevSettings.language}","theme":"${prevSettings.theme}"}"""
                repository.insertSettingsHistory(SettingsHistory(System.currentTimeMillis(), System.currentTimeMillis(), jsonSnapshot))
            }

            val newSettings = UserSettings(
                id = 1,
                name = name,
                email = email,
                phone = phone,
                currency = currency,
                language = language,
                theme = theme,
                notifBills = notifBills,
                notifBudget = notifBudget,
                notifGoals = notifGoals,
                notifSummary = notifSummary
            )
            repository.insertSettings(newSettings)
            logEdit("edit", "الإعدادات", "حفظ وتحديث معلومات الملف الشخصي والإعدادات")
        }
    }

    fun restoreSettingsHistory(history: SettingsHistory) {
        viewModelScope.launch {
            try {
                // Parse simple json snapshot (manual parsing avoids dependency issues)
                val json = history.snapshotJson
                val name = getValueFromJson(json, "name") ?: "Bichoy Karam"
                val email = getValueFromJson(json, "email") ?: "bichoykaram@gmail.com"
                val phone = getValueFromJson(json, "phone") ?: "+20 100 000 0000"
                val currency = getValueFromJson(json, "currency") ?: "EGP"
                val language = getValueFromJson(json, "language") ?: "ar"
                val theme = getValueFromJson(json, "theme") ?: "light"

                val prev = repository.getSettings() ?: return@launch
                val restored = prev.copy(
                    name = name,
                    email = email,
                    phone = phone,
                    currency = currency,
                    language = language,
                    theme = theme
                )
                repository.insertSettings(restored)
                logEdit("restore", "الإعدادات", "استرجاع نسخة إعدادات سابقة")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getValueFromJson(json: String, key: String): String? {
        val pattern = "\"$key\":\"([^\"]*)\"".toRegex()
        return pattern.find(json)?.groupValues?.get(1)
    }

    // Snapshot Backup & Restore Actions
    fun saveTxnSnapshot(label: String) {
        viewModelScope.launch {
            val currentTxns = repository.getAllTransactions()
            // Serialize list of txns to pseudo-json string safely
            val serialized = currentTxns.joinToString(separator = "|||") { t ->
                "${t.id};${t.type};${t.name};${t.cat};${t.amt};${t.date};${t.accId};${t.toAccId ?: ""};${t.note}"
            }
            val snapshot = TxnSnapshot(
                id = System.currentTimeMillis(),
                ts = System.currentTimeMillis(),
                label = label,
                txnsJson = serialized
            )
            repository.insertTxnSnapshot(snapshot)
            logEdit("add", "نسخة احتياطية", label)
        }
    }

    fun restoreTxnSnapshot(snapshot: TxnSnapshot) {
        viewModelScope.launch {
            try {
                // Clear current and insert snapshot data
                repository.deleteTransactionById(-1) // Triggers flow refresh
                val dao = AppDatabase.Companion.getDatabase(null ?: return@launch).financeDao()
                dao.clearTransactions()

                val rows = snapshot.txnsJson.split("|||")
                val restored = rows.mapNotNull { row ->
                    val parts = row.split(";")
                    if (parts.size >= 9) {
                        Transaction(
                            id = parts[0].toLongOrNull() ?: System.currentTimeMillis(),
                            type = parts[1],
                            name = parts[2],
                            cat = parts[3],
                            amt = parts[4].toDoubleOrNull() ?: 0.0,
                            date = parts[5],
                            accId = parts[6].toLongOrNull() ?: 1L,
                            toAccId = parts[7].toLongOrNull(),
                            note = parts[8]
                        )
                    } else null
                }
                dao.insertTransactions(restored)
                logEdit("restore", "العمليات", "استرجاع النسخة المحفوظة: ${snapshot.label}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun restoreOriginalTxns() {
        viewModelScope.launch {
            val dao = AppDatabase.Companion.getDatabase(null ?: return@launch).financeDao()
            dao.clearTransactions()
            logEdit("restore", "العمليات", "استرجاع النسخة الأصلية (قائمة فارغة صفرية)")
        }
    }

    // Reset whole app logic
    fun resetAllAppData() {
        viewModelScope.launch {
            repository.resetAllDataToDefault()
            logEdit("restore", "التطبيق", "إعادة ضبط وتصفير التطبيق بالكامل")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearEditHistories()
        }
    }
}

class MainViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
