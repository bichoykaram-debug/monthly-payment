package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    // Accounts
    @Query("SELECT * FROM accounts")
    fun getAllAccountsFlow(): Flow<List<Account>>

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<Account>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<Account>)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)

    @Query("DELETE FROM accounts")
    suspend fun clearAccounts()


    // Transactions
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactionsFlow(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    suspend fun getAllTransactions(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()


    // Budgets
    @Query("SELECT * FROM budgets")
    fun getAllBudgetsFlow(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgets(): List<Budget>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<Budget>)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: Long)

    @Query("DELETE FROM budgets")
    suspend fun clearBudgets()


    // Goals
    @Query("SELECT * FROM goals")
    fun getAllGoalsFlow(): Flow<List<Goal>>

    @Query("SELECT * FROM goals")
    suspend fun getAllGoals(): List<Goal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<Goal>)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)

    @Query("DELETE FROM goals")
    suspend fun clearGoals()


    // Reminders
    @Query("SELECT * FROM reminders ORDER BY due ASC")
    fun getAllRemindersFlow(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders ORDER BY due ASC")
    suspend fun getAllReminders(): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<Reminder>)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("DELETE FROM reminders")
    suspend fun clearReminders()


    // Commitments
    @Query("SELECT * FROM commitments ORDER BY due ASC")
    fun getAllCommitmentsFlow(): Flow<List<Commitment>>

    @Query("SELECT * FROM commitments ORDER BY due ASC")
    suspend fun getAllCommitments(): List<Commitment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitment(commitment: Commitment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitments(commitments: List<Commitment>)

    @Query("DELETE FROM commitments WHERE id = :id")
    suspend fun deleteCommitmentById(id: Long)

    @Query("DELETE FROM commitments")
    suspend fun clearCommitments()


    // Recurring
    @Query("SELECT * FROM recurring_transactions")
    fun getAllRecurringFlow(): Flow<List<Recurring>>

    @Query("SELECT * FROM recurring_transactions")
    suspend fun getAllRecurring(): List<Recurring>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(recurring: Recurring)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurrings(recurrings: List<Recurring>)

    @Query("DELETE FROM recurring_transactions WHERE id = :id")
    suspend fun deleteRecurringById(id: Long)

    @Query("DELETE FROM recurring_transactions")
    suspend fun clearRecurrings()


    // Calendar Notes
    @Query("SELECT * FROM calendar_notes")
    fun getAllCalendarNotesFlow(): Flow<List<CalendarNote>>

    @Query("SELECT * FROM calendar_notes")
    suspend fun getAllCalendarNotes(): List<CalendarNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarNote(note: CalendarNote)

    @Query("DELETE FROM calendar_notes WHERE date = :date")
    suspend fun deleteCalendarNoteByDate(date: String)

    @Query("DELETE FROM calendar_notes")
    suspend fun clearCalendarNotes()


    // Edit History
    @Query("SELECT * FROM edit_history ORDER BY id DESC")
    fun getAllEditHistoriesFlow(): Flow<List<EditHistory>>

    @Query("SELECT * FROM edit_history ORDER BY id DESC")
    suspend fun getAllEditHistories(): List<EditHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEditHistory(history: EditHistory)

    @Query("DELETE FROM edit_history")
    suspend fun clearEditHistories()


    // Settings
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettingsFlow(): Flow<UserSettings?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettings(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettings)


    // Settings History
    @Query("SELECT * FROM settings_history ORDER BY id DESC")
    fun getAllSettingsHistoriesFlow(): Flow<List<SettingsHistory>>

    @Query("SELECT * FROM settings_history ORDER BY id DESC")
    suspend fun getAllSettingsHistories(): List<SettingsHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettingsHistory(history: SettingsHistory)

    @Query("DELETE FROM settings_history")
    suspend fun clearSettingsHistories()


    // Transaction Snapshots
    @Query("SELECT * FROM txn_snapshots ORDER BY id DESC")
    fun getAllTxnSnapshotsFlow(): Flow<List<TxnSnapshot>>

    @Query("SELECT * FROM txn_snapshots ORDER BY id DESC")
    suspend fun getAllTxnSnapshots(): List<TxnSnapshot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTxnSnapshot(snapshot: TxnSnapshot)

    @Query("DELETE FROM txn_snapshots")
    suspend fun clearTxnSnapshots()
}

@Database(
    entities = [
        Account::class,
        Transaction::class,
        Budget::class,
        Goal::class,
        Reminder::class,
        Commitment::class,
        Recurring::class,
        CalendarNote::class,
        EditHistory::class,
        UserSettings::class,
        SettingsHistory::class,
        TxnSnapshot::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
