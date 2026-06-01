package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey val id: Long,
    val name: String,
    val ob: Double,
    val type: String,
    val color: String,
    val icon: String,
    val active: Boolean
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: Long,
    val type: String, // "income", "expense", "transfer"
    val name: String,
    val cat: String,
    val amt: Double,
    val date: String, // "YYYY-MM-DD"
    val accId: Long,
    val toAccId: Long?, // For transfers
    val note: String
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val id: Long,
    val cat: String,
    val limit: Double,
    val color: String
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey val id: Long,
    val name: String,
    val icon: String,
    val target: Double,
    val current: Double,
    val color: String
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey val id: Long,
    val name: String,
    val icon: String,
    val amt: Double,
    val due: String // "YYYY-MM-DD"
)

@Entity(tableName = "commitments")
data class Commitment(
    @PrimaryKey val id: Long,
    val name: String,
    val icon: String,
    val amt: Double,
    val due: String, // "YYYY-MM-DD"
    val freq: String, // "شهري", "ربع سنوي", etc.
    val acc: String // Account Name
)

@Entity(tableName = "recurring_transactions")
data class Recurring(
    @PrimaryKey val id: Long,
    val name: String,
    val type: String, // "income", "expense"
    val amt: Double,
    val freq: String, // "يومي", "أسبوعي", etc.
    val day: Int,
    val acc: String,
    val on: Boolean
)

@Entity(tableName = "calendar_notes")
data class CalendarNote(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val note: String
)

@Entity(tableName = "edit_history")
data class EditHistory(
    @PrimaryKey val id: Long, // timestamp
    val ts: Long,
    val action: String, // "add", "edit", "delete", "restore"
    val entity: String,
    val desc: String
)

@Entity(tableName = "settings")
data class UserSettings(
    @PrimaryKey val id: Long = 1, // Always 1
    val name: String,
    val email: String,
    val phone: String,
    val currency: String,
    val language: String,
    val theme: String, // "light", "dark"
    val notifBills: Boolean,
    val notifBudget: Boolean,
    val notifGoals: Boolean,
    val notifSummary: Boolean
)

@Entity(tableName = "settings_history")
data class SettingsHistory(
    @PrimaryKey val id: Long, // timestamp
    val ts: Long,
    val snapshotJson: String // Serialized Settings
)

@Entity(tableName = "txn_snapshots")
data class TxnSnapshot(
    @PrimaryKey val id: Long, // timestamp
    val ts: Long,
    val label: String,
    val txnsJson: String // Serialized List<Transaction>
)
