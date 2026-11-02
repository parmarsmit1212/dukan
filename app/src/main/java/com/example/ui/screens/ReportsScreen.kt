package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MetricCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.DukaanViewModel
import com.example.util.PaymentUtils

@Composable
fun ReportsScreen(
  viewModel: DukaanViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val profile by viewModel.businessProfile.collectAsState()
  val invoices by viewModel.allInvoices.collectAsState()
  val expenses by viewModel.allExpenses.collectAsState()
  val customers by viewModel.allCustomers.collectAsState()
  val products by viewModel.allProducts.collectAsState()

  val totalRevenue = invoices.sumOf { it.grandTotal }
  val totalExpenses = expenses.sumOf { it.amount }
  val totalDues = customers.sumOf { if (it.totalDue > 0) it.totalDue else 0.0 }
  val netProfit = (totalRevenue - totalExpenses).coerceAtLeast(0.0)

  val cashSales = invoices.filter { it.paymentMode == "CASH" }.sumOf { it.grandTotal }
  val upiSales = invoices.filter { it.paymentMode == "UPI" }.sumOf { it.grandTotal }
  val khataSales = invoices.filter { it.paymentMode == "KHATA_CREDIT" }.sumOf { it.grandTotal }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("reports_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Profit & Loss Statement Header
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSecondary)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "PROFIT & LOSS OVERVIEW",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = PaymentUtils.formatCurrency(netProfit),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = EmeraldLight
          )
          Text(
            text = "Estimated Net Operating Profit",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
          )

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Total Revenue", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
              Text(PaymentUtils.formatCurrency(totalRevenue), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            }
            Column {
              Text("Total Expenses", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
              Text(PaymentUtils.formatCurrency(totalExpenses), fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 14.sp)
            }
            Column {
              Text("Customer Dues", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
              Text(PaymentUtils.formatCurrency(totalDues), fontWeight = FontWeight.Bold, color = CrimsonDebt, fontSize = 14.sp)
            }
          }
        }
      }
    }

    // 2. Payment Breakdown Donut/Bar Chart
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Revenue by Payment Method", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Text("Distribution of collected revenue", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

          Spacer(modifier = Modifier.height(14.dp))

          // Segmented Bar representation
          val grandTotal = if (totalRevenue > 0) totalRevenue else 1.0
          val cashWeight = (cashSales / grandTotal).toFloat()
          val upiWeight = (upiSales / grandTotal).toFloat()
          val khataWeight = (khataSales / grandTotal).toFloat()

          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(20.dp)
              .clip(RoundedCornerShape(10.dp))
          ) {
            val w = size.width
            val h = size.height

            var curX = 0f
            val cashW = w * cashWeight
            val upiW = w * upiWeight
            val khataW = w * khataWeight

            drawRect(Color(0xFF10B981), Offset(curX, 0f), Size(cashW, h))
            curX += cashW
            drawRect(Color(0xFF0284C7), Offset(curX, 0f), Size(upiW, h))
            curX += upiW
            drawRect(Color(0xFFEF4444), Offset(curX, 0f), Size(khataW, h))
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            PaymentModeLegendItem("Cash", cashSales, Color(0xFF10B981))
            PaymentModeLegendItem("UPI QR", upiSales, Color(0xFF0284C7))
            PaymentModeLegendItem("Khata Due", khataSales, Color(0xFFEF4444))
          }
        }
      }
    }

    // 3. Top Products Overview
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Top Inventory Items", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Text("Best stocked FMCG products", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

          Spacer(modifier = Modifier.height(12.dp))

          products.take(4).forEachIndexed { index, product ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "#${index + 1}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(28.dp)
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("${product.category} • ${product.stockQuantity.toInt()} ${product.unit} remaining", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Text(
                text = PaymentUtils.formatCurrency(product.sellingPrice),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = EmeraldPrimary
              )
            }
          }
        }
      }
    }

    // 4. Share Summary Report Action
    item {
      Button(
        onClick = {
          val report = """
            📊 DukaanPro Business Report for ${profile?.shopName ?: "My Store"}
            Date: ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())}
            
            💰 Total Revenue: ${PaymentUtils.formatCurrency(totalRevenue)}
            💵 Cash Sales: ${PaymentUtils.formatCurrency(cashSales)}
            📱 UPI Sales: ${PaymentUtils.formatCurrency(upiSales)}
            ⏳ Customer Dues: ${PaymentUtils.formatCurrency(totalDues)}
            📉 Total Expenses: ${PaymentUtils.formatCurrency(totalExpenses)}
            ✨ Net Profit: ${PaymentUtils.formatCurrency(netProfit)}
            
            Generated with DukaanPro Smart Business Manager
          """.trimIndent()

          val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, report)
          }
          context.startActivity(Intent.createChooser(shareIntent, "Share P&L Report"))
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
      ) {
        Icon(Icons.Filled.Share, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Share Financial Statement Summary", fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun PaymentModeLegendItem(title: String, amount: Double, color: Color) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
    Spacer(modifier = Modifier.width(6.dp))
    Column {
      Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(PaymentUtils.formatCurrency(amount), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
  }
}
