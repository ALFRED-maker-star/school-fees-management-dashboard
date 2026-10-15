package com.example.feetrack.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feetrack.model.*
import com.example.feetrack.ui.FeeTrackViewModel
import com.example.feetrack.ui.Screen
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentScreen(
  preSelectedStudentId: String? = null,
  viewModel: FeeTrackViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateBack()
  }

  val allStudents by viewModel.allStudents.collectAsState()
  val activeTerm by viewModel.selectedAcademicTerm.collectAsState()

  var selectedStudent by remember(preSelectedStudentId, allStudents) {
    mutableStateOf(allStudents.find { it.id == preSelectedStudentId })
  }

  var studentSearchQuery by remember { mutableStateOf("") }
  var isStudentDropdownOpen by remember { mutableStateOf(false) }

  var amountText by remember { mutableStateOf("") }
  var selectedMethod by remember { mutableStateOf(PaymentMethod.BANK_TRANSFER) }
  var selectedTerm by remember { mutableStateOf(activeTerm) }
  var receiptNumber by remember { mutableStateOf(viewModel.generateReceiptNumber()) }
  var referenceNotes by remember { mutableStateOf("") }
  val dateFormatted = remember {
    SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
  }

  val matchingStudents = remember(studentSearchQuery, allStudents) {
    if (studentSearchQuery.isBlank()) allStudents.take(8)
    else allStudents.filter {
      it.fullName.contains(studentSearchQuery, ignoreCase = true) ||
          it.admissionNo.contains(studentSearchQuery, ignoreCase = true)
    }.take(8)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(SlateBackground)
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // Top Bar
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
          IconButton(onClick = { viewModel.navigateBack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
          Column {
            Text(
              text = "Record Fee Payment",
              style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Text(
              text = "Process fee collection & generate official receipt",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )
          }
        }
      }
    }

    // Student Selection Card
    item {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SlateSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text(
            text = "1. Student Identification",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )

          // Auto-complete search / selector
          ExposedDropdownMenuBox(
            expanded = isStudentDropdownOpen,
            onExpandedChange = { isStudentDropdownOpen = it }
          ) {
            OutlinedTextField(
              value = selectedStudent?.let { "${it.fullName} (${it.admissionNo}) • ${it.className}" } ?: studentSearchQuery,
              onValueChange = {
                studentSearchQuery = it
                selectedStudent = null
                isStudentDropdownOpen = true
              },
              label = { Text("Search Student by Name or Admission No. *") },
              placeholder = { Text("Type name e.g. Alexander, Diana...") },
              leadingIcon = { Icon(Icons.Default.PersonSearch, contentDescription = null, tint = TealPrimary) },
              trailingIcon = {
                if (selectedStudent != null) {
                  IconButton(onClick = {
                    selectedStudent = null
                    studentSearchQuery = ""
                    amountText = ""
                  }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                  }
                } else {
                  ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStudentDropdownOpen)
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .testTag("record_payment_student_search"),
              singleLine = true,
              shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
              expanded = isStudentDropdownOpen,
              onDismissRequest = { isStudentDropdownOpen = false }
            ) {
              matchingStudents.forEach { std ->
                DropdownMenuItem(
                  text = {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column {
                        Text(std.fullName, fontWeight = FontWeight.SemiBold)
                        Text("${std.admissionNo} • ${std.className}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                      }
                      Text(
                        text = "Due: ${FeeTrackFormatters.formatCurrency(std.balance)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                          fontWeight = FontWeight.Bold,
                          color = if (std.balance > 0) OwingRed else PaidGreen
                        )
                      )
                    }
                  },
                  onClick = {
                    selectedStudent = std
                    studentSearchQuery = ""
                    isStudentDropdownOpen = false
                    // Auto suggest balance
                    if (std.balance > 0) {
                      amountText = std.balance.toInt().toString()
                    }
                  }
                )
              }
            }
          }

          // Selected Student Summary Banner
          if (selectedStudent != null) {
            val student = selectedStudent!!
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
                  Text(student.fullName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                  Text("Guardian: ${student.guardianName} (${student.guardianPhone})", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                  Text("Class: ${student.className}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text("Current Balance Due", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                  Text(
                    text = FeeTrackFormatters.formatCurrency(student.balance),
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (student.balance > 0) OwingRed else PaidGreen
                    )
                  )
                }
              }
            }
          }
        }
      }
    }

    // Payment Details Card
    item {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SlateSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Text(
            text = "2. Transaction Information",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )

          // Amount Input & Quick Buttons
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = amountText,
              onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
              label = { Text("Payment Amount ($) *") },
              leadingIcon = {
                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = TealPrimary)
              },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("record_payment_amount_input"),
              singleLine = true,
              shape = RoundedCornerShape(12.dp)
            )

            // Quick Amount Buttons
            if (selectedStudent != null && selectedStudent!!.balance > 0) {
              val bal = selectedStudent!!.balance
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = { amountText = bal.toInt().toString() },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("Full: ${FeeTrackFormatters.formatCompactCurrency(bal)}", fontSize = 12.sp)
                }

                OutlinedButton(
                  onClick = { amountText = (bal / 2.0).toInt().toString() },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("50%: ${FeeTrackFormatters.formatCompactCurrency(bal / 2.0)}", fontSize = 12.sp)
                }

                OutlinedButton(
                  onClick = { amountText = "500" },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("$500", fontSize = 12.sp)
                }

                OutlinedButton(
                  onClick = { amountText = "1000" },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("$1,000", fontSize = 12.sp)
                }
              }
            }
          }

          // Payment Method Selector
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "Payment Method *",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = TextSecondary
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              PaymentMethod.entries.forEach { method ->
                val isSelected = selectedMethod == method
                FilterChip(
                  selected = isSelected,
                  onClick = { selectedMethod = method },
                  label = { Text(method.label, fontSize = 12.sp) },
                  leadingIcon = {
                    if (isSelected) {
                      Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TealContainer,
                    selectedLabelColor = TealDark
                  ),
                  shape = RoundedCornerShape(8.dp)
                )
              }
            }
          }

          // Academic Term & Date
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = "Target Term *",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
              )
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AcademicTerm.entries.forEach { term ->
                  FilterChip(
                    selected = selectedTerm == term,
                    onClick = { selectedTerm = term },
                    label = { Text(term.shortLabel, fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                  )
                }
              }
            }

            OutlinedTextField(
              value = dateFormatted,
              onValueChange = {},
              readOnly = true,
              label = { Text("Payment Date") },
              leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp)) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            )
          }

          // Auto-generated Receipt Number
          OutlinedTextField(
            value = receiptNumber,
            onValueChange = { receiptNumber = it },
            label = { Text("Receipt Number (Auto-Generated) *") },
            leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null, tint = TealPrimary) },
            trailingIcon = {
              IconButton(onClick = { receiptNumber = viewModel.generateReceiptNumber() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Regenerate")
              }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )

          // Reference & Notes
          OutlinedTextField(
            value = referenceNotes,
            onValueChange = { referenceNotes = it },
            label = { Text("Reference / Bank Teller No. / Remarks") },
            placeholder = { Text("e.g. GTB-TXN-847291 or Direct POS transfer") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )

          // Save Payment Button
          val isFormValid = selectedStudent != null && (amountText.toDoubleOrNull() ?: 0.0) > 0.0
          Button(
            onClick = {
              val amt = amountText.toDoubleOrNull() ?: 0.0
              if (selectedStudent != null && amt > 0.0) {
                viewModel.recordPayment(
                  studentId = selectedStudent!!.id,
                  amount = amt,
                  method = selectedMethod,
                  term = selectedTerm,
                  receiptNo = receiptNumber,
                  notes = referenceNotes
                )
              }
            },
            enabled = isFormValid,
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("save_payment_button")
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Save Payment & Issue Receipt",
              style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
          }
        }
      }
    }
  }
}
