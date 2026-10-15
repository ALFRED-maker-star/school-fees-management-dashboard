package com.example.feetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feetrack.model.*
import com.example.feetrack.ui.FeeTrackViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeStructureScreen(
  viewModel: FeeTrackViewModel,
  modifier: Modifier = Modifier
) {
  val allFeeStructures by viewModel.feeStructures.collectAsState()

  val classes = listOf(
    "Grade 7-A", "Grade 7-B",
    "Grade 8-A", "Grade 8-B",
    "Grade 9-A", "Grade 9-B",
    "Grade 10-Science", "Grade 10-Arts",
    "Grade 11-Science", "Grade 11-Commerce",
    "Grade 12-Senior A", "Grade 12-Senior B"
  )

  var selectedClass by remember { mutableStateOf(classes.first()) }
  var selectedTerm by remember { mutableStateOf(AcademicTerm.TERM_2) }

  // Current editable items state
  val currentStructure = remember(selectedClass, selectedTerm, allFeeStructures) {
    allFeeStructures.find { it.className == selectedClass && it.term == selectedTerm }
  }

  var editableItems by remember(currentStructure) {
    mutableStateOf(currentStructure?.items ?: emptyList())
  }

  val totalTermAmount = remember(editableItems) {
    editableItems.sumOf { it.amount }
  }

  var showAddCategoryDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(SlateBackground)
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // Title
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Fee Structure Configuration",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
          Text(
            text = "Define and tune fee amounts per class, academic term, and category",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }

        Button(
          onClick = {
            viewModel.updateFeeStructure(selectedClass, selectedTerm, editableItems)
          },
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("save_fee_structure_btn")
        ) {
          Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Save Structure")
        }
      }
    }

    // Class Selector Tabs
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Select Academic Class",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = TextSecondary
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          classes.forEach { cls ->
            FilterChip(
              selected = selectedClass == cls,
              onClick = { selectedClass = cls },
              label = { Text(cls) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TealContainer,
                selectedLabelColor = TealDark
              ),
              shape = RoundedCornerShape(20.dp)
            )
          }
        }
      }
    }

    // Term Selector
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Academic Term: ",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = TextSecondary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          AcademicTerm.entries.forEach { term ->
            FilterChip(
              selected = selectedTerm == term,
              onClick = { selectedTerm = term },
              label = { Text(term.label) },
              shape = RoundedCornerShape(10.dp)
            )
          }
        }
      }
    }

    // Fee Items Breakdown Card
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
            Text(
              text = "$selectedClass • ${selectedTerm.label}",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )

            TextButton(onClick = { showAddCategoryDialog = true }) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = TealPrimary)
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add Fee Item", color = TealPrimary)
            }
          }

          HorizontalDivider(color = SlateBorder)

          // Table Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("CATEGORY & ITEM NAME", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextMuted)
            Text("AMOUNT ($)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextMuted)
          }

          // Editable Category Rows
          editableItems.forEachIndexed { index, item ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1.8f)
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TealContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = when (item.category) {
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
                  Text(item.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                  Text(item.category.displayName, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
              }

              // Amount TextField
              OutlinedTextField(
                value = if (item.amount == 0.0) "" else item.amount.toString(),
                onValueChange = { newVal ->
                  val parsed = newVal.toDoubleOrNull() ?: 0.0
                  val updated = editableItems.toMutableList()
                  updated[index] = item.copy(amount = parsed)
                  editableItems = updated
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                  .width(130.dp)
                  .testTag("fee_amount_input_${item.id}"),
                shape = RoundedCornerShape(8.dp)
              )
            }
            HorizontalDivider(color = SlateBorder.copy(alpha = 0.5f))
          }

          // Total Calculation Bar
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SlateSurfaceVariant
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Assessed Total per Student",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimary
                )
                Text(
                  text = "Estimated annual: ${FeeTrackFormatters.formatCurrency(totalTermAmount * 3)}",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondary
                )
              }

              Text(
                text = FeeTrackFormatters.formatCurrency(totalTermAmount),
                style = MaterialTheme.typography.titleLarge.copy(
                  fontSize = 24.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = TealPrimary
                )
              )
            }
          }
        }
      }
    }
  }

  // Dialog to Add Custom Category Item
  if (showAddCategoryDialog) {
    var newName by remember { mutableStateOf("") }
    var newAmount by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf(FeeCategory.TUITION) }

    AlertDialog(
      onDismissRequest = { showAddCategoryDialog = false },
      title = { Text("Add New Fee Item") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text("Fee Item Description") },
            placeholder = { Text("e.g. Robotics Kit / Graduation Cap") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = newAmount,
            onValueChange = { newAmount = it },
            label = { Text("Amount ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amt = newAmount.toDoubleOrNull() ?: 0.0
            if (newName.isNotBlank() && amt > 0) {
              val newItem = FeeItem(
                id = "custom-${System.currentTimeMillis()}",
                category = newCategory,
                name = newName,
                amount = amt
              )
              editableItems = editableItems + newItem
              showAddCategoryDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Text("Add Item")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddCategoryDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
