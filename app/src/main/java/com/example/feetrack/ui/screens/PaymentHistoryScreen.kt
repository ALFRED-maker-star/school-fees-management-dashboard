package com.example.feetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
  viewModel: FeeTrackViewModel,
  modifier: Modifier = Modifier
) {
  val payments by viewModel.filteredPayments.collectAsState()
  val allPayments by viewModel.allPayments.collectAsState()
  val allStudents by viewModel.allStudents.collectAsState()

  val searchQuery by viewModel.paymentSearchQuery.collectAsState()
  val selectedClass by viewModel.paymentClassFilter.collectAsState()
  val selectedMethod by viewModel.paymentMethodFilter.collectAsState()

  val classes = remember(allStudents) {
    listOf("All Classes") + allStudents.map { it.className }.distinct().sorted()
  }

  val totalFilteredSum = remember(payments) {
    payments.sumOf { it.amount }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SlateBackground)
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Title & Export Button
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Fee Payment Audit Log",
          style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
          color = TextPrimary
        )
        Text(
          text = "Official transaction receipts & financial clearance audit",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )
      }

      Button(
        onClick = { viewModel.setShowExportDialog(true) },
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("export_transactions_btn")
      ) {
        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Export CSV")
      }
    }

    // Filter Controls
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Search Box
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.setPaymentSearchQuery(it) },
        placeholder = { Text("Search receipt, student or admission...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { viewModel.setPaymentSearchQuery("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        },
        modifier = Modifier
          .weight(1f)
          .testTag("payment_search_input"),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      // Class Filter Dropdown
      var classDropdownExpanded by remember { mutableStateOf(false) }
      ExposedDropdownMenuBox(
        expanded = classDropdownExpanded,
        onExpandedChange = { classDropdownExpanded = it }
      ) {
        OutlinedTextField(
          value = selectedClass,
          onValueChange = {},
          readOnly = true,
          label = { Text("Class") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
          modifier = Modifier
            .menuAnchor()
            .widthIn(min = 160.dp),
          shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
          expanded = classDropdownExpanded,
          onDismissRequest = { classDropdownExpanded = false }
        ) {
          classes.forEach { cls ->
            DropdownMenuItem(
              text = { Text(cls) },
              onClick = {
                viewModel.setPaymentClassFilter(cls)
                classDropdownExpanded = false
              }
            )
          }
        }
      }
    }

    // Payment Method Filters Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = selectedMethod == null,
        onClick = { viewModel.setPaymentMethodFilter(null) },
        label = { Text("All Methods (${allPayments.size})") },
        shape = RoundedCornerShape(20.dp)
      )
      PaymentMethod.entries.forEach { method ->
        val count = allPayments.count { it.paymentMethod == method }
        FilterChip(
          selected = selectedMethod == method,
          onClick = { viewModel.setPaymentMethodFilter(method) },
          label = { Text("${method.label} ($count)") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = TealContainer,
            selectedLabelColor = TealDark
          ),
          shape = RoundedCornerShape(20.dp)
        )
      }
    }

    // Summary Banner
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      color = TealContainer.copy(alpha = 0.4f),
      border = androidx.compose.foundation.BorderStroke(1.dp, TealContainer)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.FactCheck, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
          Text(
            text = "${payments.size} Transactions Filtered",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = TealDark
          )
        }

        Text(
          text = "Sum: ${FeeTrackFormatters.formatCurrency(totalFilteredSum)}",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
          color = TealDark
        )
      }
    }

    // Transaction Log Table and Card Hybrid
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val isTablet = maxWidth >= 650.dp

      if (payments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
            Text("No transactions match the selected filters", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Button(
              onClick = {
                viewModel.setPaymentSearchQuery("")
                viewModel.setPaymentClassFilter("All Classes")
                viewModel.setPaymentMethodFilter(null)
              },
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
              Text("Clear Filters")
            }
          }
        }
      } else if (isTablet) {
        ElevatedCard(
          modifier = Modifier.fillMaxSize(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.elevatedCardColors(containerColor = SlateSurface),
          elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.fillMaxSize()) {
            // Table Header
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(SlateSurfaceVariant)
                .padding(horizontal = 20.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("RECEIPT NO.", modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
              Text("STUDENT & CLASS", modifier = Modifier.weight(2.0f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
              Text("TERM", modifier = Modifier.weight(1.0f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
              Text("AMOUNT", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
              Text("METHOD", modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
              Text("DATE", modifier = Modifier.weight(1.4f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
              Text("RECEIPT", modifier = Modifier.weight(0.9f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
            }

            HorizontalDivider(color = SlateBorder)

            // Table Rows
            LazyColumn(modifier = Modifier.fillMaxSize()) {
              items(payments) { payment ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showReceipt(payment) }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .testTag("payment_row_${payment.receiptNumber}"),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Receipt No
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SlateSurfaceVariant,
                    modifier = Modifier.weight(1.3f)
                  ) {
                    Text(
                      text = payment.receiptNumber,
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = TealDark,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                  }

                  // Student & Class
                  Column(modifier = Modifier.weight(2.0f)) {
                    Text(payment.studentName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                    Text("${payment.admissionNo} • ${payment.className}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                  }

                  // Term
                  Text(payment.term.shortLabel, modifier = Modifier.weight(1.0f), style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                  // Amount
                  Text(
                    text = FeeTrackFormatters.formatCurrency(payment.amount),
                    modifier = Modifier.weight(1.2f),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = PaidGreen)
                  )

                  // Method
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SlateSurfaceVariant,
                    modifier = Modifier.weight(1.3f)
                  ) {
                    Text(
                      text = payment.paymentMethod.label,
                      style = MaterialTheme.typography.labelSmall,
                      color = TextSecondary,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                  }

                  // Date
                  Text(payment.formattedShortDate, modifier = Modifier.weight(1.4f), style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                  // View Button
                  IconButton(
                    onClick = { viewModel.showReceipt(payment) },
                    modifier = Modifier.weight(0.9f)
                  ) {
                    Icon(Icons.Default.Receipt, contentDescription = "View", tint = TealPrimary, modifier = Modifier.size(20.dp))
                  }
                }
                HorizontalDivider(color = SlateBorder.copy(alpha = 0.5f))
              }
            }
          }
        }
      } else {
        // Compact Cards
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(payments) { payment ->
            ElevatedCard(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.showReceipt(payment) },
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.elevatedCardColors(containerColor = SlateSurface)
            ) {
              Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(payment.studentName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                  Text(
                    text = FeeTrackFormatters.formatCurrency(payment.amount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PaidGreen)
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("${payment.admissionNo} • ${payment.className}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                  Surface(shape = RoundedCornerShape(4.dp), color = SlateSurfaceVariant) {
                    Text(payment.receiptNumber, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TealDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                  }
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("${payment.formattedShortDate} • ${payment.paymentMethod.label}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                  TextButton(onClick = { viewModel.showReceipt(payment) }) {
                    Text("View Receipt", fontSize = 12.sp, color = TealPrimary)
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
