package com.example.feetrack.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feetrack.data.FeeTrackRepository
import com.example.feetrack.model.*
import kotlinx.coroutines.flow.*

sealed class Screen {
  data object Login : Screen()
  data object Dashboard : Screen()
  data object StudentsList : Screen()
  data class StudentProfile(val studentId: String) : Screen()
  data class RecordPayment(val preSelectedStudentId: String? = null) : Screen()
  data object FeeStructureSetup : Screen()
  data object PaymentHistory : Screen()
  data object Reports : Screen()
}

data class DashboardStats(
  val totalExpected: Double,
  val totalCollected: Double,
  val outstandingBalance: Double,
  val studentsInDefaultCount: Int,
  val totalStudentsCount: Int,
  val collectionRatePercent: Double,
  val recentPayments: List<Payment>
)

data class ClassCollectionReport(
  val className: String,
  val expected: Double,
  val collected: Double,
  val balance: Double,
  val ratePercent: Double,
  val studentCount: Int
)

data class MethodDistributionReport(
  val method: PaymentMethod,
  val totalAmount: Double,
  val count: Int,
  val percentageOfTotal: Double
)

class FeeTrackViewModel(
  private val repository: FeeTrackRepository = FeeTrackRepository()
) : ViewModel() {

  val currentUser = repository.currentUser
  val selectedAcademicTerm = repository.selectedAcademicTerm
  val allStudents = repository.students
  val allPayments = repository.payments
  val feeStructures = repository.feeStructures

  private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
  val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

  // Navigation history for back-handling
  private val navigationBackStack = mutableListOf<Screen>()

  // Student list filters
  private val _studentSearchQuery = MutableStateFlow("")
  val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

  private val _studentClassFilter = MutableStateFlow("All Classes")
  val studentClassFilter: StateFlow<String> = _studentClassFilter.asStateFlow()

  private val _studentStatusFilter = MutableStateFlow<FeeStatus?>(null)
  val studentStatusFilter: StateFlow<FeeStatus?> = _studentStatusFilter.asStateFlow()

  // Payment history filters
  private val _paymentSearchQuery = MutableStateFlow("")
  val paymentSearchQuery: StateFlow<String> = _paymentSearchQuery.asStateFlow()

  private val _paymentClassFilter = MutableStateFlow("All Classes")
  val paymentClassFilter: StateFlow<String> = _paymentClassFilter.asStateFlow()

  private val _paymentMethodFilter = MutableStateFlow<PaymentMethod?>(null)
  val paymentMethodFilter: StateFlow<PaymentMethod?> = _paymentMethodFilter.asStateFlow()

  // Modals / Dialogs
  private val _lastRecordedPayment = MutableStateFlow<Payment?>(null)
  val lastRecordedPayment: StateFlow<Payment?> = _lastRecordedPayment.asStateFlow()

  private val _showReceiptModal = MutableStateFlow(false)
  val showReceiptModal: StateFlow<Boolean> = _showReceiptModal.asStateFlow()

  private val _showExportDialog = MutableStateFlow(false)
  val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

  private val _showAddStudentDialog = MutableStateFlow(false)
  val showAddStudentDialog: StateFlow<Boolean> = _showAddStudentDialog.asStateFlow()

  private val _reminderStudent = MutableStateFlow<Student?>(null)
  val reminderStudent: StateFlow<Student?> = _reminderStudent.asStateFlow()

  private val _userFeedbackMessage = MutableStateFlow<String?>(null)
  val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

  // Filtered Students Flow
  val filteredStudents: StateFlow<List<Student>> = combine(
    allStudents,
    _studentSearchQuery,
    _studentClassFilter,
    _studentStatusFilter
  ) { students, query, classFilter, statusFilter ->
    students.filter { student ->
      val matchesQuery = query.isBlank() ||
          student.fullName.contains(query, ignoreCase = true) ||
          student.admissionNo.contains(query, ignoreCase = true)
      val matchesClass = classFilter == "All Classes" || student.className == classFilter
      val matchesStatus = statusFilter == null || student.status == statusFilter
      matchesQuery && matchesClass && matchesStatus
    }
  }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  // Filtered Payments Flow
  val filteredPayments: StateFlow<List<Payment>> = combine(
    allPayments,
    _paymentSearchQuery,
    _paymentClassFilter,
    _paymentMethodFilter
  ) { payments, query, classFilter, methodFilter ->
    payments.filter { payment ->
      val matchesQuery = query.isBlank() ||
          payment.studentName.contains(query, ignoreCase = true) ||
          payment.admissionNo.contains(query, ignoreCase = true) ||
          payment.receiptNumber.contains(query, ignoreCase = true)
      val matchesClass = classFilter == "All Classes" || payment.className == classFilter
      val matchesMethod = methodFilter == null || payment.paymentMethod == methodFilter
      matchesQuery && matchesClass && matchesMethod
    }
  }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  // Dashboard Stats Flow
  val dashboardStats: StateFlow<DashboardStats> = combine(
    allStudents,
    allPayments
  ) { students, payments ->
    val totalExp = students.sumOf { it.totalFee }
    val totalCol = students.sumOf { it.amountPaid }
    val balance = (totalExp - totalCol).coerceAtLeast(0.0)
    val inDefault = students.count { it.status == FeeStatus.OWING }
    val rate = if (totalExp > 0.0) (totalCol / totalExp) * 100.0 else 0.0
    val recent = payments.take(6)

    DashboardStats(
      totalExpected = totalExp,
      totalCollected = totalCol,
      outstandingBalance = balance,
      studentsInDefaultCount = inDefault,
      totalStudentsCount = students.size,
      collectionRatePercent = rate,
      recentPayments = recent
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.Lazily,
    DashboardStats(0.0, 0.0, 0.0, 0, 0, 0.0, emptyList())
  )

  // Reports data
  val classCollectionReports: StateFlow<List<ClassCollectionReport>> = allStudents.map { students ->
    students.groupBy { it.className }.map { (className, classStudents) ->
      val expected = classStudents.sumOf { it.totalFee }
      val collected = classStudents.sumOf { it.amountPaid }
      val balance = (expected - collected).coerceAtLeast(0.0)
      val rate = if (expected > 0.0) (collected / expected) * 100.0 else 0.0
      ClassCollectionReport(
        className = className,
        expected = expected,
        collected = collected,
        balance = balance,
        ratePercent = rate,
        studentCount = classStudents.size
      )
    }.sortedByDescending { it.expected }
  }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  val paymentMethodReports: StateFlow<List<MethodDistributionReport>> = allPayments.map { payments ->
    val totalSum = payments.sumOf { it.amount }
    PaymentMethod.entries.map { method ->
      val methodPayments = payments.filter { it.paymentMethod == method }
      val sum = methodPayments.sumOf { it.amount }
      val count = methodPayments.size
      val percent = if (totalSum > 0.0) (sum / totalSum) * 100.0 else 0.0
      MethodDistributionReport(method, sum, count, percent)
    }.filter { it.count > 0 }.sortedByDescending { it.totalAmount }
  }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  val defaultLeaderboard: StateFlow<List<Student>> = allStudents.map { students ->
    students.filter { it.balance > 0.0 }
      .sortedByDescending { it.balance }
  }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  // Navigation Methods
  fun navigateTo(screen: Screen) {
    if (_currentScreen.value != screen) {
      navigationBackStack.add(_currentScreen.value)
      _currentScreen.value = screen
    }
  }

  fun navigateBack(): Boolean {
    if (navigationBackStack.isNotEmpty()) {
      _currentScreen.value = navigationBackStack.removeAt(navigationBackStack.size - 1)
      return true
    } else if (_currentScreen.value !is Screen.Dashboard) {
      _currentScreen.value = Screen.Dashboard
      return true
    }
    return false
  }

  // Filter setters
  fun setStudentSearchQuery(query: String) { _studentSearchQuery.value = query }
  fun setStudentClassFilter(cls: String) { _studentClassFilter.value = cls }
  fun setStudentStatusFilter(status: FeeStatus?) { _studentStatusFilter.value = status }

  fun setPaymentSearchQuery(query: String) { _paymentSearchQuery.value = query }
  fun setPaymentClassFilter(cls: String) { _paymentClassFilter.value = cls }
  fun setPaymentMethodFilter(method: PaymentMethod?) { _paymentMethodFilter.value = method }

  fun setSelectedAcademicTerm(term: AcademicTerm) {
    repository.setAcademicTerm(term)
  }

  fun recordPayment(
    studentId: String,
    amount: Double,
    method: PaymentMethod,
    term: AcademicTerm,
    receiptNo: String,
    notes: String
  ): Payment? {
    val payment = repository.recordPayment(
      studentId = studentId,
      amount = amount,
      paymentMethod = method,
      term = term,
      receiptNo = receiptNo,
      referenceNotes = notes
    )
    if (payment != null) {
      _lastRecordedPayment.value = payment
      _showReceiptModal.value = true
      _userFeedbackMessage.value = "Payment recorded successfully! Receipt #${payment.receiptNumber}"
    }
    return payment
  }

  fun addStudent(
    name: String,
    adm: String,
    cls: String,
    gender: String,
    gName: String,
    gPhone: String,
    gEmail: String
  ) {
    repository.addStudent(
      fullName = name,
      admissionNo = adm,
      className = cls,
      gender = gender,
      guardianName = gName,
      guardianPhone = gPhone,
      guardianEmail = gEmail
    )
    _showAddStudentDialog.value = false
    _userFeedbackMessage.value = "Student $name registered successfully!"
  }

  fun updateFeeStructure(className: String, term: AcademicTerm, items: List<FeeItem>) {
    repository.updateFeeStructure(className, term, items)
    _userFeedbackMessage.value = "Fee structure updated for $className (${term.shortLabel})!"
  }

  fun generateReceiptNumber(): String = repository.generateReceiptNumber()

  fun getExportCsv(): String = repository.exportPaymentsCsv(filteredPayments.value)

  fun showReceipt(payment: Payment) {
    _lastRecordedPayment.value = payment
    _showReceiptModal.value = true
  }

  fun dismissReceiptModal() {
    _showReceiptModal.value = false
  }

  fun setShowExportDialog(show: Boolean) {
    _showExportDialog.value = show
  }

  fun setShowAddStudentDialog(show: Boolean) {
    _showAddStudentDialog.value = show
  }

  fun setReminderStudent(student: Student?) {
    _reminderStudent.value = student
  }

  fun clearFeedbackMessage() {
    _userFeedbackMessage.value = null
  }

  fun login(staffId: String, role: String, name: String) {
    repository.login(staffId, role, name)
    navigateTo(Screen.Dashboard)
  }

  fun logout() {
    repository.logout()
    navigateTo(Screen.Login)
  }
}
