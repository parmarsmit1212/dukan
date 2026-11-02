package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BusinessProfileEntity
import com.example.data.model.InvoiceCartItem
import com.example.data.model.InvoiceEntity
import com.example.ui.theme.*
import com.example.util.PaymentUtils
import com.example.util.PdfInvoiceGenerator
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoicePreviewDialog(
  invoice: InvoiceEntity,
  profile: BusinessProfileEntity,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
  val dateStr = dateFormat.format(Date(invoice.timestamp))

  val parsedItems: List<InvoiceCartItem> = remember(invoice.itemsJson) {
    val list = mutableListOf<InvoiceCartItem>()
    try {
      val jsonArray = JSONArray(invoice.itemsJson)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(
          InvoiceCartItem(
            productId = obj.optLong("productId", 0L),
            productName = obj.optString("productName", "Item"),
            unitPrice = obj.optDouble("unitPrice", 0.0),
            quantity = obj.optDouble("quantity", 1.0),
            unit = obj.optString("unit", "pcs"),
            total = obj.optDouble("total", 0.0)
          )
        )
      }
    } catch (e: Exception) {
      list.add(
        InvoiceCartItem(
          productId = 0L,
          productName = "Store Items Summary",
          unitPrice = invoice.subtotal,
          quantity = 1.0,
          unit = "order",
          total = invoice.subtotal
        )
      )
    }
    list
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .fillMaxHeight(0.90f)
        .testTag("invoice_preview_dialog"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Top Header Bar
        Surface(
          color = EmeraldPrimary,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("RETAIL TAX INVOICE", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
              Text(invoice.invoiceNumber, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
            }
          }
        }

        // Scrollable Receipt Body
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
        ) {
          // Store Info
          Text(profile.shopName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Text("${profile.address} • Phone: ${profile.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("GSTIN: ${profile.gstin}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(14.dp))

          // Bill Info
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Customer: ${invoice.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              if (invoice.customerPhone.isNotBlank()) {
                Text("Phone: ${invoice.customerPhone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(dateStr, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Surface(
                color = if (invoice.paymentMode == "KHATA_CREDIT") CrimsonContainer else EmeraldContainer,
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = "${invoice.paymentMode} - ${invoice.status}",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (invoice.paymentMode == "KHATA_CREDIT") OnCrimsonContainer else OnEmeraldContainer,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Table Items
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Item", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(2f))
              Text("Qty", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
              Text("Rate", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
              Text("Total", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          for (item in parsedItems) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(item.productName, fontSize = 12.sp, modifier = Modifier.weight(2f))
              Text("${item.quantity.toInt()} ${item.unit}", fontSize = 12.sp, modifier = Modifier.weight(1f))
              Text(PaymentUtils.formatCurrency(item.unitPrice), fontSize = 12.sp, modifier = Modifier.weight(1f))
              Text(PaymentUtils.formatCurrency(item.total), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Totals Breakdown
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(14.dp)
          ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Subtotal:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(PaymentUtils.formatCurrency(invoice.subtotal), fontSize = 12.sp)
            }
            if (invoice.discountAmount > 0) {
              Spacer(modifier = Modifier.height(4.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Discount:", fontSize = 12.sp, color = MintReceived)
                Text("-${PaymentUtils.formatCurrency(invoice.discountAmount)}", fontSize = 12.sp, color = MintReceived, fontWeight = FontWeight.Bold)
              }
            }
            if (invoice.taxAmount > 0) {
              Spacer(modifier = Modifier.height(4.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("GST Tax:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("+${PaymentUtils.formatCurrency(invoice.taxAmount)}", fontSize = 12.sp)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("GRAND TOTAL:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Text(PaymentUtils.formatCurrency(invoice.grandTotal), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = EmeraldPrimary)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          Text(profile.invoiceFooter, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Bottom Action Buttons
        Surface(
          tonalElevation = 6.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // PDF Download & Open
            Button(
              onClick = {
                val pdfFile = PdfInvoiceGenerator.generateInvoicePdf(context, invoice, profile)
                if (pdfFile != null) {
                  PdfInvoiceGenerator.openOrShareInvoice(context, pdfFile, isShare = false)
                } else {
                  Toast.makeText(context, "Could not generate PDF", Toast.LENGTH_SHORT).show()
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("btn_pdf_invoice")
            ) {
              Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("PDF", fontWeight = FontWeight.Bold)
            }

            // WhatsApp Share
            FilledTonalButton(
              onClick = {
                val summary = """
                  🧾 *Invoice from ${profile.shopName}*
                  Invoice: #${invoice.invoiceNumber}
                  Customer: ${invoice.customerName}
                  *Total Amount: ${PaymentUtils.formatCurrency(invoice.grandTotal)}*
                  Payment: ${invoice.paymentMode} (${invoice.status})
                  
                  Thank you for shopping with us!
                  UPI: ${profile.upiId}
                """.trimIndent()

                PaymentUtils.shareInvoiceOnWhatsApp(context, invoice.customerPhone, summary)
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("btn_whatsapp_invoice")
            ) {
              Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("WhatsApp", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
