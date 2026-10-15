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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.feetrack.ui.Screen
import com.example.feetrack.ui.components.FeeStatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsListScreen(
  viewModel: FeeTrackViewModel,
  modifier: Modifier = Modifier
) {
  val students by viewModel.filteredStudents.collectAsState()
  val searchQuery by viewModel.studentSearchQuery.collectAsState()
  val selectedClass by viewModel.studentClassFilter.collectAsState()
  val selectedStatus by viewModel.studentStatusFilter.collectAsState()
  val allStudents by viewModel.allStudents.collectAsState()

  val classes = remember(allStudents) {
    listOf("All Classes") + allStudents.map { it.className }.distinct().sorted()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SlateBackground)
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Title & Add Student Action
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Students Fee Registry",
          style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
          color = TextPrimary
        )
        Text(
          text = "Showing ${students.size} of ${allStudents.size} registered students",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )
      }

      Button(
        onClick = { viewModel.setShowAddStudentDialog(true) },
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("add_student_button")
      ) {
        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Add Student")
      }
    }

    // Filters Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Search TextField
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.setStudentSearchQuery(it) },
        placeholder = { Text("Search by student name or admission no...") },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
        },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { viewModel.setStudentSearchQuery("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        },
        modifier = Modifier
          .weight(1f)
          .testTag("student_search_input"),
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
          label = { Text("Class Filter") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
          modifier = Modifier
            .menuAnchor()
            .widthIn(min = 170.dp),
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
                viewModel.setStudentClassFilter(cls)
                classDropdownExpanded = false
              }
            )
          }
        }
      }
    }

    // Status Filter Tabs
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = selectedStatus == null,
        onClick = { viewModel.setStudentStatusFilter(null) },
        label = { Text("All (${allStudents.size})") },
        shape = RoundedCornerShape(20.dp)
      )
      FilterChip(
        selected = selectedStatus == FeeStatus.PAID,
        onClick = { viewModel.setStudentStatusFilter(FeeStatus.PAID) },
        label = { Text("Paid (${allStudents.count { it.status == FeeStatus.PAID }})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = PaidGreenContainer,
          selectedLabelColor = OnPaidGreen
        ),
        shape = RoundedCornerShape(20.dp)
      )
      FilterChip(
        selected = selectedStatus == FeeStatus.PARTIAL,
        onClick = { viewModel.setStudentStatusFilter(FeeStatus.PARTIAL) },
        label = { Text("Partial (${allStudents.count { it.status == FeeStatus.PARTIAL }})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = PartialAmberContainer,
          selectedLabelColor = OnPartialAmber
        ),
        shape = RoundedCornerShape(20.dp)
      )
      FilterChip(
        selected = selectedStatus == FeeStatus.OWING,
        onClick = { viewModel.setStudentStatusFilter(FeeStatus.OWING) },
        label = { Text("Owing (${allStudents.count { it.status == FeeStatus.OWING }})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = OwingRedContainer,
          selectedLabelColor = OnOwingRed
        ),
        shape = RoundedCornerShape(20.dp)
      )
    }

    // Responsive Table and Card Hybrid
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val isTabletTable = maxWidth >= 650.dp

      if (students.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
            Text("No students found matching current filters", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Button(
              onClick = {
                viewModel.setStudentSearchQuery("")
                viewModel.setStudentClassFilter("All Classes")
                viewModel.setStudentStatusFilter(null)
              },
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
              Text("Reset Filters")
            }
          }
        }
      } else if (isTabletTable) {
        // Tablet Table View
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
              Text(
                text = "STUDENT & ADMISSION",
                modifier = Modifier.weight(2.2f),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
              )
              Text(
                text = "CLASS",
                modifier = Modifier.weight(1.3f),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
              )
              Text(
                text = "TOTAL FEE",
                modifier = Modifier.weight(1.2f),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
              )
              Text(
                text = "PAID",
                modifier = Modifier.weight(1.2f),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
              )
              Text(
                text = "BALANCE",
                modifier = Modifier.weight(1.2f),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
              )
              Text(
                text = "STATUS",
                modifier = Modifier.weight(1.1f),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
              )
              Text(
                text = "ACTION",
                modifier = Modifier.weight(1.3f),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
              )
            }

            HorizontalDivider(color = SlateBorder)

            // Table Rows
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(vertical = 4.dp)
            ) {
              items(students) { student ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.StudentProfile(student.id)) }
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .testTag("student_row_${student.admissionNo}"),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Name & Adm
                  Row(
                    modifier = Modifier.weight(2.2f),
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
                      Text(
                        text = student.fullName.take(2).uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = TealPrimary
                      )
                    }
                    Column {
                      Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                      )
                      Text(
                        text = student.admissionNo,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                      )
                    }
                  }

                  // Class
                  Text(
                    text = student.className,
                    modifier = Modifier.weight(1.3f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                  )

                  // Total Fee
                  Text(
                    text = FeeTrackFormatters.formatCurrency(student.totalFee),
                    modifier = Modifier.weight(1.2f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                  )

                  // Paid
                  Text(
                    text = FeeTrackFormatters.formatCurrency(student.amountPaid),
                    modifier = Modifier.weight(1.2f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.SemiBold,
                      color = if (student.amountPaid > 0) PaidGreen else TextMuted
                    )
                  )

                  // Balance
                  Text(
                    text = FeeTrackFormatters.formatCurrency(student.balance),
                    modifier = Modifier.weight(1.2f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (student.balance > 0) OwingRed else TextMuted
                    )
                  )

                  // Status Badge
                  Box(modifier = Modifier.weight(1.1f)) {
                    FeeStatusBadge(status = student.status)
                  }

                  // Action Button
                  Row(
                    modifier = Modifier.weight(1.3f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    FilledTonalButton(
                      onClick = { viewModel.navigateTo(Screen.StudentProfile(student.id)) },
                      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                      shape = RoundedCornerShape(8.dp)
                    ) {
                      Text("Profile", fontSize = 12.sp)
                    }

                    if (student.balance > 0) {
                      IconButton(
                        onClick = { viewModel.navigateTo(Screen.RecordPayment(student.id)) },
                        modifier = Modifier.size(32.dp)
                      ) {
                        Icon(
                          Icons.Default.Payment,
                          contentDescription = "Pay",
                          tint = TealPrimary,
                          modifier = Modifier.size(18.dp)
                        )
                      }
                    }
                  }
                }
                HorizontalDivider(color = SlateBorder.copy(alpha = 0.6f))
              }
            }
          }
        }
      } else {
        // Compact Cards View
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(students) { student ->
            ElevatedCard(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo(Screen.StudentProfile(student.id)) },
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.elevatedCardColors(containerColor = SlateSurface)
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
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(TealContainer),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = student.fullName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TealPrimary
                      )
                    }
                    Column {
                      Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                      )
                      Text(
                        text = "${student.admissionNo} • ${student.className}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                      )
                    }
                  }

                  FeeStatusBadge(status = student.status)
                }

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateSurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column {
                    Text("Total Fee", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(FeeTrackFormatters.formatCurrency(student.totalFee), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                  }
                  Column {
                    Text("Paid", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(FeeTrackFormatters.formatCurrency(student.amountPaid), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = PaidGreen))
                  }
                  Column(horizontalAlignment = Alignment.End) {
                    Text("Balance", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(FeeTrackFormatters.formatCurrency(student.balance), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = if (student.balance > 0) OwingRed else TextPrimary))
                  }
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  OutlinedButton(
                    onClick = { viewModel.navigateTo(Screen.StudentProfile(student.id)) },
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text("View Profile", fontSize = 12.sp)
                  }
                  if (student.balance > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                      onClick = { viewModel.navigateTo(Screen.RecordPayment(student.id)) },
                      colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                      shape = RoundedCornerShape(8.dp)
                    ) {
                      Text("Pay Now", fontSize = 12.sp)
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
}
