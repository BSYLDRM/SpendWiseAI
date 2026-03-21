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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendwiseai.data.db.dao.CategoryTotal

@Composable
fun DoughnutChart(
    categoryTotals: List<CategoryTotal>,
    modifier: Modifier = Modifier,
    isIncome: Boolean = false
) {
    val total = categoryTotals.sumOf { it.totalAmount }
    if (total <= 0.0) {
        Text(
            text = if (isIncome) "Henüz gelir verisi yok" else "Henüz gider verisi yok",
            color = Color.Gray,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    val stroke = 28.dp

    Column(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .aspectRatio(1f)
            ) {
                var startAngle = -90f
                val strokePx = stroke.toPx()

                categoryTotals.forEach { segment ->
                    val fraction = segment.totalAmount / total
                    val sweepAngle = fraction.toFloat() * 360f
                    val color = if (isIncome)
                        incomeCategoryColor(segment.categoryName)
                    else
                        expenseCategoryColor(segment.categoryName)

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                        size = this.size,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        categoryTotals.take(7).forEach { segment ->
            val color = if (isIncome)
                incomeCategoryColor(segment.categoryName)
            else
                expenseCategoryColor(segment.categoryName)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = categoryTr(segment.categoryName),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${((segment.totalAmount / total) * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Gider kategorisi renkleri
fun expenseCategoryColor(categoryName: String): Color = when (categoryName) {
    "Food & Drink"      -> Color(0xFFFF6B6B) // kırmızı
    "Groceries"         -> Color(0xFF4ECDC4) // turkuaz
    "Transportation"    -> Color(0xFF45B7D1) // mavi
    "Entertainment"     -> Color(0xFF96CEB4) // yeşil
    "Shopping"          -> Color(0xFFFFD93D) // sarı
    "Bills & Utilities" -> Color(0xFFDDA0DD) // mor
    "Health"            -> Color(0xFFFF8A65) // turuncu
    "Education"         -> Color(0xFF82B1FF) // açık mavi
    "Technology"        -> Color(0xFFFFAB40) // amber
    else                -> Color(0xFFB0B0B0) // gri
}

// Gelir kategorisi renkleri — her biri birbirinden tamamen farklı
fun incomeCategoryColor(categoryName: String): Color = when (categoryName) {
    "Salary"         -> Color(0xFF00E5FF) // neon cyan — maaş parlak
    "Freelance"      -> Color(0xFFD500F9) // neon mor — freelance bold
    "Refund"         -> Color(0xFFFF1744) // parlak kırmızı — iade dikkat çekici
    "Meal Allowance" -> Color(0xFFFF6D00) // yanık turuncu — yemek parası
    "Investment"     -> Color(0xFF76FF03) // neon lime — yatırım
    "Gift"           -> Color(0xFFFF4081) // fuşya — hediye
    "Other Income"   -> Color(0xFFFFD740) // altın sarı — diğer
    else             -> Color(0xFF64FFDA) // mint — bilinmeyen
}

// Türkçe kategori adları
fun categoryTr(category: String): String = when (category) {
    "Food & Drink"      -> "Yemek & İçecek"
    "Groceries"         -> "Market"
    "Transportation"    -> "Ulaşım"
    "Entertainment"     -> "Eğlence"
    "Shopping"          -> "Alışveriş"
    "Bills & Utilities" -> "Faturalar"
    "Health"            -> "Sağlık"
    "Education"         -> "Eğitim"
    "Technology"        -> "Teknoloji"
    "Salary"            -> "Maaş"
    "Freelance"         -> "Freelance"
    "Refund"            -> "İade"
    "Meal Allowance"    -> "Yemek Parası"
    "Investment"        -> "Yatırım"
    "Gift"              -> "Hediye"
    "Other Income"      -> "Diğer Gelir"
    else                -> category
}