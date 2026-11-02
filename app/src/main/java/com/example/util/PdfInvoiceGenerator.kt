package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.BusinessProfileEntity
import com.example.data.model.InvoiceEntity
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfInvoiceGenerator {

  fun generateInvoicePdf(
    context: Context,
    invoice: InvoiceEntity,
    profile: BusinessProfileEntity
  ): File? {
    try {
      val pdfDoc = PdfDocument()
      val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points
      val page = pdfDoc.startPage(pageInfo)
      val canvas: Canvas = page.canvas

      val paint = Paint(Paint.ANTI_ALIAS_FLAG)
      val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }

      // 1. Header background banner
      paint.color = Color.rgb(13, 92, 58) // Emerald Green #0D5C3A
      canvas.drawRect(0f, 0f, 595f, 90f, paint)

      // Header shop title
      boldPaint.color = Color.WHITE
      boldPaint.textSize = 20f
      canvas.drawText(profile.shopName, 30f, 38f, boldPaint)

      // Header address & phone
      paint.color = Color.rgb(220, 240, 230)
      paint.textSize = 10f
      canvas.drawText("${profile.address} | Phone: ${profile.phone}", 30f, 56f, paint)
      canvas.drawText("GSTIN: ${profile.gstin} | UPI: ${profile.upiId}", 30f, 72f, paint)

      // 2. Tax Invoice Title
      boldPaint.color = Color.rgb(15, 23, 42)
      boldPaint.textSize = 14f
      canvas.drawText("RETAIL TAX INVOICE", 30f, 120f, boldPaint)

      // Decorative divider
      paint.color = Color.rgb(203, 213, 225)
      paint.strokeWidth = 1.5f
      canvas.drawLine(30f, 128f, 565f, 128f, paint)

      // 3. Invoice & Customer details block
      paint.textSize = 10f
      paint.color = Color.rgb(71, 85, 105)

      val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
      val dateStr = dateFormat.format(Date(invoice.timestamp))

      canvas.drawText("Invoice No: ${invoice.invoiceNumber}", 30f, 148f, paint)
      canvas.drawText("Date: $dateStr", 30f, 164f, paint)
      canvas.drawText("Payment Mode: ${invoice.paymentMode}", 30f, 180f, paint)

      canvas.drawText("Billed To: ${invoice.customerName}", 320f, 148f, paint)
      if (invoice.customerPhone.isNotBlank()) {
        canvas.drawText("Mobile: ${invoice.customerPhone}", 320f, 164f, paint)
      }
      canvas.drawText("Status: ${invoice.status}", 320f, 180f, paint)

      // 4. Table Header
      val tableTop = 205f
      paint.color = Color.rgb(241, 245, 249)
      canvas.drawRect(30f, tableTop, 565f, tableTop + 24f, paint)

      boldPaint.color = Color.rgb(30, 41, 59)
      boldPaint.textSize = 10f
      canvas.drawText("#", 38f, tableTop + 16f, boldPaint)
      canvas.drawText("Item Description", 70f, tableTop + 16f, boldPaint)
      canvas.drawText("Qty", 340f, tableTop + 16f, boldPaint)
      canvas.drawText("Rate (₹)", 410f, tableTop + 16f, boldPaint)
      canvas.drawText("Total (₹)", 495f, tableTop + 16f, boldPaint)

      // 5. Line Items
      var currentY = tableTop + 42f
      paint.color = Color.rgb(15, 23, 42)
      paint.textSize = 9.5f

      try {
        val jsonArray = JSONArray(invoice.itemsJson)
        for (i in 0 until jsonArray.length()) {
          val obj = jsonArray.getJSONObject(i)
          val name = obj.optString("productName", "Item")
          val qty = obj.optDouble("quantity", 1.0)
          val unit = obj.optString("unit", "pcs")
          val rate = obj.optDouble("unitPrice", 0.0)
          val total = obj.optDouble("total", qty * rate)

          canvas.drawText("${i + 1}", 38f, currentY, paint)
          val truncatedName = if (name.length > 40) name.take(37) + "..." else name
          canvas.drawText(truncatedName, 70f, currentY, paint)
          canvas.drawText(String.format(Locale.US, "%.1f %s", qty, unit), 340f, currentY, paint)
          canvas.drawText(String.format(Locale.US, "%.2f", rate), 410f, currentY, paint)
          canvas.drawText(String.format(Locale.US, "%.2f", total), 495f, currentY, paint)

          // Light separator line
          val lineY = currentY + 6f
          val linePaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            strokeWidth = 0.8f
          }
          canvas.drawLine(30f, lineY, 565f, lineY, linePaint)

          currentY += 22f
          if (currentY > 650f) break // Avoid page overflow
        }
      } catch (e: Exception) {
        canvas.drawText("General Store Items", 70f, currentY, paint)
        canvas.drawText(String.format(Locale.US, "%.2f", invoice.subtotal), 495f, currentY, paint)
        currentY += 22f
      }

      // 6. Summary Block
      val summaryY = currentY + 20f
      paint.color = Color.rgb(203, 213, 225)
      paint.strokeWidth = 1f
      canvas.drawLine(320f, summaryY, 565f, summaryY, paint)

      paint.color = Color.rgb(71, 85, 105)
      paint.textSize = 10f
      canvas.drawText("Subtotal:", 340f, summaryY + 18f, paint)
      canvas.drawText(String.format(Locale.US, "₹%.2f", invoice.subtotal), 495f, summaryY + 18f, paint)

      if (invoice.discountAmount > 0) {
        canvas.drawText("Discount:", 340f, summaryY + 34f, paint)
        canvas.drawText(String.format(Locale.US, "-₹%.2f", invoice.discountAmount), 495f, summaryY + 34f, paint)
      }

      if (invoice.taxAmount > 0) {
        canvas.drawText("GST Tax:", 340f, summaryY + 50f, paint)
        canvas.drawText(String.format(Locale.US, "+₹%.2f", invoice.taxAmount), 495f, summaryY + 50f, paint)
      }

      // Grand Total Box
      val totalBoxY = summaryY + 65f
      paint.color = Color.rgb(209, 250, 229) // Emerald light container
      canvas.drawRect(320f, totalBoxY, 565f, totalBoxY + 30f, paint)

      boldPaint.color = Color.rgb(6, 78, 59)
      boldPaint.textSize = 12f
      canvas.drawText("Grand Total:", 335f, totalBoxY + 20f, boldPaint)
      canvas.drawText(String.format(Locale.US, "₹%.2f", invoice.grandTotal), 485f, totalBoxY + 20f, boldPaint)

      // 7. Footer & Signatory
      paint.color = Color.rgb(100, 116, 139)
      paint.textSize = 9f
      canvas.drawText(profile.invoiceFooter, 30f, 760f, paint)
      canvas.drawText("Computer Generated Invoice - DukaanPro Smart Business Manager", 30f, 775f, paint)

      boldPaint.color = Color.rgb(30, 41, 59)
      boldPaint.textSize = 10f
      canvas.drawText("For ${profile.shopName}", 400f, 740f, boldPaint)
      paint.textSize = 8.5f
      canvas.drawText("Authorized Signatory", 420f, 775f, paint)

      pdfDoc.finishPage(page)

      // Write to file
      val dir = File(context.cacheDir, "invoices")
      if (!dir.exists()) dir.mkdirs()
      val file = File(dir, "Invoice_${invoice.invoiceNumber}.pdf")
      val outputStream = FileOutputStream(file)
      pdfDoc.writeTo(outputStream)
      outputStream.close()
      pdfDoc.close()

      return file
    } catch (e: Exception) {
      e.printStackTrace()
      return null
    }
  }

  fun openOrShareInvoice(context: Context, file: File, isShare: Boolean = true) {
    try {
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )

      val intent = Intent(if (isShare) Intent.ACTION_SEND else Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val title = if (isShare) "Share Invoice PDF" else "Open Invoice PDF"
      context.startActivity(Intent.createChooser(intent, title))
    } catch (e: Exception) {
      Toast.makeText(context, "Could not open/share PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
  }
}
