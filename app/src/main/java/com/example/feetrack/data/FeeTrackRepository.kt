package com.example.feetrack.data

import com.example.feetrack.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class FeeTrackRepository {

  private val _currentUser = MutableStateFlow<AdminUser?>(
    AdminUser(
      staffId = "BUR-2026-042",
      name = "Mrs. Eleanor Vance",
      role = "Senior Bursar / Accounts Head"
    )
  )
  val currentUser: StateFlow<AdminUser?> = _currentUser.asStateFlow()

  private val _selectedAcademicTerm = MutableStateFlow(AcademicTerm.TERM_2)
  val selectedAcademicTerm: StateFlow<AcademicTerm> = _selectedAcademicTerm.asStateFlow()

  private val _students = MutableStateFlow<List<Student>>(emptyList())
  val students: StateFlow<List<Student>> = _students.asStateFlow()

  private val _payments = MutableStateFlow<List<Payment>>(emptyList())
  val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

  private val _feeStructures = MutableStateFlow<List<FeeStructure>>(emptyList())
  val feeStructures: StateFlow<List<FeeStructure>> = _feeStructures.asStateFlow()

  init {
    seedInitialData()
  }

  fun login(staffId: String, role: String, name: String) {
    _currentUser.value = AdminUser(
      staffId = staffId.ifBlank { "BUR-2026-042" },
      name = name.ifBlank { "Mrs. Eleanor Vance" },
      role = role
    )
  }

  fun logout() {
    _currentUser.value = null
  }

  fun setAcademicTerm(term: AcademicTerm) {
    _selectedAcademicTerm.value = term
  }

  fun generateReceiptNumber(): String {
    val count = _payments.value.size + 1045
    return "REC-2026-$count"
  }

  fun recordPayment(
    studentId: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    term: AcademicTerm,
    receiptNo: String,
    referenceNotes: String,
    dateMillis: Long = System.currentTimeMillis()
  ): Payment? {
    val studentList = _students.value.toMutableList()
    val studentIndex = studentList.indexOfFirst { it.id == studentId }
    if (studentIndex == -1) return null

    val currentStudent = studentList[studentIndex]
    val receipt = receiptNo.ifBlank { generateReceiptNumber() }

    val newPayment = Payment(
      id = UUID.randomUUID().toString(),
      receiptNumber = receipt,
      studentId = currentStudent.id,
      studentName = currentStudent.fullName,
      admissionNo = currentStudent.admissionNo,
      className = currentStudent.className,
      amount = amount,
      paymentMethod = paymentMethod,
      dateMillis = dateMillis,
      term = term,
      referenceNotes = referenceNotes,
      receivedBy = _currentUser.value?.name ?: "Bursar Office"
    )

    // Update student's payment allocation for the target term
    val updatedTerms = currentStudent.termBreakdowns.map { tb ->
      if (tb.term == term) {
        tb.copy(amountPaid = tb.amountPaid + amount)
      } else {
        tb
      }
    }

    studentList[studentIndex] = currentStudent.copy(termBreakdowns = updatedTerms)
    _students.value = studentList

    val updatedPayments = listOf(newPayment) + _payments.value
    _payments.value = updatedPayments

    return newPayment
  }

  fun addStudent(
    fullName: String,
    admissionNo: String,
    className: String,
    gender: String,
    guardianName: String,
    guardianPhone: String,
    guardianEmail: String
  ): Student {
    val classStructure = _feeStructures.value.filter { it.className == className }

    val termBreakdowns = AcademicTerm.entries.map { term ->
      val matched = classStructure.find { it.term == term }
      val items = matched?.items ?: defaultFeeItemsForClass(className)
      TermFeeBreakdown(
        term = term,
        academicYear = "2026/2027",
        items = items,
        amountPaid = 0.0
      )
    }

    val newStudent = Student(
      id = UUID.randomUUID().toString(),
      admissionNo = admissionNo,
      fullName = fullName,
      className = className,
      gender = gender,
      guardianName = guardianName,
      guardianPhone = guardianPhone,
      guardianEmail = guardianEmail,
      termBreakdowns = termBreakdowns
    )

    _students.value = listOf(newStudent) + _students.value
    return newStudent
  }

  fun updateFeeStructure(className: String, term: AcademicTerm, items: List<FeeItem>) {
    val existing = _feeStructures.value.toMutableList()
    val index = existing.indexOfFirst { it.className == className && it.term == term }
    val newStruct = FeeStructure(className = className, term = term, items = items)
    if (index >= 0) {
      existing[index] = newStruct
    } else {
      existing.add(newStruct)
    }
    _feeStructures.value = existing

    // Optionally recompute for students in this class whose assessed fee has changed
    val updatedStudents = _students.value.map { student ->
      if (student.className == className) {
        val updatedTerms = student.termBreakdowns.map { tb ->
          if (tb.term == term) {
            tb.copy(items = items)
          } else {
            tb
          }
        }
        student.copy(termBreakdowns = updatedTerms)
      } else {
        student
      }
    }
    _students.value = updatedStudents
  }

  fun exportPaymentsCsv(filteredList: List<Payment> = _payments.value): String {
    val sb = StringBuilder()
    sb.append("Receipt Number,Student Name,Admission No,Class,Term,Amount,Payment Method,Date,Received By,Notes\n")
    for (p in filteredList) {
      sb.append("\"${p.receiptNumber}\",")
      sb.append("\"${p.studentName}\",")
      sb.append("\"${p.admissionNo}\",")
      sb.append("\"${p.className}\",")
      sb.append("\"${p.term.shortLabel}\",")
      sb.append("${p.amount},")
      sb.append("\"${p.paymentMethod.label}\",")
      sb.append("\"${p.formattedShortDate}\",")
      sb.append("\"${p.receivedBy}\",")
      sb.append("\"${p.referenceNotes.replace("\"", "\"\"")}\"\n")
    }
    return sb.toString()
  }

  private fun defaultFeeItemsForClass(className: String): List<FeeItem> {
    val isSenior = className.contains("10") || className.contains("11") || className.contains("12")
    val tuition = if (isSenior) 1200.0 else 950.0
    val lab = if (isSenior) 250.0 else 150.0
    return listOf(
      FeeItem("f1", FeeCategory.TUITION, "Tuition & Academics", tuition),
      FeeItem("f2", FeeCategory.BOOKS, "Textbooks & Library Access", 180.0),
      FeeItem("f3", FeeCategory.UNIFORM, "Uniform & Sports Gear", 140.0),
      FeeItem("f4", FeeCategory.LAB_TECH, "Science & STEM Lab", lab),
      FeeItem("f5", FeeCategory.ICT_DEV, "ICT & Digital Learning", 100.0),
      FeeItem("f6", FeeCategory.EXAM, "Assessment & Examination", 80.0),
      FeeItem("f7", FeeCategory.PTA, "PTA & Development Levy", 60.0)
    )
  }

  private fun seedInitialData() {
    val classNames = listOf(
      "Grade 7-A", "Grade 7-B",
      "Grade 8-A", "Grade 8-B",
      "Grade 9-A", "Grade 9-B",
      "Grade 10-Science", "Grade 10-Arts",
      "Grade 11-Science", "Grade 11-Commerce",
      "Grade 12-Senior A", "Grade 12-Senior B"
    )

    // Seed Fee Structures
    val structures = mutableListOf<FeeStructure>()
    for (cls in classNames) {
      for (term in AcademicTerm.entries) {
        structures.add(FeeStructure(cls, term, defaultFeeItemsForClass(cls)))
      }
    }
    _feeStructures.value = structures

    // Seed Students
    val rawStudents = listOf(
      Triple("Alexander Sterling", "ADM-2024-001", "Grade 10-Science") to (Triple("Dr. Marcus Sterling", "+1 (555) 234-5678", "m.sterling@example.com") to "M"),
      Triple("Beatrice O'Connor", "ADM-2024-002", "Grade 10-Science") to (Triple("Claire O'Connor", "+1 (555) 345-6789", "c.oconnor@example.com") to "F"),
      Triple("Chidi Okafor", "ADM-2024-003", "Grade 12-Senior A") to (Triple("Engr. Emeka Okafor", "+1 (555) 456-7890", "e.okafor@example.com") to "M"),
      Triple("Diana Morales", "ADM-2024-004", "Grade 12-Senior A") to (Triple("Rosa Morales", "+1 (555) 567-8901", "rosa.m@example.com") to "F"),
      Triple("Ethan Montgomery", "ADM-2024-005", "Grade 9-A") to (Triple("Arthur Montgomery", "+1 (555) 678-9012", "amont@example.com") to "M"),
      Triple("Fatima Al-Mansoor", "ADM-2024-006", "Grade 9-A") to (Triple("Tariq Al-Mansoor", "+1 (555) 789-0123", "talmansoor@example.com") to "F"),
      Triple("Gabriel Santos", "ADM-2024-007", "Grade 8-A") to (Triple("Elena Santos", "+1 (555) 890-1234", "esantos@example.com") to "M"),
      Triple("Hannah Lindqvist", "ADM-2024-008", "Grade 8-A") to (Triple("Erik Lindqvist", "+1 (555) 901-2345", "elindqvist@example.com") to "F"),
      Triple("Isaac Nwachukwu", "ADM-2024-009", "Grade 11-Science") to (Triple("Obinna Nwachukwu", "+1 (555) 012-3456", "onwachukwu@example.com") to "M"),
      Triple("Jasmine Patel", "ADM-2024-010", "Grade 11-Commerce") to (Triple("Rohan Patel", "+1 (555) 123-4567", "rpatel@example.com") to "F"),
      Triple("Kofi Mensah", "ADM-2024-011", "Grade 7-A") to (Triple("Kwame Mensah", "+1 (555) 234-8901", "kmensah@example.com") to "M"),
      Triple("Luna Dubois", "ADM-2024-012", "Grade 7-A") to (Triple("Julien Dubois", "+1 (555) 345-9012", "jdubois@example.com") to "F"),
      Triple("Mateo Rossi", "ADM-2024-013", "Grade 10-Arts") to (Triple("Sofia Rossi", "+1 (555) 456-0123", "srossi@example.com") to "M"),
      Triple("Nadia Kowalski", "ADM-2024-014", "Grade 8-B") to (Triple("Piotr Kowalski", "+1 (555) 567-1234", "pkowalski@example.com") to "F"),
      Triple("Omar Farooq", "ADM-2024-015", "Grade 12-Senior B") to (Triple("Zainab Farooq", "+1 (555) 678-2345", "zfarooq@example.com") to "M"),
      Triple("Penelope Cruz-Wood", "ADM-2024-016", "Grade 9-B") to (Triple("David Wood", "+1 (555) 789-3456", "dwood@example.com") to "F"),
      Triple("Quentin Thorne", "ADM-2024-017", "Grade 7-B") to (Triple("Sarah Thorne", "+1 (555) 890-4567", "sthorne@example.com") to "M"),
      Triple("Riley Jenkins", "ADM-2024-018", "Grade 11-Science") to (Triple("Gary Jenkins", "+1 (555) 901-5678", "gjenkins@example.com") to "F")
    )

    val studentList = mutableListOf<Student>()
    val paymentList = mutableListOf<Payment>()
    var receiptCounter = 1001

    // Seed payments & payment progress
    rawStudents.forEachIndexed { idx, pair ->
      val (studentInfo, guardianPair) = pair
      val (name, adm, cls) = studentInfo
      val (guardianInfo, gender) = guardianPair
      val (gName, gPhone, gEmail) = guardianInfo
      val items = defaultFeeItemsForClass(cls)
      val termAssessed = items.sumOf { it.amount }

      // Assign realistic payment ratios based on index pattern
      // 0, 3, 7, 11: Fully Paid all terms
      // 1, 4, 8, 12, 15: Partial
      // 2, 6, 10, 14, 17: Owing in default
      val (t1Paid, t2Paid, t3Paid) = when (idx % 4) {
        0 -> Triple(termAssessed, termAssessed, termAssessed) // Fully paid
        1 -> Triple(termAssessed, termAssessed * 0.5, 0.0)    // Partial in Term 2
        2 -> Triple(termAssessed * 0.4, 0.0, 0.0)             // Owing / Default
        else -> Triple(termAssessed, termAssessed * 0.75, 0.0) // Partial
      }

      val breakdowns = listOf(
        TermFeeBreakdown(AcademicTerm.TERM_1, "2026/2027", items, t1Paid),
        TermFeeBreakdown(AcademicTerm.TERM_2, "2026/2027", items, t2Paid),
        TermFeeBreakdown(AcademicTerm.TERM_3, "2026/2027", items, t3Paid)
      )

      val student = Student(
        id = "std-$idx",
        admissionNo = adm,
        fullName = name,
        className = cls,
        gender = gender,
        guardianName = gName,
        guardianPhone = gPhone,
        guardianEmail = gEmail,
        termBreakdowns = breakdowns
      )
      studentList.add(student)

      // Generate corresponding payment records for whatever was paid
      val now = System.currentTimeMillis()
      val dayMillis = 86_400_000L

      if (t1Paid > 0) {
        paymentList.add(
          Payment(
            id = "pay-$receiptCounter",
            receiptNumber = "REC-2026-$receiptCounter",
            studentId = student.id,
            studentName = student.fullName,
            admissionNo = student.admissionNo,
            className = student.className,
            amount = t1Paid,
            paymentMethod = if (idx % 2 == 0) PaymentMethod.BANK_TRANSFER else PaymentMethod.CARD_POS,
            dateMillis = now - (60L * dayMillis) - (idx * 500000L),
            term = AcademicTerm.TERM_1,
            referenceNotes = "Term 1 Tuition & Fees settled in full",
            receivedBy = "Mrs. Eleanor Vance (Bursar)"
          )
        )
        receiptCounter++
      }

      if (t2Paid > 0) {
        paymentList.add(
          Payment(
            id = "pay-$receiptCounter",
            receiptNumber = "REC-2026-$receiptCounter",
            studentId = student.id,
            studentName = student.fullName,
            admissionNo = student.admissionNo,
            className = student.className,
            amount = t2Paid,
            paymentMethod = when (idx % 5) {
              0 -> PaymentMethod.BANK_TRANSFER
              1 -> PaymentMethod.CARD_POS
              2 -> PaymentMethod.CASH
              3 -> PaymentMethod.MOBILE_MONEY
              else -> PaymentMethod.CHEQUE
            },
            dateMillis = now - (14L * dayMillis) + (idx * 3600000L),
            term = AcademicTerm.TERM_2,
            referenceNotes = "Term 2 fee installment via portal",
            receivedBy = "Mrs. Eleanor Vance (Bursar)"
          )
        )
        receiptCounter++
      }

      if (t3Paid > 0) {
        paymentList.add(
          Payment(
            id = "pay-$receiptCounter",
            receiptNumber = "REC-2026-$receiptCounter",
            studentId = student.id,
            studentName = student.fullName,
            admissionNo = student.admissionNo,
            className = student.className,
            amount = t3Paid,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            dateMillis = now - (3L * dayMillis),
            term = AcademicTerm.TERM_3,
            referenceNotes = "Advance early settlement for Term 3",
            receivedBy = "Mrs. Eleanor Vance (Bursar)"
          )
        )
        receiptCounter++
      }
    }

    _students.value = studentList
    _payments.value = paymentList.sortedByDescending { it.dateMillis }
  }
}
