package com.example.feetrack.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FeeCategory(val displayName: String) {
  TUITION("Tuition Fee"),
  BOOKS("Books & Materials"),
  UNIFORM("Uniform & Sports"),
  LAB_TECH("Science & STEM Lab"),
  ICT_DEV("ICT & Digital Learning"),
  EXAM("Examination & Assessment"),
  PTA("PTA Development Levy")
}

enum class AcademicTerm(val label: String, val shortLabel: String) {
  TERM_1("Term 1 (First Term)", "Term 1"),
  TERM_2("Term 2 (Second Term)", "Term 2"),
  TERM_3("Term 3 (Third Term)", "Term 3")
}

enum class FeeStatus(val label: String) {
  PAID("Paid"),
  PARTIAL("Partial"),
  OWING("Owing")
}

enum class PaymentMethod(val label: String) {
  BANK_TRANSFER("Bank Transfer"),
  CARD_POS("Debit / POS Card"),
  CASH("Cash Deposit"),
  CHEQUE("Bank Cheque"),
  MOBILE_MONEY("Mobile Money")
}

data class FeeItem(
  val id: String,
  val category: FeeCategory,
  val name: String,
  val amount: Double
)

data class TermFeeBreakdown(
  val term: AcademicTerm,
  val academicYear: String = "2026/2027",
  val items: List<FeeItem>,
  val amountPaid: Double
) {
  val totalAssessed: Double
    get() = items.sumOf { it.amount }

  val balance: Double
    get() = (totalAssessed - amountPaid).coerceAtLeast(0.0)

  val status: FeeStatus
    get() = when {
      balance <= 0.01 -> FeeStatus.PAID
      amountPaid > 0.0 -> FeeStatus.PARTIAL
      else -> FeeStatus.OWING
    }
}

data class Student(
  val id: String,
  val admissionNo: String,
  val fullName: String,
  val className: String,
  val gender: String,
  val guardianName: String,
  val guardianPhone: String,
  val guardianEmail: String,
  val termBreakdowns: List<TermFeeBreakdown>
) {
  val totalFee: Double
    get() = termBreakdowns.sumOf { it.totalAssessed }

  val amountPaid: Double
    get() = termBreakdowns.sumOf { it.amountPaid }

  val balance: Double
    get() = (totalFee - amountPaid).coerceAtLeast(0.0)

  val status: FeeStatus
    get() = when {
      balance <= 0.01 -> FeeStatus.PAID
      amountPaid > 0.0 -> FeeStatus.PARTIAL
      else -> FeeStatus.OWING
    }
}

data class Payment(
  val id: String,
  val receiptNumber: String,
  val studentId: String,
  val studentName: String,
  val admissionNo: String,
  val className: String,
  val amount: Double,
  val paymentMethod: PaymentMethod,
  val dateMillis: Long,
  val term: AcademicTerm,
  val referenceNotes: String = "",
  val receivedBy: String = "Mrs. Eleanor Vance (Bursar)"
) {
  val formattedDate: String
    get() {
      val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
      return sdf.format(Date(dateMillis))
    }

  val formattedShortDate: String
    get() {
      val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
      return sdf.format(Date(dateMillis))
    }
}

data class FeeStructure(
  val className: String,
  val term: AcademicTerm,
  val items: List<FeeItem>
) {
  val totalAmount: Double
    get() = items.sumOf { it.amount }
}

data class AdminUser(
  val staffId: String,
  val name: String,
  val role: String,
  val department: String = "Finance & Bursary Office"
)

object FeeTrackFormatters {
  fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(amount)
  }

  fun formatCompactCurrency(amount: Double): String {
    return if (amount >= 1_000_000) {
      String.format(Locale.US, "$%.1fM", amount / 1_000_000.0)
    } else if (amount >= 1_000) {
      String.format(Locale.US, "$%.1fk", amount / 1_000.0)
    } else {
      String.format(Locale.US, "$%.0f", amount)
    }
  }
}
