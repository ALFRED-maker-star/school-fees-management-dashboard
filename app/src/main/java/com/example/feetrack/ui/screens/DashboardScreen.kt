package com.example.feetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feetrack.model.*
import com.example.feetrack.ui.FeeTrackViewModel
import com.example.feetrack.ui.Screen
import com.example.feetrack.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
  viewModel: FeeTrackViewModel,
  modifier: Modifier = Modifier
) {
  val stats by viewModel.dashboardStats.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val selectedTerm by viewModel.selectedAcademicTerm.collectAsState()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(SlateBackground)
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // Header Greeting & Quick Actions
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Financial Dashboard",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
          Text(
            text = "Welcome, ${currentUser?.name ?: "Bursar"} • ${selectedTerm.label}",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Button(
            onClick = { viewModel.navigateTo(Screen.RecordPayment()) },
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("dashboard_record_payment_btn")
          ) {
            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Record Payment")
          }
        }
      }
    }

    // 4 Key Stat Cards
    item {
      BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isWide = maxWidth >= 600.dp
        if (isWide) {
          Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              StatCard(
                title = "Total Expected",
                value = FeeTrackFormatters.formatCurrency(stats.totalExpected),
                subtitle = "Across all enrolled students",
                icon = Icons.Default.AccountBalance,
                iconTint = TealPrimary,
                iconContainerColor = TealContainer,
                modifier = Modifier.weight(1f)
              )
              StatCard(
                title = "Total Collected",
                value = FeeTrackFormatters.formatCurrency(stats.totalCollected),
                subtitle = "Cleared transactions",
                icon = Icons.Default.CheckCircle,
                iconTint = PaidGreen,
                iconContainerColor = PaidGreenContainer,
                badgeText = String.format("%.1f%%", stats.collectionRatePercent),
                badgePositive = true,
                modifier = Modifier.weight(1f)
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              StatCard(
                title = "Outstanding Balance",
                value = FeeTrackFormatters.formatCurrency(stats.outstandingBalance),
                subtitle = "Unpaid fee commitments",
                icon = Icons.Default.Warning,
                iconTint = OwingRed,
                iconContainerColor = OwingRedContainer,
                badgeText = "Due Now",
                badgePositive = false,
                modifier = Modifier.weight(1f)
              )
              StatCard(
                title = "Students in Default",
                value = "${stats.studentsInDefaultCount} Students",
                subtitle = "Zero payments logged this term",
                icon = Icons.Default.PersonOff,
                iconTint = PartialAmber,
                iconContainerColor = PartialAmberContainer,
                modifier = Modifier.weight(1f),
                onClick = {
                  viewModel.setStudentStatusFilter(FeeStatus.OWING)
                  viewModel.navigateTo(Screen.StudentsList)
                }
              )
            }
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
              title = "Total Expected",
              value = FeeTrackFormatters.formatCurrency(stats.totalExpected),
              subtitle = "All enrolled students",
              icon = Icons.Default.AccountBalance,
              iconTint = TealPrimary,
              iconContainerColor = TealContainer
            )
            StatCard(
              title = "Total Collected",
              value = FeeTrackFormatters.formatCurrency(stats.totalCollected),
              subtitle = "Cleared transactions",
              icon = Icons.Default.CheckCircle,
              iconTint = PaidGreen,
              iconContainerColor = PaidGreenContainer,
              badgeText = String.format("%.1f%%", stats.collectionRatePercent),
              badgePositive = true
            )
            StatCard(
              title = "Outstanding Balance",
              value = FeeTrackFormatters.formatCurrency(stats.outstandingBalance),
              subtitle = "Unpaid fee commitments",
              icon = Icons.Default.Warning,
              iconTint = OwingRed,
              iconContainerColor = OwingRedContainer,
              badgeText = "Due Now",
              badgePositive = false
            )
            StatCard(
              title = "Students in Default",
              value = "${stats.studentsInDefaultCount} Students",
              subtitle = "Zero payments logged",
              icon = Icons.Default.PersonOff,
              iconTint = PartialAmber,
              iconContainerColor = PartialAmberContainer,
              onClick = {
                viewModel.setStudentStatusFilter(FeeStatus.OWING)
                viewModel.navigateTo(Screen.StudentsList)
              }
            )
          }
        }
      }
    }

    // Collection Rate Progress Bar Card
    item {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SlateSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Collection Rate Progress",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
              )
              Text(
                text = "Midterm benchmark goal: 85.0%",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
              )
            }

            Text(
              text = String.format("%.1f%%", stats.collectionRatePercent),
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = if (stats.collectionRatePercent >= 75.0) PaidGreen else PartialAmber,
                fontSize = 24.sp
              )
            )
          }

          // Progress Bar
          val progressFraction = (stats.collectionRatePercent / 100.0).toFloat().coerceIn(0f, 1f)
          LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
              .fillMaxWidth()
              .height(14.dp)
              .clip(RoundedCornerShape(8.dp)),
            color = if (progressFraction >= 0.7f) PaidGreen else TealPrimary,
            trackColor = SlateSurfaceVariant
          )

          // Sub-metrics split
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(PaidGreen)
              )
              Text(
                text = "Collected: ${FeeTrackFormatters.formatCompactCurrency(stats.totalCollected)}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = TextPrimary
              )
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(OwingRed)
              )
              Text(
                text = "Remaining: ${FeeTrackFormatters.formatCompactCurrency(stats.outstandingBalance)}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = TextPrimary
              )
            }
          }
        }
      }
    }

    // Recent Payments Feed Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Recent Payments Feed",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
          Text(
            text = "Latest verified transactions across all classes",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }

        TextButton(
          onClick = { viewModel.navigateTo(Screen.PaymentHistory) },
          modifier = Modifier.testTag("dashboard_view_all_payments_btn")
        ) {
          Text("View All", color = TealPrimary, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.width(4.dp))
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = TealPrimary)
        }
      }
    }

    // Recent Payments Feed Items
    items(stats.recentPayments) { payment ->
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { viewModel.showReceipt(payment) }
          .testTag("recent_payment_card_${payment.receiptNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SlateSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(TealContainer),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = payment.studentName.take(2).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TealPrimary
              )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = payment.studentName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
              )
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = payment.className,
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondary
                )
                Text(text = "•", color = TextMuted)
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = SlateSurfaceVariant
                ) {
                  Text(
                    text = payment.paymentMethod.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }

          Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Text(
              text = "+${FeeTrackFormatters.formatCurrency(payment.amount)}",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PaidGreen
              )
            )
            Text(
              text = payment.formattedShortDate,
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted
            )
          }
        }
      }
    }
  }
}
