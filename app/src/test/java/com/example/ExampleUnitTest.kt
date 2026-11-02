package com.example

import com.example.util.PaymentUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testCurrencyFormatting() {
    val formatted = PaymentUtils.formatCurrency(1450.0)
    assertTrue(formatted.contains("1,450") || formatted.contains("1450"))
  }

  @Test
  fun testUpiUriGeneration() {
    val uri = PaymentUtils.generateUpiUri(
      upiId = "rameshkirana@okaxis",
      merchantName = "Shree Ganesh Kirana",
      amount = 620.0,
      transactionNote = "Bill POS Payment"
    )
    assertTrue(uri.startsWith("upi://pay"))
    assertTrue(uri.contains("pa=rameshkirana@okaxis"))
    assertTrue(uri.contains("am=620.00"))
    assertTrue(uri.contains("cu=INR"))
  }

  @Test
  fun testMarginCalculation() {
    val costPrice = 210.0
    val sellingPrice = 245.0
    val margin = ((sellingPrice - costPrice) / sellingPrice) * 100
    assertEquals(14.28, margin, 0.1)
  }
}
