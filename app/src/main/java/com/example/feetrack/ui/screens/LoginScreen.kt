package com.example.feetrack.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.feetrack.ui.FeeTrackViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
  viewModel: FeeTrackViewModel,
  modifier: Modifier = Modifier
) {
  var staffId by remember { mutableStateOf("BUR-2026-042") }
  var pinPassword by remember { mutableStateOf("••••••") }
  var selectedRole by remember { mutableStateOf("Senior Bursar") }
  var passwordVisible by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(SlateBackground),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .padding(24.dp)
        .widthIn(max = 500.dp)
        .fillMaxWidth(),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = SlateSurface),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(32.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        // App / Institution Emblem
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(TealContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AccountBalance,
            contentDescription = "FeeTrack Crest",
            tint = TealPrimary,
            modifier = Modifier.size(40.dp)
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "FeeTrack",
            style = MaterialTheme.typography.displayMedium.copy(
              color = TealPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 30.sp
            )
          )
          Text(
            text = "School Fees Management & Bursary Portal",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(6.dp))
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = SlateSurfaceVariant
          ) {
            Text(
              text = "Academic Session 2026/2027",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = TealDark,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }

        HorizontalDivider(color = SlateBorder)

        // Role Selector Chips
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Administrative Role",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = TextSecondary
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = selectedRole == "Senior Bursar",
              onClick = { selectedRole = "Senior Bursar" },
              label = { Text("Bursar") },
              leadingIcon = {
                if (selectedRole == "Senior Bursar") {
                  Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                }
              },
              modifier = Modifier.weight(1f)
            )
            FilterChip(
              selected = selectedRole == "Principal / Auditor",
              onClick = { selectedRole = "Principal / Auditor" },
              label = { Text("Auditor / Principal") },
              leadingIcon = {
                if (selectedRole == "Principal / Auditor") {
                  Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                }
              },
              modifier = Modifier.weight(1f)
            )
          }
        }

        // Staff ID Input
        OutlinedTextField(
          value = staffId,
          onValueChange = { staffId = it },
          label = { Text("Bursar / Staff ID") },
          leadingIcon = {
            Icon(Icons.Default.Badge, contentDescription = null, tint = TealPrimary)
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("login_staff_id_input"),
          singleLine = true
        )

        // Security PIN / Password
        OutlinedTextField(
          value = pinPassword,
          onValueChange = { pinPassword = it },
          label = { Text("Workstation PIN") },
          leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = TealPrimary)
          },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = null
              )
            }
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("login_pin_input"),
          singleLine = true
        )

        // Main Login Button
        Button(
          onClick = {
            val name = if (selectedRole.contains("Bursar")) "Mrs. Eleanor Vance" else "Dr. Raymond Vance"
            viewModel.login(staffId, selectedRole, name)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("login_submit_button"),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Login, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Access Dashboard",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
          )
        }

        // Quick Demo Helper
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = SlateSurfaceVariant
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Fast Demo One-Click Access:",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = TextSecondary
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = {
                  viewModel.login("BUR-2026-042", "Senior Bursar", "Mrs. Eleanor Vance")
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("demo_bursar_button"),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Bursar Access", fontSize = 12.sp)
              }

              OutlinedButton(
                onClick = {
                  viewModel.login("PRN-2026-001", "Principal / Auditor", "Dr. Raymond Vance")
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("demo_principal_button"),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Auditor Access", fontSize = 12.sp)
              }
            }
          }
        }

        // Security Footnote
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = PaidGreen, modifier = Modifier.size(16.dp))
          Text(
            text = "Encrypted Bursary Channel • Audit Logged",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
          )
        }
      }
    }
  }
}
