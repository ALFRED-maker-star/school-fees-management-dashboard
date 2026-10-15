package com.example.feetrack.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feetrack.model.*
import com.example.feetrack.ui.FeeTrackViewModel
import com.example.feetrack.ui.Screen
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ReportsScreen(
  viewModel: FeeTrackViewModel,
  modifier: Modifier = Modifier
) {
  val classReports by viewModel.classCollectionReports.collectAsState()
  val methodReports by viewModel.paymentMethodReports.collectAsState()
  val leaderboard by viewModel.defaultLeaderboard.collectAsState()
  val stats by viewModel.dashboardStats.collectAsState()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(SlateBackground)
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp)
  ) {
    // Header
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "Financial Reports & Analytics",
          style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
          color = TextPrimary
        )
        Text(
          text = "Revenue collection trends, payment channel breakdown, and delinquency risk audit",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )
      }
    }

    // High Level KPI Strip
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          color = SlateSurface,
          shadowElevation = 1.dp
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Collection Efficiency", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(
              String.format("%.1f%%", stats.collectionRatePercent),
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = PaidGreen)
            )
            Text("Overall institutional rate", style = MaterialTheme.typography.labelSmall, color = TextMuted)
          }
        }

        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          color = SlateSurface,
          shadowElevation = 1.dp
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Average / Student", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            val avg = if (stats.totalStudentsCount > 0) stats.totalCollected / stats.totalStudentsCount else 0.0
            Text(
              FeeTrackFormatters.formatCurrency(avg),
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TealPrimary)
            )
            Text("Realized collections", style = MaterialTheme.typography.labelSmall, color = TextMuted)
          }
        }

        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          color = SlateSurface,
          shadowElevation = 1.dp
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Overdue Accounts", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(
              "${leaderboard.size} Students",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = OwingRed)
            )
            Text("Actionable balances", style = MaterialTheme.typography.labelSmall, color = TextMuted)
          }
        }
      }
    }

    // 1. Bar Chart of Collections by Class
    item {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SlateSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Collections by Class (Expected vs Collected)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
              )
              Text(
                text = "Comparative revenue attainment across grade levels",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
              )
            }

            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(TealPrimary))
                Text("Collected", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
              }
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(SlateBorderHover))
                Text("Target / Expected", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
              }
            }
          }

          HorizontalDivider(color = SlateBorder)

          // Canvas Bar Chart
          val topClasses = remember(classReports) { classReports.take(6) }
          val maxExpected = remember(topClasses) {
            topClasses.maxOfOrNull { it.expected } ?: 1.0
          }

          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            topClasses.forEach { cr ->
              val progress = if (cr.expected > 0) (cr.collected / cr.expected).toFloat().coerceIn(0f, 1f) else 0f
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = cr.className,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                  )
                  Text(
                    text = "${FeeTrackFormatters.formatCurrency(cr.collected)} / ${FeeTrackFormatters.formatCurrency(cr.expected)} (${String.format("%.0f%%", cr.ratePercent)})",
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (cr.ratePercent >= 75) PaidGreen else TealPrimary
                    )
                  )
                }

                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SlateSurfaceVariant)
                ) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth(fraction = progress)
                      .fillMaxHeight()
                      .clip(RoundedCornerShape(6.dp))
                      .background(if (progress >= 0.75f) PaidGreen else TealPrimary)
                  )
                }
              }
            }
          }
        }
      }
    }

    // 2. Pie / Donut Chart of Payment Methods
    item {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SlateSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Column {
            Text(
              text = "Payment Methods Distribution",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Text(
              text = "Volume and capital distribution by transaction instrument",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )
          }

          HorizontalDivider(color = SlateBorder)

          val methodColors = listOf(
            TealPrimary,
            Color(0xFF0284C7), // Blue
            PaidGreen,
            PartialAmber,
            Color(0xFF8B5CF6)  // Purple
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
          ) {
            // Donut Canvas
            Box(
              modifier = Modifier
                .size(160.dp),
              contentAlignment = Alignment.Center
            ) {
              Canvas(modifier = Modifier.fillMaxSize()) {
                var currentAngle = -90f
                val strokeWidth = 32.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)

                methodReports.forEachIndexed { idx, rep ->
                  val sweep = (rep.percentageOfTotal / 100.0 * 360.0).toFloat()
                  val color = methodColors[idx % methodColors.size]
                  drawArc(
                    color = color,
                    startAngle = currentAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth)
                  )
                  currentAngle += sweep
                }
              }

              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = FeeTrackFormatters.formatCompactCurrency(stats.totalCollected),
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimary
                )
                Text(
                  text = "Total Paid",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextMuted
                )
              }
            }

            // Method Legend List
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              methodReports.forEachIndexed { idx, rep ->
                val color = methodColors[idx % methodColors.size]
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(color)
                    )
                    Text(
                      text = rep.method.label,
                      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                      color = TextPrimary
                    )
                  }

                  Text(
                    text = "${String.format("%.1f%%", rep.percentageOfTotal)} (${FeeTrackFormatters.formatCompactCurrency(rep.totalAmount)})",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                  )
                }
              }
            }
          }
        }
      }
    }

    // 3. Outstanding Balance Leaderboard
    item {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SlateSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Outstanding Balance Leaderboard",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
              )
              Text(
                text = "Priority delinquent accounts requiring bursary follow-up",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
              )
            }

            Surface(
              shape = RoundedCornerShape(20.dp),
              color = OwingRedContainer
            ) {
              Text(
                text = "Top Priority",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = OnOwingRed,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }

          HorizontalDivider(color = SlateBorder)

          // Leaderboard Items
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            leaderboard.take(6).forEachIndexed { index, student ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { viewModel.navigateTo(Screen.StudentProfile(student.id)) }
                  .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Surface(
                    shape = CircleShape,
                    color = if (index < 3) OwingRedContainer else SlateSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = "#${index + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (index < 3) OnOwingRed else TextSecondary
                      )
                    }
                  }

                  Column {
                    Text(
                      text = student.fullName,
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                      color = TextPrimary
                    )
                    Text(
                      text = "${student.className} • Guardian: ${student.guardianName} (${student.guardianPhone})",
                      style = MaterialTheme.typography.bodySmall,
                      color = TextSecondary
                    )
                  }
                }

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = FeeTrackFormatters.formatCurrency(student.balance),
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = OwingRed)
                    )
                    Text(
                      text = "Total: ${FeeTrackFormatters.formatCurrency(student.totalFee)}",
                      style = MaterialTheme.typography.labelSmall,
                      color = TextMuted
                    )
                  }

                  IconButton(
                    onClick = { viewModel.setReminderStudent(student) },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      Icons.Default.NotificationsActive,
                      contentDescription = "Send Reminder",
                      tint = OwingRed,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }
              HorizontalDivider(color = SlateBorder.copy(alpha = 0.5f))
            }
          }
        }
      }
    }
  }
}
