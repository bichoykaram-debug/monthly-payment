package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FinanceRepository(private val dao: FinanceDao) {

    // Seeding default values where tracking starts at zero
    suspend fun seedDatabaseIfEmpty() {
        val existingAccounts = dao.getAllAccounts()
        if (existingAccounts.isEmpty()) {
            val defaultAccounts = listOf(
                Account(1, "نقدي", 0.0, "نقد", "#2D6A4F", "fa-money-bill-wave", true),
                Account(2, "بنك الأهلي", 0.0, "حساب توفير", "#1D4ED8", "fa-university", true),
                Account(3, "بنك QNB", 0.0, "حساب توفير", "#7C3AED", "fa-university", true),
                Account(4, "بطاقة بنك القاهرة", 0.0, "بطاقة ائتمان", "#DC2626", "fa-credit-card", true),
                Account(5, "محفظة إلكترونية", 0.0, "محفظة", "#D97706", "fa-mobile-alt", true),
                Account(6, "حساب استثماري", 0.0, "استثمار", "#0F766E", "fa-chart-line", true)
            )
            dao.insertAccounts(defaultAccounts)
        }

        val existingBudgets = dao.getAllBudgets()
        if (existingBudgets.isEmpty()) {
            val defaultBudgets = listOf(
                Budget(1, "🏠 المنزل", 5000.0, "#2D6A4F"),
                Budget(2, "⛽ المواصلات", 1500.0, "#3B82F6"),
                Budget(3, "🍽️ الطعام", 2000.0, "#F59E0B"),
                Budget(4, "📱 اتصالات", 500.0, "#8B5CF6"),
                Budget(5, "💊 صحة", 1000.0, "#EF4444")
            )
            dao.insertBudgets(defaultBudgets)
        }

        val settings = dao.getSettings()
        if (settings == null) {
            val defaultSettings = UserSettings(
                name = "Bichoy Karam",
                email = "bichoykaram@gmail.com",
                phone = "+20 100 000 0000",
                currency = "EGP",
                language = "ar",
                theme = "light",
                notifBills = true,
                notifBudget = true,
                notifGoals = true,
                notifSummary = true
            )
            dao.insertSettings(defaultSettings)
        }
    }

    // Reset whole app data to original/seeded state
    suspend fun resetAllDataToDefault() {
        dao.clearAccounts()
        dao.clearTransactions()
        dao.clearBudgets()
        dao.clearGoals()
        dao.clearReminders()
        dao.clearCommitments()
        dao.clearRecurrings()
        dao.clearCalendarNotes()
        dao.clearEditHistories()
        dao.clearSettingsHistories()
        dao.clearTxnSnapshots()
        seedDatabaseIfEmpty()
    }

    // Accounts
    val accountsFlow: Flow<List<Account>> = dao.getAllAccountsFlow()
    suspend fun getAllAccounts(): List<Account> = dao.getAllAccounts()
    suspend fun insertAccount(account: Account) = dao.insertAccount(account)
    suspend fun insertAccounts(accounts: List<Account>) = dao.insertAccounts(accounts)
    suspend fun deleteAccountById(id: Long) = dao.deleteAccountById(id)

    // Transactions
    val transactionsFlow: Flow<List<Transaction>> = dao.getAllTransactionsFlow()
    suspend fun getAllTransactions(): List<Transaction> = dao.getAllTransactions()
    suspend fun insertTransaction(transaction: Transaction) = dao.insertTransaction(transaction)
    suspend fun insertTransactions(transactions: List<Transaction>) = dao.insertTransactions(transactions)
    suspend fun deleteTransactionById(id: Long) = dao.deleteTransactionById(id)

    // Budgets
    val budgetsFlow: Flow<List<Budget>> = dao.getAllBudgetsFlow()
    suspend fun getAllBudgets(): List<Budget> = dao.getAllBudgets()
    suspend fun insertBudget(budget: Budget) = dao.insertBudget(budget)
    suspend fun deleteBudgetById(id: Long) = dao.deleteBudgetById(id)

    // Goals
    val goalsFlow: Flow<List<Goal>> = dao.getAllGoalsFlow()
    suspend fun getAllGoals(): List<Goal> = dao.getAllGoals()
    suspend fun insertGoal(goal: Goal) = dao.insertGoal(goal)
    suspend fun deleteGoalById(id: Long) = dao.deleteGoalById(id)

    // Reminders
    val remindersFlow: Flow<List<Reminder>> = dao.getAllRemindersFlow()
    suspend fun getAllReminders(): List<Reminder> = dao.getAllReminders()
    suspend fun insertReminder(reminder: Reminder) = dao.insertReminder(reminder)
    suspend fun deleteReminderById(id: Long) = dao.deleteReminderById(id)

    // Commitments
    val commitmentsFlow: Flow<List<Commitment>> = dao.getAllCommitmentsFlow()
    suspend fun getAllCommitments(): List<Commitment> = dao.getAllCommitments()
    suspend fun insertCommitment(commitment: Commitment) = dao.insertCommitment(commitment)
    suspend fun deleteCommitmentById(id: Long) = dao.deleteCommitmentById(id)

    // Recurring
    val recurringFlow: Flow<List<Recurring>> = dao.getAllRecurringFlow()
    suspend fun getAllRecurrings(): List<Recurring> = dao.getAllRecurring()
    suspend fun insertRecurring(recurring: Recurring) = dao.insertRecurring(recurring)
    suspend fun deleteRecurringById(id: Long) = dao.deleteRecurringById(id)

    // Calendar Notes
    val calendarNotesFlow: Flow<List<CalendarNote>> = dao.getAllCalendarNotesFlow()
    suspend fun getAllCalendarNotes(): List<CalendarNote> = dao.getAllCalendarNotes()
    suspend fun insertCalendarNote(note: CalendarNote) = dao.insertCalendarNote(note)
    suspend fun deleteCalendarNoteByDate(date: String) = dao.deleteCalendarNoteByDate(date)

    // History
    val editHistoriesFlow: Flow<List<EditHistory>> = dao.getAllEditHistoriesFlow()
    suspend fun getAllEditHistories(): List<EditHistory> = dao.getAllEditHistories()
    suspend fun insertEditHistory(history: EditHistory) = dao.insertEditHistory(history)
    suspend fun clearEditHistories() = dao.clearEditHistories()

    // Settings
    val settingsFlow: Flow<UserSettings?> = dao.getSettingsFlow()
    suspend fun getSettings(): UserSettings? = dao.getSettings()
    suspend fun insertSettings(settings: UserSettings) = dao.insertSettings(settings)

    // Settings History
    val settingsHistoriesFlow: Flow<List<SettingsHistory>> = dao.getAllSettingsHistoriesFlow()
    suspend fun getAllSettingsHistories(): List<SettingsHistory> = dao.getAllSettingsHistories()
    suspend fun insertSettingsHistory(history: SettingsHistory) = dao.insertSettingsHistory(history)

    // Snapshots
    val txnSnapshotsFlow: Flow<List<TxnSnapshot>> = dao.getAllTxnSnapshotsFlow()
    suspend fun getAllTxnSnapshots(): List<TxnSnapshot> = dao.getAllTxnSnapshots()
    suspend fun insertTxnSnapshot(snapshot: TxnSnapshot) = dao.insertTxnSnapshot(snapshot)
}
