package com.example.feetrack.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.feetrack.model.*
import com.example.ui.theme.*

@Composable
fun FeeStatusBadge(
  status: FeeStatus,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, borderCol, icon) = when (status) {
    FeeStatus.PAID -> Quad(PaidGreenContainer, OnPaidGreen, PaidGreen.copy(alpha = 0.4f), Icons.Default.CheckCircle)
    FeeStatus.PARTIAL -> Quad(PartialAmberContainer, OnPartialAmber, PartialAmber.copy(alpha = 0.4f), Icons.Default.Schedule)
    FeeStatus.OWING -> Quad(OwingRedContainer, OnOwingRed, OwingRed.copy(alpha = 0.4f), Icons.Default.Warning)
  }

  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .border(1.dp, borderCol, RoundedCornerShape(20.dp)),
    color = bgColor
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = textColor,
        modifier = Modifier.size(13.dp)
      )
      Text(
        text = status.label,
        color = textColor,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
      )
    }
  }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun StatCard(
  title: String,
  value: String,
  subtitle: String? = null,
  icon: ImageVector,
  iconTint: Color,
  iconContainerColor: Color,
  modifier: Modifier = Modifier,
  badgeText: String? = null,
  badgePositive: Boolean = true,
  onClick: (() -> Unit)? = null
) {
  ElevatedCard(
    modifier = modifier
      .fillMaxWidth()
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = SlateSurface
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(iconContainerColor),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
          )
        }

        if (badgeText != null) {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (badgePositive) PaidGreenContainer else OwingRedContainer
          ) {
            Text(
              text = badgeText,
              color = if (badgePositive) OnPaidGreen else OnOwingRed,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary
        )
        Text(
          text = value,
          style = MaterialTheme.typography.titleLarge.copy(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
          ),
          color = TextPrimary
        )
        if (subtitle != null) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
          )
        }
      }
    }
  }
}

@Composable
fun ReceiptDialog(
  payment: Payment,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .widthIn(max = 520.dp)
        .padding(16.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = SlateSurface),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Receipt Header
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(TealContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Success",
            tint = TealPrimary,
            modifier = Modifier.size(32.dp)
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Official School Fee Receipt",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
          Text(
            text = "Greenwood International Academy • Bursary",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = TealContainer.copy(alpha = 0.5f)
          ) {
            Text(
              text = payment.receiptNumber,
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = TealDark,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
        }

        HorizontalDivider(color = SlateBorder)

        // Amount Display
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlateSurfaceVariant)
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "AMOUNT PAID",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = TextMuted
          )
          Text(
            text = FeeTrackFormatters.formatCurrency(payment.amount),
            style = MaterialTheme.typography.displayMedium.copy(
              color = TealPrimary,
              fontWeight = FontWeight.ExtraBold
            )
          )
          Text(
            text = "Method: ${payment.paymentMethod.label}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }

        // Details Grid
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          ReceiptRow("Student Name", payment.studentName)
          ReceiptRow("Admission No.", payment.admissionNo)
          ReceiptRow("Class", payment.className)
          ReceiptRow("Academic Term", payment.term.label)
          ReceiptRow("Date & Time", payment.formattedDate)
          ReceiptRow("Authorized Bursar", payment.receivedBy)
          if (payment.referenceNotes.isNotBlank()) {
            ReceiptRow("Reference / Note", payment.referenceNotes)
          }
        }

        HorizontalDivider(color = SlateBorder)

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedButton(
            onClick = {
              shareReceipt(context, payment)
            },
            modifier = Modifier
              .weight(1f)
              .testTag("share_receipt_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share")
          }

          Button(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .testTag("done_receipt_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Done")
          }
        }
      }
    }
  }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = TextSecondary
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      color = TextPrimary,
      textAlign = TextAlign.End,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

private fun shareReceipt(context: Context, payment: Payment) {
  val text = """
    ======================================
    GREENWOOD INTERNATIONAL ACADEMY
    OFFICIAL FEES RECEIPT
    ======================================
    Receipt No: ${payment.receiptNumber}
    Date: ${payment.formattedDate}
    Student: ${payment.studentName} (${payment.admissionNo})
    Class: ${payment.className}
    Term: ${payment.term.label}
    Amount Paid: ${FeeTrackFormatters.formatCurrency(payment.amount)}
    Method: ${payment.paymentMethod.label}
    Received By: ${payment.receivedBy}
    Notes: ${payment.referenceNotes}
    ======================================
    Status: VERIFIED & CLEARED
    ======================================
  """.trimIndent()

  val intent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_SUBJECT, "Fee Receipt - ${payment.receiptNumber} - ${payment.studentName}")
    putExtra(Intent.EXTRA_TEXT, text)
  }
  context.startActivity(Intent.createChooser(intent, "Share Fee Receipt"))
}

@Composable
fun ExportDialog(
  csvData: String,
  transactionCount: Int,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  var copied by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = SlateSurface)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(TealContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.FileDownload, contentDescription = null, tint = TealPrimary)
          }
          Column {
            Text(
              text = "Export Transaction Log",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Text(
              text = "$transactionCount transactions ready for export",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )
          }
        }

        Text(
          text = "CSV Preview:",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = TextSecondary
        )

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 160.dp),
          shape = RoundedCornerShape(8.dp),
          color = SlateSurfaceVariant
        ) {
          Text(
            text = csvData.lines().take(6).joinToString("\n") + "\n...",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(12.dp)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = {
              clipboardManager.setText(AnnotatedString(csvData))
              copied = true
            },
            modifier = Modifier
              .weight(1f)
              .testTag("copy_csv_button"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(
              imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (copied) "Copied!" else "Copy CSV")
          }

          Button(
            onClick = {
              val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "FeeTrack Transactions Export")
                putExtra(Intent.EXTRA_TEXT, csvData)
              }
              context.startActivity(Intent.createChooser(intent, "Share Transactions CSV"))
              onDismiss()
            },
            modifier = Modifier
              .weight(1f)
              .testTag("share_csv_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Share / Send")
          }
        }

        TextButton(
          onClick = onDismiss,
          modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
          Text("Close", color = TextSecondary)
        }
      }
    }
  }
}

@Composable
fun FeeReminderDialog(
  student: Student,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = SlateSurface)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(OwingRedContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = OwingRed)
          }
          Column {
            Text(
              text = "Fee Reminder Notice",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Text(
              text = "Guardian: ${student.guardianName}",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )
          }
        }

        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = SlateSurfaceVariant
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Outstanding Balance:",
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondary
            )
            Text(
              text = FeeTrackFormatters.formatCurrency(student.balance),
              style = MaterialTheme.typography.titleLarge.copy(color = OwingRed, fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Student: ${student.fullName} • ${student.className}",
              style = MaterialTheme.typography.bodySmall,
              color = TextPrimary
            )
          }
        }

        val messagePreview = "Dear ${student.guardianName}, this is a gentle reminder from Greenwood International Academy Bursary that the outstanding school fees for ${student.fullName} (${student.className}) is ${FeeTrackFormatters.formatCurrency(student.balance)}. Please arrange payment at your earliest convenience. Thank you."

        Text(
          text = "Message Preview:",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = TextSecondary
        )
        Text(
          text = messagePreview,
          style = MaterialTheme.typography.bodySmall,
          color = TextPrimary,
          modifier = Modifier
            .background(SlateBackground, RoundedCornerShape(8.dp))
            .padding(10.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Cancel")
          }

          Button(
            onClick = {
              val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Fee Payment Reminder - ${student.fullName}")
                putExtra(Intent.EXTRA_TEXT, messagePreview)
              }
              context.startActivity(Intent.createChooser(intent, "Send Fee Reminder"))
              onDismiss()
            },
            modifier = Modifier
              .weight(1f)
              .testTag("send_reminder_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Send SMS/Email")
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
  onDismiss: () -> Unit,
  onAddStudent: (name: String, adm: String, cls: String, gender: String, gName: String, gPhone: String, gEmail: String) -> Unit
) {
  var fullName by remember { mutableStateOf("") }
  var admissionNo by remember { mutableStateOf("ADM-2026-${(100..999).random()}") }
  var selectedClass by remember { mutableStateOf("Grade 10-Science") }
  var gender by remember { mutableStateOf("M") }
  var guardianName by remember { mutableStateOf("") }
  var guardianPhone by remember { mutableStateOf("") }
  var guardianEmail by remember { mutableStateOf("") }

  val classes = listOf(
    "Grade 7-A", "Grade 7-B",
    "Grade 8-A", "Grade 8-B",
    "Grade 9-A", "Grade 9-B",
    "Grade 10-Science", "Grade 10-Arts",
    "Grade 11-Science", "Grade 11-Commerce",
    "Grade 12-Senior A", "Grade 12-Senior B"
  )

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = SlateSurface)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Enroll New Student",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary
        )

        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it },
          label = { Text("Student Full Name *") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_student_name_input"),
          singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = admissionNo,
            onValueChange = { admissionNo = it },
            label = { Text("Admission No.") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )

          // Class Dropdown
          var classExpanded by remember { mutableStateOf(false) }
          ExposedDropdownMenuBox(
            expanded = classExpanded,
            onExpandedChange = { classExpanded = it },
            modifier = Modifier.weight(1f)
          ) {
            OutlinedTextField(
              value = selectedClass,
              onValueChange = {},
              readOnly = true,
              label = { Text("Class") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
              modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
              expanded = classExpanded,
              onDismissRequest = { classExpanded = false }
            ) {
              classes.forEach { cls ->
                DropdownMenuItem(
                  text = { Text(cls) },
                  onClick = {
                    selectedClass = cls
                    classExpanded = false
                  }
                )
              }
            }
          }
        }

        OutlinedTextField(
          value = guardianName,
          onValueChange = { guardianName = it },
          label = { Text("Guardian / Parent Name *") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = guardianPhone,
          onValueChange = { guardianPhone = it },
          label = { Text("Guardian Phone *") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = guardianEmail,
          onValueChange = { guardianEmail = it },
          label = { Text("Guardian Email") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (fullName.isNotBlank() && guardianName.isNotBlank()) {
                onAddStudent(
                  fullName,
                  admissionNo,
                  selectedClass,
                  gender,
                  guardianName,
                  guardianPhone.ifBlank { "+1 (555) 000-0000" },
                  guardianEmail.ifBlank { "parent@example.com" }
                )
              }
            },
            enabled = fullName.isNotBlank() && guardianName.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("save_new_student_button")
          ) {
            Text("Enroll & Assign Fees")
          }
        }
      }
    }
  }
}
