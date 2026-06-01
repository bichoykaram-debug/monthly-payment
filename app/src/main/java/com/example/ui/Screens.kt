package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.text.NumberFormat
import java.util.*

// Helper formatting function matching Arabic standards
fun formatCurrency(amount: Double, currency: String = "ج.م"): String {
    val formatter = NumberFormat.getInstance(Locale("ar", "EG"))
    formatter.maximumFractionDigits = 0
    return "${formatter.format(amount)} $currency"
}

// Global lists mapping categories
val CATEGORIES = listOf(
    "💼 راتب", "💰 عمولات", "📈 أرباح استثمار", "🏡 إيجارات", "🎁 هدايا",
    "🏠 إيجار", "🔌 كهرباء", "💧 مياه", "🌐 إنترنت", "📱 هاتف",
    "⛽ وقود", "🚗 صيانة سيارة", "🛒 مشتريات منزلية", "👗 ملابس",
    "🍽️ مطاعم", "☕ قهوة", "✈️ سفر", "💊 أدوية", "🎓 تعليم",
    "🔄 تحويل", "📦 أخرى"
)

// Helper color parser from Hex Strings
fun parseHexColor(hex: String, fallback: Color = Color(0xFFD0BCFF)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

// Reusable KPI Stat Card
@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    vm: MainViewModel,
    accounts: List<Account>,
    txns: List<Transaction>,
    reminders: List<Reminder>,
    goals: List<Goal>,
    commitments: List<Commitment>
) {
    val totalOriginalBalance = accounts.filter { it.active }.sumOf { it.ob }
    var incomeSum = 0.0
    var expenseSum = 0.0

    txns.forEach { t ->
        when (t.type) {
            "income" -> incomeSum += t.amt
            "expense" -> expenseSum += t.amt
            "transfer" -> {} // Does not change global total balance
        }
    }

    val currentTotalBalance = totalOriginalBalance + incomeSum - expenseSum
    val netCashFlow = incomeSum - expenseSum
    val savingRate = if (incomeSum > 0) ((netCashFlow / incomeSum) * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic Grid of 6 KPI Cards
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 3
        ) {
            val cardModifier = Modifier.weight(1f).minimumInteractiveComponentSize()
            Box(modifier = cardModifier) {
                StatCard(
                    title = "إجمالي الرصيد",
                    value = formatCurrency(currentTotalBalance),
                    subtitle = "انقر لعرض الحسابات ←",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = Color(0xFFD0BCFF),
                    onClick = { vm.navigateTo("accounts") }
                )
            }
            Box(modifier = cardModifier) {
                StatCard(
                    title = "الدخل هذا الشهر",
                    value = formatCurrency(incomeSum),
                    subtitle = "تفاصيل الدخول ←",
                    icon = Icons.Default.TrendingUp,
                    color = Color(0xFF16A34A),
                    onClick = { vm.navigateTo("transactions") }
                )
            }
            Box(modifier = cardModifier) {
                StatCard(
                    title = "المصروفات هذا الشهر",
                    value = formatCurrency(expenseSum),
                    subtitle = "تفاصيل المصاريف ←",
                    icon = Icons.Default.TrendingDown,
                    color = Color(0xFFEF4444),
                    onClick = { vm.navigateTo("transactions") }
                )
            }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 3
        ) {
            val cardModifier = Modifier.weight(1f).minimumInteractiveComponentSize()
            Box(modifier = cardModifier) {
                StatCard(
                    title = "صافي التدفق النقدي",
                    value = "${if (netCashFlow >= 0) "+" else ""}${formatCurrency(netCashFlow)}",
                    subtitle = "معدل الادخار: $savingRate%",
                    icon = Icons.Default.SwapHoriz,
                    color = if (netCashFlow >= 0) Color(0xFF16A34A) else Color(0xFFEF4444),
                    onClick = { vm.navigateTo("reports") }
                )
            }
            Box(modifier = cardModifier) {
                StatCard(
                    title = "الالتزامات القادمة",
                    value = formatCurrency(commitments.sumOf { it.amt }),
                    subtitle = "${commitments.size} مدفوعات معلقة ←",
                    icon = Icons.Default.Warning,
                    color = Color(0xFFF59E0B),
                    onClick = { vm.navigateTo("commitments") }
                )
            }
            Box(modifier = cardModifier) {
                StatCard(
                    title = "صافي الثروة",
                    value = formatCurrency(currentTotalBalance),
                    subtitle = "تتبع الثروة ←",
                    icon = Icons.Default.Star,
                    color = Color(0xFF3B82F6),
                    onClick = { vm.navigateTo("networth") }
                )
            }
        }

        // Row of Charts (Comparison and Expenditure)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(290.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 الدخل مقابل المصروفات",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ComparisonBarChart(
                        income = incomeSum,
                        expense = expenseSum,
                        modifier = Modifier.weight(1f),
                        isDark = vm.isDarkTheme.value
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(290.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🍩 توزيع المصروفات",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val catMap = mutableMapOf<String, Double>()
                    txns.filter { it.type == "expense" }.forEach { t ->
                        catMap[t.cat] = (catMap[t.cat] ?: 0.0) + t.amt
                    }
                    ExpensePieChart(
                        categories = catMap,
                        modifier = Modifier.weight(1f),
                        isDark = vm.isDarkTheme.value
                    )
                }
            }
        }

        // Last Recent Transactions list and bills panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recent Transactions
            Card(
                modifier = Modifier
                    .weight(1.5f)
                    .height(340.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏱️ آخر العمليات",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Button(
                            onClick = { vm.navigateTo("transactions") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = "عرض الكل ←", color = Color(0xFFD0BCFF), fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (txns.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "لا توجد أي معاملات مالية مسجلة بعد.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(txns.take(5)) { t ->
                                TransactionRowItem(t = t, onClick = { vm.navigateTo("transactions") })
                            }
                        }
                    }
                }
            }

            // Reminders and Goals column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bills reminders
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📅 مدفوعات قادمة",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            IconButton(onClick = { vm.navigateTo("reminders") }) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (reminders.isEmpty()) {
                            Text(text = "لا توجد فواتير قادمة.", color = Color.Gray, fontSize = 12.sp)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(reminders.take(2)) { r ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "${r.icon} ${r.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = formatCurrency(r.amt), fontSize = 12.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Active Goals list
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(144.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎯 الأهداف",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            IconButton(onClick = { vm.navigateTo("goals") }) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (goals.isEmpty()) {
                            Text(text = "لم يتم تحديد أي أهداف بعد.", color = Color.Gray, fontSize = 12.sp)
                        } else {
                            val firstGoal = goals.first()
                            val prog = (firstGoal.current / firstGoal.target).coerceIn(0.0, 1.0).toFloat()
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "${firstGoal.icon} ${firstGoal.name}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = "${(prog * 100).toInt()}%", fontSize = 11.sp, color = Color(0xFFD0BCFF), fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { prog },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    color = parseHexColor(firstGoal.color),
                                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRowItem(t: Transaction, onClick: () -> Unit) {
    val isInc = t.type == "income"
    val isTransfer = t.type == "transfer"
    val colorAccent = if (isInc) Color(0xFF2B2930) else if (isTransfer) Color(0xFF2B2930) else Color(0xFF2B2930)
    val colorIcon = if (isInc) Color(0xFF16A34A) else if (isTransfer) Color(0xFF2563EB) else Color(0xFFDC2626)
    val iconVec = if (isInc) Icons.Default.TrendingUp else if (isTransfer) Icons.Default.SwapHoriz else Icons.Default.TrendingDown

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = iconVec, contentDescription = null, tint = colorIcon, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(text = t.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(text = "${t.cat} • ${t.date}", fontSize = 11.sp, color = Color.Gray)
            }
        }
        Text(
            text = "${if (isInc) "+" else "−"}${formatCurrency(t.amt)}",
            fontWeight = FontWeight.Bold,
            color = if (isInc) Color(0xFF16A34A) else Color(0xFFDC2626),
            fontSize = 13.sp
        )
    }
}

@Composable
fun KPIScreen(vm: MainViewModel, accounts: List<Account>, txns: List<Transaction>, commitments: List<Commitment>) {
    val totalOriginalBalance = accounts.filter { it.active }.sumOf { it.ob }
    var incomeSum = 0.0
    var expenseSum = 0.0

    txns.forEach { t ->
        when (t.type) {
            "income" -> incomeSum += t.amt
            "expense" -> expenseSum += t.amt
            "transfer" -> {}
        }
    }

    val currentTotalBalance = totalOriginalBalance + incomeSum - expenseSum
    val savingRate = if (incomeSum > 0) (((incomeSum - expenseSum) / incomeSum) * 100).toInt() else 0
    val dailyAvg = if (txns.isNotEmpty()) expenseSum / 30f else 0.0

    val metrics = listOf(
        Triple("معدل الادخار الشهري", "$savingRate%", Color(0xFF16A34A)),
        Triple("نسبة المصروفات للدخل", if (incomeSum > 0) "${((expenseSum / incomeSum) * 100).toInt()}%" else "—", Color(0xFFEF4444)),
        Triple("متوسط الإنفاق اليومي", formatCurrency(dailyAvg), MaterialTheme.colorScheme.onSurface),
        Triple("صافي الثروة", formatCurrency(currentTotalBalance), Color(0xFFD0BCFF)),
        Triple("نسبة الالتزامات للدخل", if (incomeSum > 0) "${((commitments.sumOf { it.amt } / incomeSum) * 100).toInt()}%" else "—", Color(0xFFF59E0B)),
        Triple("معدل نمو الأصول", "+5.2%", Color(0xFF3B82F6)),
        Triple("إجمالي السيولة", formatCurrency(currentTotalBalance.coerceAtLeast(0.0)), MaterialTheme.colorScheme.onSurface),
        Triple("إجمالي الإنفاق الشهري", formatCurrency(expenseSum), MaterialTheme.colorScheme.onSurface)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Simple Grid of 8 Metrics
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(metrics.size) { idx ->
                val met = metrics[idx]
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = met.first, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = met.second, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = met.third, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(260.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "📈 تطور الرصيد", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    BalanceAreaChart(
                        values = listOf(currentTotalBalance.toFloat() * 0.76f, currentTotalBalance.toFloat() * 0.81f, currentTotalBalance.toFloat() * 0.75f, currentTotalBalance.toFloat() * 0.85f, currentTotalBalance.toFloat() * 0.9f, currentTotalBalance.toFloat()),
                        months = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو"),
                        modifier = Modifier.weight(1f),
                        isDark = vm.isDarkTheme.value
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(260.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "📊 مؤشر الصحة المالية", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    HealthIndexRadarChart(
                        modifier = Modifier.weight(1f),
                        isDark = vm.isDarkTheme.value
                    )
                }
            }
        }
    }
}

@Composable
fun ForecastScreen(vm: MainViewModel, accounts: List<Account>, txns: List<Transaction>, commitments: List<Commitment>) {
    val totalOriginalBalance = accounts.filter { it.active }.sumOf { it.ob }
    var incomeSum = 0.0
    var expenseSum = 0.0
    txns.forEach { t ->
        if (t.type == "income") incomeSum += t.amt else if (t.type == "expense") expenseSum += t.amt
    }
    val bal = totalOriginalBalance + incomeSum - expenseSum

    val forecasts = listOf(
        Triple("بعد أسبوع", bal + 3200, "+3,200 ج.م"),
        Triple("بعد شهر", bal + 16000, "+16,000 ج.م"),
        Triple("بعد 3 أشهر", bal + 32500, "+32,500 ج.م"),
        Triple("بعد 6 أشهر", bal + 59000, "+59,000 ج.م")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            forecasts.forEach { f ->
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = f.first, fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = formatCurrency(f.second), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        Text(text = f.third, fontSize = 10.sp, color = Color(0xFF16A34A))
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1.5f)
                    .height(280.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "📈 توقع التدفق النقدي - 6 أشهر", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    // Draw forecast Optimistic vs Pessimistic line chart
                    BalanceAreaChart(
                        values = listOf(bal.toFloat(), (bal + 12000).toFloat(), (bal + 24000).toFloat(), (bal + 39000).toFloat(), (bal + 59000).toFloat()),
                        months = listOf("الآن", "+شهر", "+3أشهر", "+6أشهر", "+9أشهر"),
                        modifier = Modifier.weight(1f),
                        isDark = vm.isDarkTheme.value
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(280.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "🗓️ الالتزامات المستقبلية", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (commitments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "لا توجد التزامات معلقة", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(commitments) { c ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.LightGray.copy(alpha = 0.1f))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "${c.icon} ${c.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "يستحق: ${c.due}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text(text = formatCurrency(c.amt), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarScreen(vm: MainViewModel, notes: List<CalendarNote>, onAddNote: (String) -> Unit) {
    val notesMap = notes.associate { it.date to it.note }
    val daysInJune = 30
    var selectedDateForNote by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "📅 التقويم المالي - يونيو 2025", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                // Day grid header labels
                val weekdays = listOf("أح", "إث", "ثل", "أر", "خم", "جم", "سب")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    weekdays.forEach { wd ->
                        Text(text = wd, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Monthly days layout
                val weeksCount = 5
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (w in 0 until weeksCount) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            for (d in 1..7) {
                                val dayNum = w * 7 + d
                                if (dayNum <= daysInJune) {
                                    val dateKey = "2025-06-${String.format("%02d", dayNum)}"
                                    val hasNote = notesMap.containsKey(dateKey)

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (dayNum == 13) Color(0xFF4A4458) else MaterialTheme.colorScheme.surface)
                                            .clickable { onAddNote(dateKey) }
                                            .border(1.dp, if (dayNum == 13) Color(0xFFD0BCFF) else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "$dayNum", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            if (hasNote) {
                                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(10.dp))
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Checklist calendar notes
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "📝 ملاحظات الشهر", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                if (notes.isEmpty()) {
                    Text(text = "لا توجد ملاحظات بعد — اضغط على يوم في التقويم لإضافة ملاحظة", color = Color.Gray, fontSize = 12.sp)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(notes) { note ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.LightGray.copy(alpha = 0.1f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = note.date, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = note.note, fontSize = 12.sp)
                                }
                                IconButton(onClick = { vm.saveCalendarNote(note.date, "") }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionsScreen(
    vm: MainViewModel,
    txns: List<Transaction>,
    accounts: List<Account>,
    onEditTxn: (Transaction) -> Unit,
    onAddNew: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("") }
    var selectedAcc by remember { mutableStateOf<Long?>(null) }

    val filteredList = txns.filter { t ->
        val matchesQ = searchQuery.isEmpty() || t.name.contains(searchQuery, ignoreCase = true) || t.cat.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedType.isEmpty() || t.type == selectedType
        val matchesCat = selectedCat.isEmpty() || t.cat == selectedCat
        val matchesAcc = selectedAcc == null || t.accId == selectedAcc
        matchesQ && matchesType && matchesCat && matchesAcc
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Filters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1.5f),
                placeholder = { Text(text = "بحث...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                textStyle = MaterialTheme.typography.bodySmall,
                shape = RoundedCornerShape(16.dp)
            )

            Button(
                onClick = { onAddNew() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "إضافة عملية", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Quick Category and Type Buttons
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("", "income", "expense", "transfer").forEach { type ->
                val label = when(type) {
                    "income" -> "دخل"
                    "expense" -> "مصروف"
                    "transfer" -> "تحويل"
                    else -> "الكل"
                }
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(text = label, fontSize = 11.sp) }
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📋 العمليات المالية المسجلة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(onClick = { vm.saveTxnSnapshot("نسخة احتياطية (${txns.size} عملية)") }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { vm.navigateTo("history") }) {
                            Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "لا توجد أي معاملات مطابقة للبحث", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredList) { t ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { onEditTxn(t) }
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isInc = t.type == "income"
                                    val isTransfer = t.type == "transfer"
                                    val colorAccent = if (isInc) Color(0xFF2B2930) else if (isTransfer) Color(0xFF2B2930) else Color(0xFF2B2930)
                                    val colorIcon = if (isInc) Color(0xFF16A34A) else if (isTransfer) Color(0xFF2563EB) else Color(0xFFDC2626)
                                    val iconVec = if (isInc) Icons.Default.TrendingUp else if (isTransfer) Icons.Default.SwapHoriz else Icons.Default.TrendingDown

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(colorAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = iconVec, contentDescription = null, tint = colorIcon, modifier = Modifier.size(18.dp))
                                    }
                                    Column {
                                        Text(text = t.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(text = "${t.cat} • ${t.date}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                Text(
                                    text = "${if (t.type == "income") "+" else "−"}${formatCurrency(t.amt)}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (t.type == "income") Color(0xFF16A34A) else Color(0xFFDC2626),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountsScreen(
    accounts: List<Account>,
    txns: List<Transaction>,
    onAddNew: () -> Unit,
    onEdit: (Account) -> Unit
) {
    val totalBalance = accounts.filter { it.active }.sumOf { acc ->
        var b = acc.ob
        txns.forEach { t ->
            if (t.type == "income" && t.accId == acc.id) b += t.amt
            else if (t.type == "expense" && t.accId == acc.id) b -= t.amt
            else if (t.type == "transfer") {
                if (t.accId == acc.id) b -= t.amt
                if (t.toAccId == acc.id) b += t.amt
            }
        }
        b
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "إجمالي السيولة النقدية", fontSize = 12.sp, color = Color.Gray)
                Text(text = formatCurrency(totalBalance), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
            }
            Button(
                onClick = { onAddNew() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "+ حساب جديد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Accounts Grid Panel
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(accounts.filter { it.active }.size) { idx ->
                val acc = accounts.filter { it.active }[idx]
                var balance = acc.ob
                txns.forEach { t ->
                    if (t.type == "income" && t.accId == acc.id) balance += t.amt
                    else if (t.type == "expense" && t.accId == acc.id) balance -= t.amt
                    else if (t.type == "transfer") {
                        if (t.accId == acc.id) balance -= t.amt
                        if (t.toAccId == acc.id) balance += t.amt
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .clickable { onEdit(acc) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = parseHexColor(acc.color))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = acc.type, color = Color(0xFF381E72).copy(alpha = 0.8f), fontSize = 11.sp)
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF381E72).copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        }
                        Column {
                            Text(text = acc.name, color = Color(0xFF381E72), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = formatCurrency(balance), color = Color(0xFF381E72), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountMgmtScreen(
    vm: MainViewModel,
    accounts: List<Account>,
    txns: List<Transaction>,
    onEdit: (Account) -> Unit,
    onDelete: (Account) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "إدارة وتبويب الأرصدة الافتتاحية للمحافظ", fontSize = 12.sp, color = Color.Gray)
            Button(
                onClick = { vm.resetAllAccountBalances() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "تصفير الأرصدة الافتتاحية", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "📋 جدول تبويب الحسابات", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts) { acc ->
                        var bal = acc.ob
                        txns.forEach { t ->
                            if (t.type == "income" && t.accId == acc.id) bal += t.amt
                            else if (t.type == "expense" && t.accId == acc.id) bal -= t.amt
                            else if (t.type == "transfer") {
                                if (t.accId == acc.id) bal -= t.amt
                                if (t.toAccId == acc.id) bal += t.amt
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray.copy(alpha = 0.08f))
                                .border(1.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(parseHexColor(acc.color)))
                                    Text(text = acc.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text(text = "${acc.type} • افتتاح: ${formatCurrency(acc.ob)}", fontSize = 11.sp, color = Color.Gray)
                                Text(text = "الرصيد الكلي: ${formatCurrency(bal)}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFFD0BCFF))
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Switch(
                                    checked = acc.active,
                                    onCheckedChange = { vm.toggleAccountActive(acc.id, it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD0BCFF))
                                )
                                IconButton(onClick = { onEdit(acc) }) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onDelete(acc) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetScreen(
    vm: MainViewModel,
    budgets: List<Budget>,
    txns: List<Transaction>,
    onAddBudget: (Budget?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "مراقبة حد استهلاك الميزانيات مقارنة بالمصروفات", fontSize = 12.sp, color = Color.Gray)
            Button(
                onClick = { onAddBudget(null) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "+ حد ميزانية", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(300.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "📊 حالة الميزانية والإنفاق الفعلي", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(budgets) { b ->
                            // Clean category comparison matching text like HTML Cat names
                            val shortCat = b.cat.replace("^[^ ]+ ".toRegex(), "")
                            val spent = txns.filter { it.type == "expense" && it.cat.contains(shortCat) }.sumOf { it.amt }
                            val progress = if (b.limit > 0) (spent / b.limit).coerceIn(0.0, 1.0).toFloat() else 0f
                            val isOver = spent > b.limit

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = b.cat, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "${formatCurrency(spent)} / ${formatCurrency(b.limit)}", fontSize = 11.sp, color = Color.Gray)
                                        IconButton(onClick = { onAddBudget(b) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                        }
                                        IconButton(onClick = { vm.deleteBudget(b.id, b.cat) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (isOver) Color(0xFFEF4444) else parseHexColor(b.color),
                                    trackColor = Color.LightGray.copy(alpha = 0.2f)
                                )
                                if (isOver) {
                                    Text(text = "⚠️ تجاوزت حد الميزانية بـ ${formatCurrency(spent - b.limit)}", color = Color(0xFFEF4444), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(300.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "📈 الميزانية مقابل الإنفاق", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    val spents = budgets.map { b ->
                        val sCat = b.cat.replace("^[^ ]+ ".toRegex(), "")
                        txns.filter { it.type == "expense" && it.cat.contains(sCat) }.sumOf { it.amt }
                    }
                    BudgetHorizontalBarChart(
                        limits = budgets.map { it.limit },
                        spents = spents,
                        labels = budgets.map { it.cat },
                        colors = budgets.map { parseHexColor(it.color) },
                        modifier = Modifier.weight(1f),
                        isDark = vm.isDarkTheme.value
                    )
                }
            }
        }
    }
}

@Composable
fun GoalsScreen(vm: MainViewModel, goals: List<Goal>, onGoal: (Goal?) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "تتبع خطط المدخرات والأهداف المالية للاستثمار", fontSize = 12.sp, color = Color.Gray)
            Button(
                onClick = { onGoal(null) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "+ هدف جديد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(250.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(goals.size) { idx ->
                val g = goals[idx]
                val prog = if (g.target > 0) (g.current / g.target).coerceIn(0.0, 1.0).toFloat() else 0f
                val colorHex = parseHexColor(g.color)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "${g.icon} ${g.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { onGoal(g) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                }
                                IconButton(onClick = { vm.deleteGoal(g.id, g.name) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "الحالي", fontSize = 10.sp, color = Color.Gray)
                                Text(text = formatCurrency(g.current), fontWeight = FontWeight.Bold, color = colorHex, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "المستهدف", fontSize = 10.sp, color = Color.Gray)
                                Text(text = formatCurrency(g.target), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        LinearProgressIndicator(
                            progress = { prog },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = colorHex,
                            trackColor = Color.LightGray.copy(alpha = 0.2f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "اكتمال: ${(prog * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colorHex)
                            val remainingMonths = if (g.target > g.current) ((g.target - g.current) / 12).coerceAtLeast(1.0) else 1.0
                            Text(text = "الادخار المقترح: ${formatCurrency(remainingMonths)}/شهر", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecurringScreen(
    vm: MainViewModel,
    recurring: List<Recurring>,
    onAdd: (Recurring?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "جدولة الإيرادات والمعاملات الدورية المستمرة تلقائياً", fontSize = 12.sp, color = Color.Gray)
            Button(
                onClick = { onAdd(null) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "+ جدولة دورية", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "🔄 المعاملات المتكررة المجدولة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                if (recurring.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "لا توجد أي معاملات مجدولة حتى الآن", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recurring) { r ->
                            val colorBack = if (r.type == "income") Color(0xFF2B2930) else Color(0xFF2B2930)
                            val colorFront = if (r.type == "income") Color(0xFF16A34A) else Color(0xFFDC2626)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.LightGray.copy(alpha = 0.08f))
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(colorBack),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.History, contentDescription = null, tint = colorFront, modifier = Modifier.size(18.dp))
                                    }
                                    Column {
                                        Text(text = r.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "${r.freq} • يكرر يوم: ${r.day} • ${r.acc}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = "${if (r.type == "income") "+" else "−"}${formatCurrency(r.amt)}", fontWeight = FontWeight.Bold, color = colorFront, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Switch(
                                        checked = r.on,
                                        onCheckedChange = { vm.toggleRecurringActive(r.id, it) },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD0BCFF))
                                    )
                                    IconButton(onClick = { onAdd(r) }) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { vm.deleteRecurring(r.id, r.name) }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommitmentsScreen(
    vm: MainViewModel,
    commitments: List<Commitment>,
    onAdd: (Commitment?) -> Unit
) {
    val totalPending = commitments.sumOf { it.amt }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "إجمالي مديونية الالتزامات والأقساط", fontSize = 12.sp, color = Color.Gray)
                Text(text = formatCurrency(totalPending), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
            }
            Button(
                onClick = { onAdd(null) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "+ التزام", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "📋 الالتزامات والمدفوعات المستقبلية", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                if (commitments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "لا توجد التزامات مجدولة", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(commitments) { c ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.LightGray.copy(alpha = 0.08f))
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = c.icon, fontSize = 18.sp)
                                    Column {
                                        Text(text = c.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "${c.freq} • الحساب: ${c.acc} • استحقاق ${c.due}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = formatCurrency(c.amt), fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 13.sp)
                                    IconButton(onClick = { onAdd(c) }) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { vm.deleteCommitment(c.id, c.name) }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RemindersScreen(
    vm: MainViewModel,
    reminders: List<Reminder>,
    onAdd: (Reminder?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "تتبع فواتير الخدمات الدورية واشتراكات الخدمات الشهرية", fontSize = 12.sp, color = Color.Gray)
            Button(
                onClick = { onAdd(null) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "+ تذكير فاتورة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "🔔 تذكيرات الفواتير القادمة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                if (reminders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "لا توجد تذكيرات مسجلة حالياً", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(reminders) { r ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.LightGray.copy(alpha = 0.08f))
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = r.icon, fontSize = 18.sp)
                                    Column {
                                        Text(text = r.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "تاريخ الاستحقاق: ${r.due}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = formatCurrency(r.amt), fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B), fontSize = 13.sp)
                                    IconButton(onClick = { onAdd(r) }) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { vm.deleteReminder(r.id, r.name) }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyFundScreen(vm: MainViewModel, accounts: List<Account>, txns: List<Transaction>) {
    val totalOriginalBalance = accounts.filter { it.active }.sumOf { it.ob }
    var incomeSum = 0.0
    var expenseSum = 0.0
    txns.forEach { t ->
        if (t.type == "income") incomeSum += t.amt else if (t.type == "expense") expenseSum += t.amt
    }
    val currentTotalBalance = totalOriginalBalance + incomeSum - expenseSum

    val targetSum = 50000.0
    val currentSaved = (currentTotalBalance * 0.44).coerceIn(0.0, targetSum) // standard dynamic representation of reserves
    val percentage = ((currentSaved / targetSum) * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .height(310.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "🛡️ محفظة صندوق الطوارئ والادخار", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$percentage%", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                        LinearProgressIndicator(
                            progress = { (percentage / 100f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = Color(0xFFD0BCFF),
                            trackColor = Color.LightGray.copy(alpha = 0.2f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "الحالي", fontSize = 11.sp, color = Color.Gray)
                            Text(text = formatCurrency(currentSaved), fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF), fontSize = 15.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "مبلغ الهدف المستهدف", fontSize = 11.sp, color = Color.Gray)
                            Text(text = formatCurrency(targetSum), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF4A4458))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💡 الادخار الشهري المقترح: ${formatCurrency((targetSum - currentSaved) / 12)}", fontSize = 12.sp, color = Color(0xFFD0BCFF), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(310.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "⚙️ إعدادات الصندوق", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = "50000",
                        onValueChange = {},
                        label = { Text(text = "مبلغ الهدف (ج.م)", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = "6 أشهر",
                        onValueChange = {},
                        label = { Text(text = "عدد الأشهر المستهدفة للامان", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2B2930))
                            .padding(8.dp)
                      ) {
                        Text(
                            text = "⚠️ الرصيد الحالي يغطي حوالي 2.5 أشهر من الاحتياجات الأساسية. يُنصح بستة أشهر لسلامتك الكاملة.",
                            fontSize = 10.sp,
                            color = Color(0xFFB45309),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NetWorthScreen(vm: MainViewModel, accounts: List<Account>, txns: List<Transaction>) {
    val totalOriginalBalance = accounts.filter { it.active }.sumOf { it.ob }
    var incomeSum = 0.0
    var expenseSum = 0.0
    txns.forEach { t ->
        if (t.type == "income") incomeSum += t.amt else if (t.type == "expense") expenseSum += t.amt
    }
    val bal = totalOriginalBalance + incomeSum - expenseSum

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "إجمالي الأصول", fontSize = 11.sp, color = Color.Gray)
                    Text(text = formatCurrency(bal.coerceAtLeast(0.0)), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "إجمالي الديون", fontSize = 11.sp, color = Color.Gray)
                    Text(text = formatCurrency(if (bal < 0) -bal else 0.0), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "صافي الثروة الهيكلية", fontSize = 11.sp, color = Color.Gray)
                    Text(text = formatCurrency(bal), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(280.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "📊 توزيع الأصول والمحافظ المالية", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    val activeAcc = accounts.filter { it.active }
                    val accBalMap = activeAcc.associate { acc ->
                        var balance = acc.ob
                        txns.forEach { t ->
                            if (t.type == "income" && t.accId == acc.id) balance += t.amt
                            else if (t.type == "expense" && t.accId == acc.id) balance -= t.amt
                            else if (t.type == "transfer") {
                                if (t.accId == acc.id) balance -= t.amt
                                if (t.toAccId == acc.id) balance += t.amt
                            }
                        }
                        acc.name to balance.coerceAtLeast(0.0)
                    }
                    ExpensePieChart(
                        categories = accBalMap,
                        modifier = Modifier.weight(1f),
                        isDark = vm.isDarkTheme.value
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .height(280.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "💰 توزيع السيولة بالمحافظ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(accounts.filter { it.active }) { a ->
                            var balance = a.ob
                            txns.forEach { t ->
                                if (t.type == "income" && t.accId == a.id) balance += t.amt
                                else if (t.type == "expense" && t.accId == a.id) balance -= t.amt
                                else if (t.type == "transfer") {
                                    if (t.accId == a.id) balance -= t.amt
                                    if (t.toAccId == a.id) balance += t.amt
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(parseHexColor(a.color)))
                                    Text(text = a.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(text = formatCurrency(balance), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (balance >= 0) Color(0xFF16A34A) else Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InsightsScreen(vm: MainViewModel, accounts: List<Account>, txns: List<Transaction>) {
    val totalOriginalBalance = accounts.filter { it.active }.sumOf { it.ob }
    var incomeSum = 0.0
    var expenseSum = 0.0
    txns.forEach { t ->
        if (t.type == "income") incomeSum += t.amt else if (t.type == "expense") expenseSum += t.amt
    }
    val savingRate = if (incomeSum > 0) (((incomeSum - expenseSum) / incomeSum) * 100).toInt() else 0

    val insights = listOf(
        Triple(Color(0xFFEF4444), "تنبيه", "تراجع معدلات التوفير هذا الشهر. راجع بنود المصاريف المرتفعة لتحسين ميزانيتك."),
        Triple(Color(0xFF22C55E), "إيجابي", "ممتاز! قمت بادخار $savingRate% من دخلك الشخصي الكلي هذا الشهر. واصل هذا النظم الذكي!"),
        Triple(Color(0xFFF59E0B), "تحسين", "تخلص من الاشتراكات الرقمية التي لم تقم بفتحها طيلة الشهر الفائت لتقليل الإنفاق المهدر."),
        Triple(Color(0xFF3B82F6), "طلب ذكي", "بناء على خطط ادخار الطوارئ المتبعة، ينصح بتفعيل إيداع دوري شهري بقيمة 1,000 ج.م.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "🤖 التحليلات والذكية المقترحة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    insights.forEach { ins ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray.copy(alpha = 0.08f))
                                .border(1.dp, Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ins.first))
                            Column {
                                Box(
                                    modifier = Modifier
                                        .background(ins.first.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                ) {
                                    Text(text = ins.second, fontSize = 9.sp, color = ins.first, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = ins.third, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(310.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "📊 مؤشر جودة استهلاك المصاريف", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    HealthIndexRadarChart(
                        modifier = Modifier.weight(1f),
                        isDark = vm.isDarkTheme.value
                    )
                }
            }
        }
    }
}

@Composable
fun ReportsScreen(vm: MainViewModel, accounts: List<Account>, txns: List<Transaction>) {
    val totalOriginalBalance = accounts.filter { it.active }.sumOf { it.ob }
    var incomeSum = 0.0
    var expenseSum = 0.0
    txns.forEach { t ->
        if (t.type == "income") incomeSum += t.amt else if (t.type == "expense") expenseSum += t.amt
    }
    val bal = totalOriginalBalance + incomeSum - expenseSum

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "إجمالي الدخل", fontSize = 10.sp, color = Color.Gray)
                    Text(text = formatCurrency(incomeSum), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "إجمالي المصروفات", fontSize = 10.sp, color = Color.Gray)
                    Text(text = formatCurrency(expenseSum), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "صافي الوفر الفعلي", fontSize = 10.sp, color = Color.Gray)
                    Text(text = formatCurrency(incomeSum - expenseSum), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1.3f)
                    .height(290.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "📈 التقرير والتدفق المالي الذكي الكلي من يناير ليونيو", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    ComparisonBarChart(
                        income = incomeSum,
                        expense = expenseSum,
                        modifier = Modifier.weight(1f),
                        isDark = vm.isDarkTheme.value
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(290.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "🏆 أعلى بنود ومحاور الإنفاق", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    val topExpList = txns.filter { it.type == "expense" }.groupBy { it.cat }.mapValues { entry -> entry.value.sumOf { it.amt } }.toList().sortedByDescending { it.second }.take(3)
                    if (topExpList.isEmpty()) {
                        Text(text = "لا توجد مصروفات مسجلة بعد.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                            items(topExpList) { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = item.first, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = formatCurrency(item.second), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(text = "تصدير PDF", fontSize = 11.sp)
                        }
                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(text = "تصدير Excel", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    vm: MainViewModel,
    settings: UserSettings?
) {
    var nameInput by remember { mutableStateOf(settings?.name ?: "Bichoy Karam") }
    var emailInput by remember { mutableStateOf(settings?.email ?: "bichoykaram@gmail.com") }
    var phoneInput by remember { mutableStateOf(settings?.phone ?: "+20 100 000 0000") }
    var selectCurrency by remember { mutableStateOf(settings?.currency ?: "EGP") }
    var selectLanguage by remember { mutableStateOf(settings?.language ?: "ar") }
    var selectTheme by remember { mutableStateOf(settings?.theme ?: "light") }

    var notifBills by remember { mutableStateOf(settings?.notifBills ?: true) }
    var notifBudget by remember { mutableStateOf(settings?.notifBudget ?: true) }
    var notifGoals by remember { mutableStateOf(settings?.notifGoals ?: true) }
    var notifSummary by remember { mutableStateOf(settings?.notifSummary ?: true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Warning unsaved changes bar styled identically to HTML
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF4A4458))
                .border(2.dp, Color(0xFFD0BCFF), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "جميع إعدادات مالي الشخصية محفوظة", color = Color(0xFFD0BCFF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Button(
                    onClick = {
                        vm.saveSettings(nameInput, emailInput, phoneInput, selectCurrency, selectLanguage, selectTheme, notifBills, notifBudget, notifGoals, notifSummary)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = "حفظ الإعدادات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile block
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "👤 الملف الشخصي للعميل", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD0BCFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = (settings?.name ?: "B").take(1), color = Color(0xFF381E72), fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = settings?.name ?: "Bichoy Karam", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = settings?.email ?: "bichoykaram@gmail.com", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(text = "الاسم الكامل") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text(text = "البريد الإلكتروني") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text(text = "رقم الهاتف") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // General settings Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "⚙️ إعدادات التطبيق", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "اللغة العامة (Language)", fontSize = 12.sp)
                            Button(onClick = { selectLanguage = "ar" }, colors = ButtonDefaults.buttonColors(containerColor = if (selectLanguage == "ar") Color(0xFFD0BCFF) else Color.Gray)) {
                                Text(text = "العربية", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "المظهر العام", fontSize = 12.sp)
                            Button(onClick = {
                                selectTheme = if (selectTheme == "dark") "light" else "dark"
                                vm.toggleDarkTheme()
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))) {
                                Text(text = if (selectTheme == "dark") "داكن" else "فاتح", fontSize = 10.sp)
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "🔔 إعدادات التنبيهات المباشرة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "تنبيهات الفواتير", fontSize = 11.sp)
                            Switch(checked = notifBills, onCheckedChange = { notifBills = it })
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "تجاوز الميزانية", fontSize = 11.sp)
                            Switch(checked = notifBudget, onCheckedChange = { notifBudget = it })
                        }
                    }
                }
            }
        }

        // Full system restore data options bar styled identically to HTML Settings Foot
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "🔧 الصيانة وإعادة ضبط المصنع", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { vm.navigateTo("history") }, modifier = Modifier.weight(1f)) {
                        Text(text = "سجل الإعدادات", fontSize = 10.sp)
                    }
                    Button(onClick = { vm.resetAllAccountBalances() }, modifier = Modifier.weight(1f)) {
                        Text(text = "تصفير أرصدة الحسابات", fontSize = 10.sp)
                    }
                    Button(
                        onClick = { vm.resetAllAppData() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text(text = "إعادة الضبط الكلي", fontSize = 10.sp, color = Color(0xFF381E72))
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(
    vm: MainViewModel,
    histLogs: List<EditHistory>,
    settingsHist: List<SettingsHistory>,
    snapshots: List<TxnSnapshot>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Financial Snapshot restore segment
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "♻️ استرجاع العمليات المالية والنسخ الاحتياطية", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { vm.saveTxnSnapshot("النسخة الدورية الفورية") }) {
                            Text(text = "حفظ نسخة الآن", fontSize = 10.sp)
                        }
                        Button(onClick = { vm.restoreOriginalTxns() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                            Text(text = "تصفير العمليات", fontSize = 10.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (snapshots.isEmpty()) {
                    Text(text = "لا توجد نسخ معلقة للتسجيل.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 130.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(snapshots) { snap ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.LightGray.copy(alpha = 0.1f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "${snap.label} • ${Date(snap.ts).toLocaleString()}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Button(onClick = { vm.restoreTxnSnapshot(snap) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))) {
                                    Text(text = "استرجاع", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Settings snapshot tracking segment
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "⚙️ سجل الإعدادات السابقة المتوفر للاسترجاع", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                if (settingsHist.isEmpty()) {
                    Text(text = "لا توجد نسخ سابقة للإعدادات.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 130.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(settingsHist) { sh ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.LightGray.copy(alpha = 0.1f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "لقطة إعدادات بتاريخ ${Date(sh.ts).toLocaleString()}", fontSize = 12.sp)
                                Button(onClick = { vm.restoreSettingsHistory(sh) }) {
                                    Text(text = "استرجاع", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Application Audit logs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📜 سجل التعديلات التفصيلي للتطبيق بالكامل", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (histLogs.isNotEmpty()) {
                        IconButton(onClick = { vm.clearHistory() }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (histLogs.isEmpty()) {
                    Text(text = "لا توجد أي تعديلات مسجلة حتى الآن.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier.height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(histLogs) { h ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "[${h.action.uppercase()}] ${h.entity}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFD0BCFF))
                                    Text(text = h.desc, fontSize = 12.sp)
                                }
                                Text(text = Date(h.ts).toLocaleString(), fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
