package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Init database layers
        val database = AppDatabase.getDatabase(this)
        val repository = FinanceRepository(database.financeDao())
        val factory = MainViewModelFactory(repository)

        setContent {
            val viewModel: MainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                // Wrap whole layout in RTL for Arabic Interface support
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        AppContainer(
                            vm = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContainer(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentPage by vm.currentPage.collectAsState()
    val isGoogleLoggedIn by vm.isGoogleLoggedIn.collectAsState()

    val accounts by vm.accounts.collectAsState()
    val txns by vm.transactions.collectAsState()
    val budgets by vm.budgets.collectAsState()
    val goals by vm.goals.collectAsState()
    val reminders by vm.reminders.collectAsState()
    val commitments by vm.commitments.collectAsState()
    val recurring by vm.recurring.collectAsState()
    val calendarNotes by vm.calendarNotes.collectAsState()
    val editHistory by vm.editHistory.collectAsState()
    val settings by vm.userSettings.collectAsState()
    val settingsHistory by vm.settingsHistory.collectAsState()
    val snapshots by vm.txnSnapshots.collectAsState()

    // Dialog state controllers
    var showTxnDialog by remember { mutableStateOf(false) }
    var selectedTxnForEdit by remember { mutableStateOf<Transaction?>(null) }

    var showAccDialog by remember { mutableStateOf(false) }
    var selectedAccForEdit by remember { mutableStateOf<Account?>(null) }

    var showBudgetDialog by remember { mutableStateOf(false) }
    var selectedBudgetForEdit by remember { mutableStateOf<Budget?>(null) }

    var showGoalDialog by remember { mutableStateOf(false) }
    var selectedGoalForEdit by remember { mutableStateOf<Goal?>(null) }

    var showReminderDialog by remember { mutableStateOf(false) }
    var selectedReminderForEdit by remember { mutableStateOf<Reminder?>(null) }

    var showCommitmentDialog by remember { mutableStateOf(false) }
    var selectedCommitmentForEdit by remember { mutableStateOf<Commitment?>(null) }

    var showRecurringDialog by remember { mutableStateOf(false) }
    var selectedRecurringForEdit by remember { mutableStateOf<Recurring?>(null) }

    var showCalNoteDialog by remember { mutableStateOf(false) }
    var selectedCalDate by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (currentPage == "google_login") {
        GoogleLoginScreen(onSuccessLogin = { name, email ->
            vm.performGoogleSignIn(name, email)
        })
    } else {
        // App Layout Scaffold with RTL Side Drawer Menu
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(280.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Logo Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFD0BCFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wallet,
                                    contentDescription = null,
                                    tint = Color(0xFF381E72),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(text = "مالي", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = "Personal Finance", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Divider()

                        // Navigation Panels grouped beautifully matching HTML exactly
                        NavigationSectionGroup("الرئيسية") {
                            NavigationMenuItem("لوحة القيادة", Icons.Default.Dashboard, currentPage == "dashboard") {
                                vm.navigateTo("dashboard")
                                scope.launch { drawerState.close() }
                            }
                            NavigationMenuItem("المؤشرات المالية", Icons.Default.PieChart, currentPage == "kpi") {
                                vm.navigateTo("kpi")
                                scope.launch { drawerState.close() }
                            }
                            NavigationMenuItem("توقع التدفق", Icons.Default.TrendingUp, currentPage == "forecast") {
                                vm.navigateTo("forecast")
                                scope.launch { drawerState.close() }
                            }
                            NavigationMenuItem("التقويم المالي", Icons.Default.CalendarToday, currentPage == "calendar") {
                                vm.navigateTo("calendar")
                                scope.launch { drawerState.close() }
                            }
                        }

                        NavigationSectionGroup("العمليات") {
                            NavigationMenuItem("العمليات المالية", Icons.Default.SwapHoriz, currentPage == "transactions") {
                                vm.navigateTo("transactions")
                                scope.launch { drawerState.close() }
                            }
                            NavigationMenuItem("الحسابات والمحافظ", Icons.Default.AccountBalanceWallet, currentPage == "accounts") {
                                vm.navigateTo("accounts")
                                scope.launch { drawerState.close() }
                            }
                            NavigationMenuItem("إدارة الأرصدة والاقسام", Icons.Default.Tune, currentPage == "accounts-mgmt") {
                                vm.navigateTo("accounts-mgmt")
                                scope.launch { drawerState.close() }
                            }
                            NavigationMenuItem("المعاملات المتكررة", Icons.Default.History, currentPage == "recurring") {
                                vm.navigateTo("recurring")
                                scope.launch { drawerState.close() }
                            }
                            NavigationMenuItem("الالتزامات المستقبلية", Icons.Default.Warning, currentPage == "commitments") {
                                vm.navigateTo("commitments")
                                scope.launch { drawerState.close() }
                            }
                        }

                        NavigationSectionGroup("التخطيط والادخار") {
                            NavigationMenuItem("الميزانية و السقف", Icons.Default.ListAlt, currentPage == "budget") {
                                vm.navigateTo("budget")
                                scope.launch { drawerState.close() }
                            }
                            NavigationMenuItem("الأهداف الاستثمارية", Icons.Default.Star, currentPage == "goals") {
                                vm.navigateTo("goals")
                                scope.launch { drawerState.close() }
                            }
                            NavigationMenuItem("صندوق الطوارئ الآمن", Icons.Default.Shield, currentPage == "emergency") {
                                vm.navigateTo("emergency")
                                scope.launch { drawerState.close() }
                            }
                            NavigationMenuItem("صافي الثروة", Icons.Default.AccountBalance, currentPage == "networth") {
                                vm.navigateTo("networth")
                                scope.launch { drawerState.close() }
                            }
                        }

                        NavigationSectionGroup("الإقرار والإدارة") {
                            NavigationMenuItem("التذكيرات والفواتير", Icons.Default.Notifications, currentPage == "reminders") {
                                vm.navigateTo("reminders")
                                scope.launch { drawerState.close() }
                            }
                            NavigationMenuItem("التحليلات الذكية", Icons.Default.Lightbulb, currentPage == "insights") {
                                vm.navigateTo("insights")
                                scope.launch { drawerState.close() }
                            }
                            NavigationMenuItem("التقارير و الصادرات", Icons.Default.Info, currentPage == "reports") {
                                vm.navigateTo("reports")
                                scope.launch { drawerState.close() }
                            }
                            NavigationMenuItem("السجل بنقاط الاسترجاع", Icons.Default.Refresh, currentPage == "history") {
                                vm.navigateTo("history")
                                scope.launch { drawerState.close() }
                            }
                            NavigationMenuItem("الإعدادات والملف", Icons.Default.Settings, currentPage == "settings") {
                                vm.navigateTo("settings")
                                scope.launch { drawerState.close() }
                            }
                        }

                        Divider()

                        // DarkTheme active toggle on Drawer Foot
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vm.toggleDarkTheme() }
                                .padding(8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Menu, contentDescription = null)
                                Text(text = "الوضع الداكن (السمة)", fontSize = 12.sp)
                            }
                            Switch(checked = vm.isDarkTheme.collectAsState().value, onCheckedChange = { vm.toggleDarkTheme() })
                        }
                    }
                }
            }
        ) {
            // Main Top Bar & Display View container
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Top App bar matching CSS styling header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "فتح القائمة", tint = Color(0xFFD0BCFF))
                        }
                        Text(
                            text = when(currentPage) {
                                "dashboard" -> "لوحة القيادة"
                                "kpi" -> "المؤشرات المالية"
                                "forecast" -> "توقع التدفق النقدي"
                                "calendar" -> "التقويم المالي"
                                "transactions" -> "العمليات المالية"
                                "accounts" -> "الحسابات"
                                "accounts-mgmt" -> "إدارة الحسابات"
                                "recurring" -> "المعاملات المتكررة"
                                "commitments" -> "الالترامات المستقبلية"
                                "budget" -> "الميزانية"
                                "goals" -> "الأهداف الاستثمارية"
                                "emergency" -> "صندوق الطوارئ"
                                "networth" -> "صافي الثروة"
                                "reminders" -> "تذكير الفواتير"
                                "insights" -> "التحليلات الذكية"
                                "reports" -> "التقارير المالية"
                                "history" -> "السجل وبطاقات الاسترجاع"
                                "settings" -> "إعدادات العميل"
                                else -> "لوحة مالي"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checklist status bar
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF4A4458))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF16A34A)))
                                Text(text = "محفوظ تلقائياً", fontSize = 10.sp, color = Color(0xFFD0BCFF), fontWeight = FontWeight.Bold)
                            }
                        }

                        // Notifications indicator popup mock trigger
                        IconButton(onClick = { vm.navigateTo("reminders") }) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Gray)
                        }

                        // Google Avatar circular letter matching settings
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD0BCFF))
                                .clickable { vm.navigateTo("settings") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (settings?.name ?: "B").take(1).uppercase(),
                                color = Color(0xFF381E72),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Workspace area drawing pages
                Box(modifier = Modifier.weight(1f)) {
                    when(currentPage) {
                        "dashboard" -> DashboardScreen(vm, accounts, txns, reminders, goals, commitments)
                        "kpi" -> KPIScreen(vm, accounts, txns, commitments)
                        "forecast" -> ForecastScreen(vm, accounts, txns, commitments)
                        "calendar" -> CalendarScreen(vm, calendarNotes, onAddNote = {
                            selectedCalDate = it
                            showCalNoteDialog = true
                        })
                        "transactions" -> TransactionsScreen(vm, txns, accounts, onEditTxn = {
                            selectedTxnForEdit = it
                            showTxnDialog = true
                        }, onAddNew = {
                            selectedTxnForEdit = null
                            showTxnDialog = true
                        })
                        "accounts" -> AccountsScreen(accounts, txns, onAddNew = {
                            selectedAccForEdit = null
                            showAccDialog = true
                        }, onEdit = {
                            selectedAccForEdit = it
                            showAccDialog = true
                        })
                        "accounts-mgmt" -> AccountMgmtScreen(vm, accounts, txns, onEdit = {
                            selectedAccForEdit = it
                            showAccDialog = true
                        }, onDelete = {
                            vm.deleteAccount(it.id, it.name)
                        })
                        "recurring" -> RecurringScreen(vm, recurring, onAdd = {
                            selectedRecurringForEdit = it
                            showRecurringDialog = true
                        })
                        "commitments" -> CommitmentsScreen(vm, commitments, onAdd = {
                            selectedCommitmentForEdit = it
                            showCommitmentDialog = true
                        })
                        "budget" -> BudgetScreen(vm, budgets, txns, onAddBudget = {
                            selectedBudgetForEdit = it
                            showBudgetDialog = true
                        })
                        "goals" -> GoalsScreen(vm, goals, onGoal = {
                            selectedGoalForEdit = it
                            showGoalDialog = true
                        })
                        "emergency" -> EmergencyFundScreen(vm, accounts, txns)
                        "networth" -> NetWorthScreen(vm, accounts, txns)
                        "reminders" -> RemindersScreen(vm, reminders, onAdd = {
                            selectedReminderForEdit = it
                            showReminderDialog = true
                        })
                        "insights" -> InsightsScreen(vm, accounts, txns)
                        "reports" -> ReportsScreen(vm, accounts, txns)
                        "history" -> HistoryScreen(vm, editHistory, settingsHistory, snapshots)
                        "settings" -> SettingsScreen(vm, settings)
                    }
                }
            }
        }
    }

    // Modal dialog trigger boxes matching core user flows
    if (showTxnDialog) {
        TransactionDialog(
            txn = selectedTxnForEdit,
            accounts = accounts,
            onDismiss = { showTxnDialog = false },
            onSave = { type, name, amt, date, accId, toAccId, cat, note ->
                vm.saveTransaction(selectedTxnForEdit?.id, type, name, cat, amt, date, accId, toAccId, note)
                showTxnDialog = false
            },
            onDelete = {
                selectedTxnForEdit?.let { vm.deleteTransaction(it.id, it.name) }
                showTxnDialog = false
            }
        )
    }

    if (showAccDialog) {
        AccountDialog(
            acc = selectedAccForEdit,
            onDismiss = { showAccDialog = false },
            onSave = { name, type, ob, color, icon ->
                vm.saveAccount(selectedAccForEdit?.id, name, type, ob, color, icon)
                showAccDialog = false
            }
        )
    }

    if (showBudgetDialog) {
        BudgetDialog(
            budget = selectedBudgetForEdit,
            onDismiss = { showBudgetDialog = false },
            onSave = { cat, limit, color ->
                vm.saveBudget(selectedBudgetForEdit?.id, cat, limit, color)
                showBudgetDialog = false
            }
        )
    }

    if (showGoalDialog) {
        GoalDialog(
            goal = selectedGoalForEdit,
            onDismiss = { showGoalDialog = false },
            onSave = { name, icon, target, current, color ->
                vm.saveGoal(selectedGoalForEdit?.id, name, icon, target, current, color)
                showGoalDialog = false
            }
        )
    }

    if (showReminderDialog) {
        ReminderDialog(
            reminder = selectedReminderForEdit,
            onDismiss = { showReminderDialog = false },
            onSave = { name, icon, amt, due ->
                vm.saveReminder(selectedReminderForEdit?.id, name, icon, amt, due)
                showReminderDialog = false
            }
        )
    }

    if (showCommitmentDialog) {
        CommitmentDialog(
            commitment = selectedCommitmentForEdit,
            accounts = accounts,
            onDismiss = { showCommitmentDialog = false },
            onSave = { name, icon, amt, due, freq, acc ->
                vm.saveCommitment(selectedCommitmentForEdit?.id, name, icon, amt, due, freq, acc)
                showCommitmentDialog = false
            }
        )
    }

    if (showRecurringDialog) {
        RecurringDialog(
            rec = selectedRecurringForEdit,
            accounts = accounts,
            onDismiss = { showRecurringDialog = false },
            onSave = { name, type, amt, freq, day, acc ->
                vm.saveRecurring(selectedRecurringForEdit?.id, name, type, amt, freq, day, acc)
                showRecurringDialog = false
            }
        )
    }

    if (showCalNoteDialog) {
        CalendarNoteDialog(
            date = selectedCalDate,
            currentNote = calendarNotes.find { it.date == selectedCalDate }?.note ?: "",
            onDismiss = { showCalNoteDialog = false },
            onSave = { note ->
                vm.saveCalendarNote(selectedCalDate, note)
                showCalNoteDialog = false
            }
        )
    }
}

@Composable
fun NavigationSectionGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
        content()
    }
}

@Composable
fun NavigationMenuItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFFD0BCFF) else Color.Transparent)
            .clickable { onClick() }
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color(0xFF381E72) else Color.Gray,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            color = if (isSelected) Color(0xFF381E72) else MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// SIMULATED GOOGLE SIGN-IN SCREEN
@Composable
fun GoogleLoginScreen(onSuccessLogin: (String, String) -> Unit) {
    var showAccountsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(360.dp)
                .padding(20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
            border = BorderStroke(1.dp, Color(0xFF4A4458))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Wallet Symbol
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFD0BCFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallet,
                        contentDescription = null,
                        tint = Color(0xFF381E72),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = "مالي الشخصي",
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    color = Color.White
)

                Text(
                    text = "مدير الميزانيات والنقدية الذكي للهواتف",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated Interactive button
                Button(
                    onClick = { showAccountsDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF), contentColor = Color(0xFF381E72)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color(0xFF4285F4)
                        )
                        Text(text = "تسجيل الدخول باستخدام Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showAccountsDialog) {
        Dialog(onDismissRequest = { showAccountsDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(text = "اختر حساب Google للمتابعة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Divider()

                    // Selection Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAccountsDialog = false
                                onSuccessLogin("Bichoy Karam", "bichoykaram@gmail.com")
                            }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD0BCFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "B", color = Color(0xFF381E72), fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "Bichoy Karam", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "bichoykaram@gmail.com", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                        Text(text = "إضافة حساب آخر", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ---------------- DIALOG MODALS DEFINITIONS ----------------

@Composable
fun TransactionDialog(
    txn: Transaction?,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onSave: (type: String, name: String, amt: Double, date: String, accId: Long, toAccId: Long?, cat: String, note: String) -> Unit,
    onDelete: () -> Unit
) {
    var type by remember { mutableStateOf(txn?.type ?: "expense") }
    var amt by remember { mutableStateOf(txn?.amt?.toString() ?: "") }
    var date by remember { mutableStateOf(txn?.date ?: "2025-06-13") }
    var accId by remember { mutableStateOf(txn?.accId ?: accounts.firstOrNull()?.id ?: 1L) }
    var toAccId by remember { mutableStateOf(txn?.toAccId ?: accounts.getOrNull(1)?.id) }
    var cat by remember { mutableStateOf(txn?.cat ?: CATEGORIES.first()) }
    var note by remember { mutableStateOf(txn?.note ?: "") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (txn == null) "إضافة عملية مالية" else "تعديل العملية المالية",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                // Selectors Tabs styled identically to HTML
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("income", "expense", "transfer").forEach { t ->
                        val label = when(t) {
                            "income" -> "دخل"
                            "expense" -> "مصروف"
                            else -> "تحويل"
                        }
                        val isActive = type == t
                        Button(
                            onClick = { type = t },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) Color(0xFFD0BCFF) else Color.LightGray.copy(alpha = 0.3f),
                                contentColor = if (isActive) Color(0xFF381E72) else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = amt,
                    onValueChange = { amt = it },
                    label = { Text("المبلغ (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("التاريخ (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Account Selection dropdown selectors
                Text(text = "الحساب", fontSize = 11.sp)
                accounts.forEach { acc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { accId = acc.id }
                            .background(if (accId == acc.id) Color(0xFF4A4458) else Color.Transparent)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RadioButton(selected = accId == acc.id, onClick = { accId = acc.id })
                        Text(text = acc.name, fontSize = 12.sp)
                    }
                }

                if (type == "transfer") {
                    Text(text = "إلى الحساب المصدر", fontSize = 11.sp)
                    accounts.forEach { acc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { toAccId = acc.id }
                                .background(if (toAccId == acc.id) Color(0xFF2B2930) else Color.Transparent)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RadioButton(selected = toAccId == acc.id, onClick = { toAccId = acc.id })
                            Text(text = acc.name, fontSize = 12.sp)
                        }
                    }
                }

                Text(text = "التصنيف", fontSize = 11.sp)
                // Linear grid chip selector for quick category picks
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CATEGORIES.forEach { category ->
                        FilterChip(
                            selected = cat == category,
                            onClick = { cat = category },
                            label = { Text(text = category, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("الوصف أو الملاحظة") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (txn != null) {
                        Button(onClick = { onDelete() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                            Text(text = "حذف", fontSize = 11.sp, color = Color(0xFF381E72))
                        }
                    } else Spacer(modifier = Modifier.width(1.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { onDismiss() }) { Text(text = "إلغاء", fontSize = 11.sp) }
                        Button(
                            onClick = {
                                val amountVal = amt.toDoubleOrNull() ?: 0.0
                                onSave(type, note.ifEmpty { cat }, amountVal, date, accId, if (type == "transfer") toAccId else null, cat, note)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                        ) {
                            Text(text = "حفظ", fontSize = 11.sp, color = Color(0xFF381E72))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountDialog(
    acc: Account?,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, ob: Double, color: String, icon: String) -> Unit
) {
    var name by remember { mutableStateOf(acc?.name ?: "") }
    var type by remember { mutableStateOf(acc?.type ?: "نقد") }
    var ob by remember { mutableStateOf(acc?.ob?.toString() ?: "0.0") }
    var color by remember { mutableStateOf(acc?.color ?: "#2D6A4F") }

    val colorsHex = listOf("#2D6A4F", "#1D4ED8", "#7C3AED", "#DC2626", "#D97706", "#0F766E")

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = if (acc == null) "إضافة حساب" else "تعديل حساب", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الحساب") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("نوع الحساب") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ob,
                    onValueChange = { ob = it },
                    label = { Text("الرصيد الافتتاحي") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "تبويب لون الحساب", fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorsHex.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .clickable { color = hex }
                                .border(
                                    2.dp,
                                    if (color == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    CircleShape
                                )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { onDismiss() }) { Text(text = "إلغاء", fontSize = 11.sp) }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { onSave(name, type, ob.toDoubleOrNull() ?: 0.0, color, "fa-university") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                    ) {
                        Text(text = "حفظ", fontSize = 11.sp, color = Color(0xFF381E72))
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetDialog(
    budget: Budget?,
    onDismiss: () -> Unit,
    onSave: (cat: String, limit: Double, color: String) -> Unit
) {
    var cat by remember { mutableStateOf(budget?.cat ?: "🏠 المنزل") }
    var limit by remember { mutableStateOf(budget?.limit?.toString() ?: "1000") }
    var color by remember { mutableStateOf(budget?.color ?: "#2D6A4F") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "الحد الأقصى للميزانية", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = cat,
                    onValueChange = { cat = it },
                    label = { Text("الفئة") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = limit,
                    onValueChange = { limit = it },
                    label = { Text("الحد الأقصى (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = { onDismiss() }) { Text(text = "إلغاء", fontSize = 11.sp) }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { onSave(cat, limit.toDoubleOrNull() ?: 0.0, color) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                    ) {
                        Text(text = "حفظ", fontSize = 11.sp, color = Color(0xFF381E72))
                    }
                }
            }
        }
    }
}

@Composable
fun GoalDialog(
    goal: Goal?,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, target: Double, current: Double, color: String) -> Unit
) {
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var icon by remember { mutableStateOf(goal?.icon ?: "🎯") }
    var target by remember { mutableStateOf(goal?.target?.toString() ?: "") }
    var current by remember { mutableStateOf(goal?.current?.toString() ?: "0.0") }
    var color by remember { mutableStateOf(goal?.color ?: "#2D6A4F") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "جدولة هدف ادخاري", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الهدف") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text("الأيقونة (إيموجي)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("المبلغ المستهدف (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = { Text("المبلغ الحالي") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = { onDismiss() }) { Text(text = "إلغاء", fontSize = 11.sp) }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { onSave(name, icon, target.toDoubleOrNull() ?: 100.0, current.toDoubleOrNull() ?: 0.0, color) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                    ) {
                        Text(text = "حفظ", fontSize = 11.sp, color = Color(0xFF381E72))
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderDialog(
    reminder: Reminder?,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, amt: Double, due: String) -> Unit
) {
    var name by remember { mutableStateOf(reminder?.name ?: "") }
    var amt by remember { mutableStateOf(reminder?.amt?.toString() ?: "") }
    var due by remember { mutableStateOf(reminder?.due ?: "2025-06-13") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "إضافة تذكير سداد", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amt,
                    onValueChange = { amt = it },
                    label = { Text("قيمة الدفع (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = due,
                    onValueChange = { due = it },
                    label = { Text("تاريخ السداد (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = { onDismiss() }) { Text(text = "إلغاء", fontSize = 11.sp) }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { onSave(name, "🔌", amt.toDoubleOrNull() ?: 0.0, due) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                    ) {
                        Text(text = "حفظ", fontSize = 11.sp, color = Color(0xFF381E72))
                    }
                }
            }
        }
    }
}

@Composable
fun CommitmentDialog(
    commitment: Commitment?,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, amt: Double, due: String, freq: String, acc: String) -> Unit
) {
    var name by remember { mutableStateOf(commitment?.name ?: "") }
    var amt by remember { mutableStateOf(commitment?.amt?.toString() ?: "") }
    var due by remember { mutableStateOf(commitment?.due ?: "2025-06-13") }
    var freq by remember { mutableStateOf(commitment?.freq ?: "شهري") }
    var accSelected by remember { mutableStateOf(commitment?.acc ?: accounts.firstOrNull()?.name ?: "") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Text(text = "تعديل التزام مالي", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amt,
                    onValueChange = { amt = it },
                    label = { Text("المبلغ (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = due,
                    onValueChange = { due = it },
                    label = { Text("تاريخ الاستحقاق") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = freq,
                    onValueChange = { freq = it },
                    label = { Text("دورية التكرار") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "حساب السداد", fontSize = 11.sp)
                accounts.forEach { acc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { accSelected = acc.name }
                            .background(if (accSelected == acc.name) Color(0xFF4A4458) else Color.Transparent)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = accSelected == acc.name, onClick = { accSelected = acc.name })
                        Text(text = acc.name, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = { onDismiss() }) { Text(text = "إلغاء", fontSize = 11.sp) }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { onSave(name, "🛡️", amt.toDoubleOrNull() ?: 0.0, due, freq, accSelected) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                    ) {
                        Text(text = "حفظ", fontSize = 11.sp, color = Color(0xFF381E72))
                    }
                }
            }
        }
    }
}

@Composable
fun RecurringDialog(
    rec: Recurring?,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, amt: Double, freq: String, day: Int, acc: String) -> Unit
) {
    var name by remember { mutableStateOf(rec?.name ?: "") }
    var type by remember { mutableStateOf(rec?.type ?: "expense") }
    var amt by remember { mutableStateOf(rec?.amt?.toString() ?: "") }
    var freq by remember { mutableStateOf(rec?.freq ?: "شهري") }
    var day by remember { mutableStateOf(rec?.day?.toString() ?: "1") }
    var accSelected by remember { mutableStateOf(rec?.acc ?: accounts.firstOrNull()?.name ?: "") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Text(text = "معاملة مجدولة دورياً", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { type = "income" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == "income") Color(0xFF16A34A) else Color.LightGray)
                    ) {
                        Text(text = "إيراد", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { type = "expense" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == "expense") Color(0xFFEF4444) else Color.LightGray)
                    ) {
                        Text(text = "مصروف", fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = amt,
                    onValueChange = { amt = it },
                    label = { Text("المبلغ (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = freq,
                    onValueChange = { freq = it },
                    label = { Text("دورة التكرار") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = day,
                    onValueChange = { day = it },
                    label = { Text("يوم الاستحقاق الفعلي (1 - 30)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "حساب الدفع / الاستقبال", fontSize = 11.sp)
                accounts.forEach { acc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { accSelected = acc.name }
                            .background(if (accSelected == acc.name) Color(0xFF4A4458) else Color.Transparent)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = accSelected == acc.name, onClick = { accSelected = acc.name })
                        Text(text = acc.name, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = { onDismiss() }) { Text(text = "إلغاء", fontSize = 11.sp) }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { onSave(name, type, amt.toDoubleOrNull() ?: 0.0, freq, day.toIntOrNull() ?: 1, accSelected) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                    ) {
                        Text(text = "حفظ", fontSize = 11.sp, color = Color(0xFF381E72))
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarNoteDialog(
    date: String,
    currentNote: String,
    onDismiss: () -> Unit,
    onSave: (note: String) -> Unit
) {
    var noteText by remember { mutableStateOf(currentNote) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "إضافة ملاحظة ليوم $date", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    placeholder = { Text(text = "اكتب ملاحظتك للتاريخ...") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = { onDismiss() }) { Text(text = "إلغاء", fontSize = 11.sp) }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { onSave(noteText) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                    ) {
                        Text(text = "حفظ الملاحظة", fontSize = 11.sp, color = Color(0xFF381E72))
                    }
                }
            }
        }
    }
}
