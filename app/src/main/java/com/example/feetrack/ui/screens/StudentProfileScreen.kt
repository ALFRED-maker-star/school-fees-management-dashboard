package com.example.feetrack.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feetrack.model.*
import com.example.feetrack.ui.FeeTrackViewModel
import com.example.feetrack.ui.Screen
import com.example.feetrack.ui.components.FeeStatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
  studentId: String,
  viewModel: FeeTrackViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateBack()
  }

  val context = LocalContext.current
  val allStudents by viewModel.allStudents.collectAsState()
  val allPayments by viewModel.allPayments.collectAsState()

  val student = allStudents.find { it.id == studentId }
  val studentPayments = remember(allPayments, studentId) {
    allPayments.filter { it.studentId == studentId }
  }

  var selectedTerm by remember { mutableStateOf(AcademicTerm.TERM_2) }

  if (student == null) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Student profile not found", style = MaterialTheme.typography.bodyLarge)
        Button(onClick = { viewModel.navigateBack() }) {
          Text("Go Back")
        }
      }
    }
    return
  }

  val currentTermBreakdown = student.termBreakdowns.find { it.term == selectedTerm }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(SlateBackground)
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // Top Bar with Back Button & Title
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          IconButton(
            onClick = { viewModel.navigateBack() },
            modifier = Modifier.testTag("student_profile_back_btn")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
          Column {
            Text(
              text = "Student Fee Profile",
              style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Text(
              text = "Comprehensive financial ledger and breakdown",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          if (student.balance > 0) {
            OutlinedButton(
              onClick = { viewModel.setReminderStudent(student) },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("send_reminder_notice_btn")
            ) {
              Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp), tint = OwingRed)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Send Reminder", color = TextPrimary)
            }
          }

          Button(
            onClick = { viewModel.navigateTo(Screen.RecordPayment(student.id)) },
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("record_payment_profile_btn")
          ) {
            Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Record Payment")
          }
        }
      }
    }

    // Student Info Header Card
    item {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SlateSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .clip(CircleShape)
                  .background(TealContainer),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = student.fullName.take(2).uppercase(),
                  style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TealPrimary)
                )
              }

              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Text(
                    text = student.fullName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                  )
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SlateSurfaceVariant
                  ) {
                    Text(
                      text = student.gender,
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = TextSecondary,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
                Text(
                  text = "Admission: ${student.admissionNo} • Class: ${student.className}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = TextSecondary
                )
                Text(
                  text = "Academic Session 2026/2027",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextMuted
                )
              }
            }

            FeeStatusBadge(status = student.status)
          }

          HorizontalDivider(color = SlateBorder)

          // Guardian Details
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
          ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text("Guardian / Parent", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text(student.guardianName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text("Phone Contact", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text(
                student.guardianPhone,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = TealPrimary),
                modifier = Modifier.clickable {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.guardianPhone}"))
                  context.startActivity(intent)
                }
              )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text("Email Address", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text(
                student.guardianEmail,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = TealPrimary),
                modifier = Modifier.clickable {
                  val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${student.guardianEmail}"))
                  context.startActivity(intent)
                }
              )
            }
          }
        }
      }
    }

    // Overall Financial Summary Row
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
            Text("Total Assessed Fee", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(FeeTrackFormatters.formatCurrency(student.totalFee), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
            Text("All 3 Terms Included", style = MaterialTheme.typography.labelSmall, color = TextMuted)
          }
        }

        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          color = PaidGreenContainer.copy(alpha = 0.5f),
          shadowElevation = 1.dp
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Total Amount Paid", style = MaterialTheme.typography.bodySmall, color = OnPaidGreen)
            Text(FeeTrackFormatters.formatCurrency(student.amountPaid), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = OnPaidGreen))
            Text("Verified payments", style = MaterialTheme.typography.labelSmall, color = OnPaidGreen.copy(alpha = 0.8f))
          }
        }

        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          color = if (student.balance > 0) OwingRedContainer.copy(alpha = 0.5f) else SlateSurface,
          shadowElevation = 1.dp
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Total Balance Due", style = MaterialTheme.typography.bodySmall, color = if (student.balance > 0) OnOwingRed else TextSecondary)
            Text(FeeTrackFormatters.formatCurrency(student.balance), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = if (student.balance > 0) OwingRed else TextPrimary))
            Text(if (student.balance > 0) "Immediate payment needed" else "Fully Settled", style = MaterialTheme.typography.labelSmall, color = if (student.balance > 0) OnOwingRed else PaidGreen)
          }
        }
      }
    }

    // Fee Breakdown Section by Term / Category
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
                text = "Fee Breakdown by Term & Category",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
              )
              Text(
                text = "Tuition, textbooks, uniform, laboratory, and levies",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
              )
            }

            // Term Chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              AcademicTerm.entries.forEach { term ->
                FilterChip(
                  selected = selectedTerm == term,
                  onClick = { selectedTerm = term },
                  label = { Text(term.shortLabel) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TealContainer,
                    selectedLabelColor = TealDark
                  ),
                  shape = RoundedCornerShape(8.dp)
                )
              }
            }
          }

          HorizontalDivider(color = SlateBorder)

          if (currentTermBreakdown != null) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              currentTermBreakdown.items.forEach { feeItem ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateSurfaceVariant),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = when (feeItem.category) {
                          FeeCategory.TUITION -> Icons.Default.School
                          FeeCategory.BOOKS -> Icons.Default.MenuBook
                          FeeCategory.UNIFORM -> Icons.Default.Checkroom
                          FeeCategory.LAB_TECH -> Icons.Default.Biotech
                          FeeCategory.ICT_DEV -> Icons.Default.Computer
                          FeeCategory.EXAM -> Icons.Default.Quiz
                          FeeCategory.PTA -> Icons.Default.Groups
                        },
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(18.dp)
                      )
                    }

                    Column {
                      Text(
                        text = feeItem.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = TextPrimary
                      )
                      Text(
                        text = feeItem.category.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                      )
                    }
                  }

                  Text(
                    text = FeeTrackFormatters.formatCurrency(feeItem.amount),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                  )
                }
                HorizontalDivider(color = SlateBorder.copy(alpha = 0.5f))
              }

              // Term Totals Summary
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SlateSurfaceVariant
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = "${selectedTerm.label} Subtotal",
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                      color = TextPrimary
                    )
                    Text(
                      text = "Paid: ${FeeTrackFormatters.formatCurrency(currentTermBreakdown.amountPaid)}",
                      style = MaterialTheme.typography.bodySmall,
                      color = PaidGreen
                    )
                  }

                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = FeeTrackFormatters.formatCurrency(currentTermBreakdown.totalAssessed),
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                      color = TextPrimary
                    )
                    Text(
                      text = "Term Balance: ${FeeTrackFormatters.formatCurrency(currentTermBreakdown.balance)}",
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (currentTermBreakdown.balance > 0) OwingRed else PaidGreen
                      )
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Payment History Table for this Student
    item {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SlateSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text(
            text = "Payment History (${studentPayments.size} Receipts)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )

          if (studentPayments.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                Text("No payments recorded yet for this student.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
              }
            }
          } else {
            studentPayments.forEach { payment ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { viewModel.showReceipt(payment) }
                  .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(TealContainer),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                  }
                  Column {
                    Text(
                      text = payment.receiptNumber,
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                      color = TextPrimary
                    )
                    Text(
                      text = "${payment.formattedShortDate} • ${payment.term.shortLabel} • ${payment.paymentMethod.label}",
                      style = MaterialTheme.typography.bodySmall,
                      color = TextSecondary
                    )
                  }
                }

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Text(
                    text = FeeTrackFormatters.formatCurrency(payment.amount),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PaidGreen)
                  )
                  IconButton(
                    onClick = { viewModel.showReceipt(payment) },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(Icons.Default.Visibility, contentDescription = "View Receipt", tint = TealPrimary, modifier = Modifier.size(18.dp))
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
