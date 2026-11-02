package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.util.Locale

object PaymentUtils {

  fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return format.format(amount).replace("INR", "₹").trim()
  }

  fun generateUpiUri(
    upiId: String,
    merchantName: String,
    amount: Double,
    transactionNote: String
  ): String {
    val cleanUpi = upiId.ifBlank { "merchant@upi" }
    val encodedName = URLEncoder.encode(merchantName, StandardCharsets.UTF_8.toString())
    val encodedNote = URLEncoder.encode(transactionNote, StandardCharsets.UTF_8.toString())
    val formattedAmount = String.format(Locale.US, "%.2f", amount)

    return "upi://pay?pa=$cleanUpi&pn=$encodedName&am=$formattedAmount&cu=INR&tn=$encodedNote"
  }

  fun openUpiPayment(
    context: Context,
    upiId: String,
    merchantName: String,
    amount: Double,
    note: String
  ) {
    val uriString = generateUpiUri(upiId, merchantName, amount, note)
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
    try {
      context.startActivity(Intent.createChooser(intent, "Pay via UPI App (GPay, PhonePe, Paytm)"))
    } catch (e: Exception) {
      Toast.makeText(context, "No UPI payment app found on this device", Toast.LENGTH_SHORT).show()
    }
  }

  fun shareWhatsAppReminder(
    context: Context,
    phone: String,
    customerName: String,
    dueAmount: Double,
    shopName: String,
    upiId: String
  ) {
    val cleanPhone = phone.replace(Regex("[^0-9]"), "")
    val targetPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone

    val message = """
      *Payment Reminder from $shopName*
      
      Dear $customerName,
      Your outstanding credit balance at our shop is *${formatCurrency(dueAmount)}*.
      
      Kindly clear your dues at your earliest convenience.
      You can pay directly via UPI: *$upiId*
      
      Thank you for being our valued customer!
    """.trimIndent()

    try {
      val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://api.whatsapp.com/send?phone=$targetPhone&text=${URLEncoder.encode(message, "UTF-8")}")
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      // Fallback to generic share
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
      }
      context.startActivity(Intent.createChooser(shareIntent, "Send Payment Reminder"))
    }
  }

  fun shareInvoiceOnWhatsApp(
    context: Context,
    customerPhone: String,
    invoiceSummary: String
  ) {
    val cleanPhone = customerPhone.replace(Regex("[^0-9]"), "")
    val targetPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone

    try {
      val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://api.whatsapp.com/send?phone=$targetPhone&text=${URLEncoder.encode(invoiceSummary, "UTF-8")}")
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, invoiceSummary)
      }
      context.startActivity(Intent.createChooser(shareIntent, "Share Invoice via"))
    }
  }
}
