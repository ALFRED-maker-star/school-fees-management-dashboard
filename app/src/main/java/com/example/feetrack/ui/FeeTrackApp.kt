package com.example.feetrack.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feetrack.model.AcademicTerm
import com.example.feetrack.ui.components.*
import com.example.feetrack.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

private data class NavItem(
  val title: String,
  val icon: ImageVector,
  val screen: Screen,
  val tag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeTrackApp(
  viewModel: FeeTrackViewModel,
  modifier: Modifier = Modifier
) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val activeTerm by viewModel.selectedAcademicTerm.collectAsState()

  // Dialog states
  val lastPayment by viewModel.lastRecordedPayment.collectAsState()
  val showReceipt by viewModel.showReceiptModal.collectAsState()
  val showExport by viewModel.showExportDialog.collectAsState()
  val showAddStudent by viewModel.showAddStudentDialog.collectAsState()
  val reminderStudent by viewModel.reminderStudent.collectAsState()
  val feedbackMessage by viewModel.userFeedbackMessage.collectAsState()

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(feedbackMessage) {
    if (feedbackMessage != null) {
      snackbarHostState.showSnackbar(feedbackMessage!!)
      viewModel.clearFeedbackMessage()
    }
  }

  // Handle back button for sub-screens
  BackHandler(enabled = currentScreen !is Screen.Dashboard && currentScreen !is Screen.Login) {
    viewModel.navigateBack()
  }

  // Navigation Items
  val navItems = listOf(
    NavItem("Dashboard", Icons.Default.Dashboard, Screen.Dashboard, "nav_dashboard"),
    NavItem("Students List", Icons.Default.People, Screen.StudentsList, "nav_students"),
    NavItem("Record Payment", Icons.Default.AddCard, Screen.RecordPayment(), "nav_record_payment"),
    NavItem("Fee Structure", Icons.Default.Tune, Screen.FeeStructureSetup, "nav_fee_structure"),
    NavItem("Payment History", Icons.Default.ReceiptLong, Screen.PaymentHistory, "nav_payment_history"),
    NavItem("Reports & Analytics", Icons.Default.BarChart, Screen.Reports, "nav_reports")
  )

  // If user is on Login screen
  if (currentScreen is Screen.Login || currentUser == null) {
    LoginScreen(viewModel = viewModel, modifier = modifier)
    return
  }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isTablet = maxWidth >= 720.dp

    if (isTablet) {
      // Tablet-First Permanent Sidebar Layout
      Row(modifier = Modifier.fillMaxSize()) {
        // Permanent Sidebar
        Surface(
          modifier = Modifier
            .width(280.dp)
            .fillMaxHeight(),
          color = SlateSurface,
          tonalElevation = 2.dp,
          shadowElevation = 3.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            // Top: Branding & Navigation
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
              // Logo & App Name
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TealContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = "FeeTrack",
                    tint = TealPrimary,
                    modifier = Modifier.size(26.dp)
                  )
                }

                Column {
                  Text(
                    text = "FeeTrack",
                    style = MaterialTheme.typography.titleLarge.copy(
                      fontWeight = FontWeight.ExtraBold,
                      color = TealPrimary,
                      fontSize = 22.sp
                    )
                  )
                  Text(
                    text = "Bursary & Fees Portal",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                  )
                }
              }

              // Academic Session & Term Switcher
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = SlateSurfaceVariant
              ) {
                Column(
                  modifier = Modifier.padding(10.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("2026/2027 Session", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Surface(shape = RoundedCornerShape(4.dp), color = TealContainer) {
                      Text(
                        activeTerm.shortLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TealDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }
                  Text(
                    "Greenwood Academy",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                  )
                }
              }

              HorizontalDivider(color = SlateBorder)

              // Nav Items List
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                navItems.forEach { item ->
                  val isSelected = when (item.screen) {
                    is Screen.Dashboard -> currentScreen is Screen.Dashboard
                    is Screen.StudentsList -> currentScreen is Screen.StudentsList || currentScreen is Screen.StudentProfile
                    is Screen.RecordPayment -> currentScreen is Screen.RecordPayment
                    is Screen.FeeStructureSetup -> currentScreen is Screen.FeeStructureSetup
                    is Screen.PaymentHistory -> currentScreen is Screen.PaymentHistory
                    is Screen.Reports -> currentScreen is Screen.Reports
                    else -> false
                  }

                  Surface(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp))
                      .clickable { viewModel.navigateTo(item.screen) }
                      .testTag(item.tag),
                    color = if (isSelected) TealContainer else Color.Transparent
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                      Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) TealPrimary else TextSecondary,
                        modifier = Modifier.size(22.dp)
                      )
                      Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                          color = if (isSelected) TealDark else TextPrimary
                        )
                      )
                    }
                  }
                }
              }
            }

            // Bottom: User Profile & Logout
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              color = SlateSurfaceVariant
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(TealPrimary),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = currentUser?.name?.take(2)?.uppercase() ?: "EV",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = Color.White
                    )
                  }

                  Column {
                    Text(
                      text = currentUser?.name ?: "Bursar",
                      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                      color = TextPrimary
                    )
                    Text(
                      text = currentUser?.staffId ?: "BUR-2026-042",
                      style = MaterialTheme.typography.labelSmall,
                      color = TextMuted
                    )
                  }
                }

                IconButton(
                  onClick = { viewModel.logout() },
                  modifier = Modifier
                    .size(32.dp)
                    .testTag("logout_button")
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    tint = OwingRed,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }

        // Main Content Area
        Scaffold(
          snackbarHost = { SnackbarHost(snackbarHostState) },
          containerColor = SlateBackground,
          modifier = Modifier.weight(1f)
        ) { innerPadding ->
          ScreenContent(
            screen = currentScreen,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    } else {
      // Mobile / Compact Screen with Modal Navigation Drawer
      ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
          ModalDrawerSheet(
            modifier = Modifier.width(300.dp),
            drawerContainerColor = SlateSurface
          ) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(42.dp)
                      .clip(RoundedCornerShape(10.dp))
                      .background(TealContainer),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = TealPrimary)
                  }
                  Column {
                    Text("FeeTrack", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TealPrimary))
                    Text("Bursary Management", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                  }
                }

                HorizontalDivider(color = SlateBorder)

                navItems.forEach { item ->
                  val isSelected = when (item.screen) {
                    is Screen.Dashboard -> currentScreen is Screen.Dashboard
                    is Screen.StudentsList -> currentScreen is Screen.StudentsList
                    is Screen.RecordPayment -> currentScreen is Screen.RecordPayment
                    is Screen.FeeStructureSetup -> currentScreen is Screen.FeeStructureSetup
                    is Screen.PaymentHistory -> currentScreen is Screen.PaymentHistory
                    is Screen.Reports -> currentScreen is Screen.Reports
                    else -> false
                  }

                  NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    label = { Text(item.title) },
                    selected = isSelected,
                    onClick = {
                      viewModel.navigateTo(item.screen)
                      scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(vertical = 2.dp)
                  )
                }
              }

              // Drawer Footer
              Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = OwingRedContainer),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = OnOwingRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", color = OnOwingRed)
              }
            }
          }
        }
      ) {
        Scaffold(
          topBar = {
            TopAppBar(
              title = {
                Text(
                  text = when (currentScreen) {
                    is Screen.Dashboard -> "FeeTrack Dashboard"
                    is Screen.StudentsList -> "Students Registry"
                    is Screen.StudentProfile -> "Student Fee Profile"
                    is Screen.RecordPayment -> "Record Payment"
                    is Screen.FeeStructureSetup -> "Fee Structure"
                    is Screen.PaymentHistory -> "Payment History"
                    is Screen.Reports -> "Reports & Analytics"
                    else -> "FeeTrack"
                  },
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
              },
              navigationIcon = {
                if (currentScreen !is Screen.Dashboard) {
                  IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                  }
                } else {
                  IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                  }
                }
              },
              actions = {
                IconButton(onClick = { viewModel.navigateTo(Screen.RecordPayment()) }) {
                  Icon(Icons.Default.AddCircle, contentDescription = "Pay", tint = TealPrimary)
                }
              },
              colors = TopAppBarDefaults.topAppBarColors(
                containerColor = SlateSurface
              )
            )
          },
          snackbarHost = { SnackbarHost(snackbarHostState) },
          containerColor = SlateBackground
        ) { innerPadding ->
          ScreenContent(
            screen = currentScreen,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }

  // Modals & Dialogs
  if (showReceipt && lastPayment != null) {
    ReceiptDialog(
      payment = lastPayment!!,
      onDismiss = { viewModel.dismissReceiptModal() }
    )
  }

  if (showExport) {
    ExportDialog(
      csvData = viewModel.getExportCsv(),
      transactionCount = viewModel.filteredPayments.collectAsState().value.size,
      onDismiss = { viewModel.setShowExportDialog(false) }
    )
  }

  if (showAddStudent) {
    AddStudentDialog(
      onDismiss = { viewModel.setShowAddStudentDialog(false) },
      onAddStudent = { name, adm, cls, gen, gName, gPhone, gEmail ->
        viewModel.addStudent(name, adm, cls, gen, gName, gPhone, gEmail)
      }
    )
  }

  if (reminderStudent != null) {
    FeeReminderDialog(
      student = reminderStudent!!,
      onDismiss = { viewModel.setReminderStudent(null) }
    )
  }
}

@Composable
private fun ScreenContent(
  screen: Screen,
  viewModel: FeeTrackViewModel,
  modifier: Modifier = Modifier
) {
  when (screen) {
    is Screen.Dashboard -> DashboardScreen(viewModel = viewModel, modifier = modifier)
    is Screen.StudentsList -> StudentsListScreen(viewModel = viewModel, modifier = modifier)
    is Screen.StudentProfile -> StudentProfileScreen(studentId = screen.studentId, viewModel = viewModel, modifier = modifier)
    is Screen.RecordPayment -> RecordPaymentScreen(preSelectedStudentId = screen.preSelectedStudentId, viewModel = viewModel, modifier = modifier)
    is Screen.FeeStructureSetup -> FeeStructureScreen(viewModel = viewModel, modifier = modifier)
    is Screen.PaymentHistory -> PaymentHistoryScreen(viewModel = viewModel, modifier = modifier)
    is Screen.Reports -> ReportsScreen(viewModel = viewModel, modifier = modifier)
    is Screen.Login -> LoginScreen(viewModel = viewModel, modifier = modifier)
  }
}
