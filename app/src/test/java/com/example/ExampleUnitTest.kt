package com.example

import com.example.feetrack.data.FeeTrackRepository
import com.example.feetrack.model.AcademicTerm
import com.example.feetrack.model.FeeStatus
import com.example.feetrack.model.PaymentMethod
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

  private lateinit var repository: FeeTrackRepository

  @Before
  fun setUp() {
    repository = FeeTrackRepository()
  }

  @Test
  fun testInitialStudentsSeeded() {
    val students = repository.students.value
    assertTrue("Students should be seeded", students.isNotEmpty())
    val payments = repository.payments.value
    assertTrue("Payments should be seeded", payments.isNotEmpty())
  }

  @Test
  fun testRecordPaymentReducesBalance() {
    val initialStudents = repository.students.value
    val owingStudent = initialStudents.first { it.balance > 0 }
    val initialBalance = owingStudent.balance

    val paymentAmount = 150.0
    val payment = repository.recordPayment(
      studentId = owingStudent.id,
      amount = paymentAmount,
      paymentMethod = PaymentMethod.BANK_TRANSFER,
      term = AcademicTerm.TERM_2,
      receiptNo = "REC-TEST-001",
      referenceNotes = "Test payment"
    )

    assertNotNull(payment)
    assertEquals("REC-TEST-001", payment?.receiptNumber)

    val updatedStudent = repository.students.value.first { it.id == owingStudent.id }
    assertEquals(initialBalance - paymentAmount, updatedStudent.balance, 0.01)
  }

  @Test
  fun testPaymentCsvExportNotEmpty() {
    val csv = repository.exportPaymentsCsv()
    assertTrue(csv.contains("Receipt Number,Student Name,Admission No"))
    assertTrue(csv.lines().size > 5)
  }
}
