package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ComparisonBarChart(
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val barColorInc = Color(0xFF74C69D)
    val barColorExp = Color(0xFFFCA5A5)
    val gridColor = if (isDark) Color(0xFF4A4458) else Color(0xFFE5E7EB)
    val labelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val paddingLeft = 60f
        val paddingBottom = 40f
        val chartWidth = width - paddingLeft - 20f
        val chartHeight = height - paddingBottom - 20f

        // Draw grid lines
        val gridLines = 4
        val maxVal = maxOf(income, expense, 1000.0).toFloat() * 1.2f

        for (i in 0..gridLines) {
            val y = paddingBottom + chartHeight - (chartHeight / gridLines * i)
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
            // Draw axis markers
            val labelVal = (maxVal / gridLines * i).toInt()
            drawContext.canvas.nativeCanvas.drawText(
                "$labelVal",
                10f,
                y + 10f,
                android.graphics.Paint().apply {
                    color = labelColor.hashCode()
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.LEFT
                }
            )
        }

        // Draw Bars
        val groupWidth = chartWidth / 3f
        val barWidth = groupWidth * 0.3f

        // Jan - May (Mocked to show a full monthly timeline like HTML Chart)
        val mockDataInc = listOf(15000f, 16200f, 14800f, 17500f, 15900f, income.toFloat())
        val mockDataExp = listOf(9200f, 10100f, 8700f, 11200f, 9800f, expense.toFloat())
        val months = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو")

        val stepX = chartWidth / mockDataInc.size
        for (idx in mockDataInc.indices) {
            val cx = paddingLeft + (stepX * idx) + (stepX / 2f)

            // Income Bar
            val incH = (mockDataInc[idx] / maxVal) * chartHeight
            val incY = paddingBottom + chartHeight - incH
            drawRect(
                color = barColorInc,
                topLeft = Offset(cx - barWidth - 4f, incY),
                size = Size(barWidth, incH)
            )

            // Expense Bar
            val expH = (mockDataExp[idx] / maxVal) * chartHeight
            val expY = paddingBottom + chartHeight - expH
            drawRect(
                color = barColorExp,
                topLeft = Offset(cx + 4f, expY),
                size = Size(barWidth, expH)
            )

            // Label
            drawContext.canvas.nativeCanvas.drawText(
                months[idx],
                cx,
                height - 10f,
                android.graphics.Paint().apply {
                    color = labelColor.hashCode()
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun ExpensePieChart(
    categories: Map<String, Double>,
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val colors = listOf(
        Color(0xFFD0BCFF), Color(0xFF3B82F6), Color(0xFFF59E0B),
        Color(0xFF8B5CF6), Color(0xFFEF4444), Color(0xFFEC4899),
        Color(0xFF0F766E), Color(0xFF16A34A)
    )
    val textLabelColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1F2937)

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val minDim = minOf(width, height)
        val radius = minDim * 0.35f
        val center = Offset(width / 2f, height / 2f)

        val total = categories.values.sum().toFloat()
        if (total <= 0f) {
            // Draw empty state circle
            drawArc(
                color = Color.LightGray.copy(alpha = 0.4f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = 30.dp.toPx())
            )
            return@Canvas
        }

        var startAngle = -90f
        categories.entries.forEachIndexed { idx, entry ->
            val sweep = (entry.value.toFloat() / total) * 360f
            val color = colors[idx % colors.size]

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = 24.dp.toPx())
            )
            startAngle += sweep
        }

        // Intermediary rating display
        val amountStr = String.format("%,.0f", total) + " ج.م"
        drawContext.canvas.nativeCanvas.drawText(
            amountStr,
            center.x,
            center.y + 10f,
            android.graphics.Paint().apply {
                color = textLabelColor.hashCode()
                textSize = 32f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )
    }
}

@Composable
fun BudgetHorizontalBarChart(
    limits: List<Double>,
    spents: List<Double>,
    labels: List<String>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val axisColor = if (isDark) Color(0xFF4A4458) else Color(0xFFE5E7EB)
    val labelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val paddingRight = 120f
        val paddingLeft = 20f
        val chartWidth = width - paddingRight - paddingLeft
        val chartHeight = height - 40f

        if (limits.isEmpty()) return@Canvas

        val maxVal = maxOf((limits.maxOrNull() ?: 1000.0), (spents.maxOrNull() ?: 0.0), 1.0).toFloat() * 1.1f
        val stepY = chartHeight / limits.size

        for (i in limits.indices) {
            val cy = 20F + (stepY * i) + (stepY / 2f)

            // Draw category label on the right (RTL Arabic alignment)
            drawContext.canvas.nativeCanvas.drawText(
                labels[i],
                width - 10f,
                cy + 8f,
                android.graphics.Paint().apply {
                    color = labelColor.hashCode()
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )

            // Draw axis vertical line
            drawLine(
                color = axisColor,
                start = Offset(width - paddingRight, 0f),
                end = Offset(width - paddingRight, height),
                strokeWidth = 1.dp.toPx()
            )

            val availableWidth = chartWidth - 20f
            val limitW = ((limits[i].toFloat() / maxVal) * availableWidth).coerceAtLeast(10f)
            val spentW = ((spents[i].toFloat() / maxVal) * availableWidth).coerceAtLeast(10f)

            val barHeight = stepY * 0.25f

            // Limit Bar background
            drawRect(
                color = Color.LightGray.copy(alpha = 0.3f),
                topLeft = Offset(width - paddingRight - limitW, cy - barHeight - 2f),
                size = Size(limitW, barHeight)
            )

            // Spent Colored overlay Bar
            drawRect(
                color = colors.getOrElse(i) { Color(0xFFD0BCFF) },
                topLeft = Offset(width - paddingRight - spentW, cy + 2f),
                size = Size(spentW, barHeight)
            )
        }
    }
}

@Composable
fun BalanceAreaChart(
    values: List<Float>,
    months: List<String>,
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val strokeColor = Color(0xFFD0BCFF)
    val gridColor = if (isDark) Color(0xFF4A4458) else Color(0xFFE5E7EB)
    val labelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val paddingLeft = 50f
        val paddingBottom = 45f
        val chartWidth = width - paddingLeft - 20f
        val chartHeight = height - paddingBottom - 20f

        val maxVal = (values.maxOrNull() ?: 1000f) * 1.15f
        val minVal = 0f

        // Grid lines
        val lines = 3
        for (i in 0..lines) {
            val y = paddingBottom + chartHeight - (chartHeight / lines * i)
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
            // Label
            val valAtGrid = minVal + (maxVal / lines * i)
            drawContext.canvas.nativeCanvas.drawText(
                "${valAtGrid.toInt()}",
                10f,
                y + 8f,
                android.graphics.Paint().apply {
                    color = labelColor.hashCode()
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.LEFT
                }
            )
        }

        if (values.size < 2) return@Canvas

        val stepX = chartWidth / (values.size - 1)
        val path = Path()

        // Construct coordinate points
        val points = values.mapIndexed { idx, v ->
            val px = paddingLeft + (stepX * idx)
            val py = paddingBottom + chartHeight - ((v / maxVal) * chartHeight)
            Offset(px, py)
        }

        // Draw Line layout
        path.moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            path.lineTo(points[i].x, points[i].y)
        }

        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw Month text labels underneath
        for (idx in months.indices) {
            val px = paddingLeft + (stepX * idx)
            drawContext.canvas.nativeCanvas.drawText(
                months[idx],
                px,
                height - 10f,
                android.graphics.Paint().apply {
                    color = labelColor.hashCode()
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun HealthIndexRadarChart(
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val strokeColor = Color(0xFFD0BCFF)
    val gridColor = if (isDark) Color(0xFF4A4458) else Color(0xFFE5E7EB)
    val labelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = minOf(size.width, size.height) * 0.38f

        val indicators = listOf("الادخار", "الميزانية", "الأهداف", "السيولة", "الالتزامات", "الاستثمار")
        val scores = listOf(85f, 65f, 42f, 70f, 48f, 60f)

        // Draw outer polygon grids (3 levels)
        val levels = 3
        for (lvl in 1..levels) {
            val r = maxRadius * (lvl.toFloat() / levels)
            val gridPath = Path()
            for (i in indicators.indices) {
                val angle = i * (360f / indicators.size) * (Math.PI / 180f)
                val px = center.x + r * cos(angle).toFloat()
                val py = center.y + r * sin(angle).toFloat()
                if (i == 0) gridPath.moveTo(px, py) else gridPath.lineTo(px, py)
            }
            gridPath.close()
            drawPath(
                path = gridPath,
                color = gridColor,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw spoke axes and labels
        for (i in indicators.indices) {
            val angle = i * (360f / indicators.size) * (Math.PI / 180f)
            val px = center.x + maxRadius * cos(angle).toFloat()
            val py = center.y + maxRadius * sin(angle).toFloat()
            drawLine(
                color = gridColor,
                start = center,
                end = Offset(px, py),
                strokeWidth = 1.dp.toPx()
            )

            // Draw RTL and LTR aligned labels dynamically
            val textX = center.x + (maxRadius + 22f) * cos(angle).toFloat()
            val textY = center.y + (maxRadius + 18f) * sin(angle).toFloat()
            drawContext.canvas.nativeCanvas.drawText(
                indicators[i],
                textX,
                textY + 8f,
                android.graphics.Paint().apply {
                    color = labelColor.hashCode()
                    textSize = 21f
                    textAlign = when {
                        cos(angle) > 0.1 -> android.graphics.Paint.Align.LEFT
                        cos(angle) < -0.1 -> android.graphics.Paint.Align.RIGHT
                        else -> android.graphics.Paint.Align.CENTER
                    }
                }
            )
        }

        // Draw Value Polygon
        val valuePath = Path()
        for (i in scores.indices) {
            val valueRad = maxRadius * (scores[i] / 100f)
            val angle = i * (360f / indicators.size) * (Math.PI / 180f)
            val px = center.x + valueRad * cos(angle).toFloat()
            val py = center.y + valueRad * sin(angle).toFloat()
            if (i == 0) valuePath.moveTo(px, py) else valuePath.lineTo(px, py)
        }
        valuePath.close()

        // Fill background transparency
        drawPath(
            path = valuePath,
            color = strokeColor.copy(alpha = 0.15f)
        )
        // Outline border
        drawPath(
            path = valuePath,
            color = strokeColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
