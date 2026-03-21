package com.example.spendwiseai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.spendwiseai.data.db.dao.CategoryTotal

@Composable
fun DoughnutChart(
    categoryTotals: List<CategoryTotal>,
    modifier: Modifier = Modifier
) {
    val total = categoryTotals.sumOf { it.totalAmount }
    if (total <= 0.0) {
        Card(modifier = modifier) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Kategori Dagilimi", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Henuz gider verisi yok.")
            }
        }
        return
    }

    val stroke = 26.dp

    Column(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .aspectRatio(1f)
            ) {
                var startAngle = -90f
                val strokePx = stroke.toPx()

                categoryTotals.forEach { segment ->
                    val fraction = segment.totalAmount / total
                    val sweepAngle = fraction.toFloat() * 360f
                    val color = categoryColor(segment.categoryName)

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(0f, 0f),
                        size = this.size,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        categoryTotals.take(6).forEach { segment ->
            val color = categoryColor(segment.categoryName)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = segment.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${((segment.totalAmount / total) * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun categoryColor(categoryName: String): Color {
    return when (categoryName) {
        "Food & Drink" -> Color(0xFFFF6B6B)
        "Groceries" -> Color(0xFF4ECDC4)
        "Transportation" -> Color(0xFF45B7D1)
        "Entertainment" -> Color(0xFF96CEB4)
        "Shopping" -> Color(0xFFFFEAA7)
        "Bills & Utilities" -> Color(0xFFDDA0DD)
        "Health" -> Color(0xFF98D8C8)
        else -> Color(0xFFB0B0B0)
    }
}

