package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvoiceEntity
import com.example.ui.components.MetricCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.DukaanViewModel
import com.example.util.PaymentUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
  viewModel: DukaanViewModel,
  modifier: Modifier = Modifier
) {
  val profile by viewModel.businessProfile.collectAsState()
  val customers by viewModel.allCustomers.collectAsState()
  val products by viewModel.allProducts.collectAsState()
  val invoices by viewModel.allInvoices.collectAsState()
  val expenses by viewModel.allExpenses.collectAsState()

  // Compute stats
  val totalSales = invoices.sumOf { it.grandTotal }
  val totalDues = customers.sumOf { if (it.totalDue > 0) it.totalDue else 0.0 }
  val totalExpenses = expenses.sumOf { it.amount }
  val netProfit = (totalSales - totalExpenses).coerceAtLeast(0.0)

  val lowStockProducts = products.filter { it.stockQuantity <= it.minStockAlert }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("dashboard_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Business Header Card with Quick Stats
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "TODAY'S TURNOVER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f),
                letterSpacing = 1.sp
              )
              Text(
                text = PaymentUtils.formatCurrency(totalSales),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
              )
            }
            Surface(
              color = Color.White.copy(alpha = 0.18f),
              shape = RoundedCornerShape(12.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+18.4%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Total Customer Dues", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
              Text(PaymentUtils.formatCurrency(totalDues), fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 15.sp)
            }
            Column {
              Text("Net Profit (Est.)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
              Text(PaymentUtils.formatCurrency(netProfit), fontWeight = FontWeight.Bold, color = EmeraldLight, fontSize = 15.sp)
            }
            Column {
              Text("Total Invoices", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
              Text("${invoices.size}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
            }
          }
        }
      }
    }

    // 2. Low Stock Alert Banner (if any)
    if (lowStockProducts.isNotEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("low_stock_banner")
            .clickable {
              viewModel.lowStockOnlyFilter.value = true
              viewModel.navigateTo(AppScreen.INVENTORY)
            },
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = CrimsonContainer)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CrimsonDebt.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Filled.Warning, contentDescription = null, tint = CrimsonDebt)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${lowStockProducts.size} Items Running Low on Stock!",
                fontWeight = FontWeight.Bold,
                color = OnCrimsonContainer,
                fontSize = 14.sp
              )
              Text(
                text = lowStockProducts.take(2).joinToString(", ") { it.name } + if (lowStockProducts.size > 2) "..." else "",
                fontSize = 12.sp,
                color = OnCrimsonContainer.copy(alpha = 0.8f)
              )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "View", tint = OnCrimsonContainer)
          }
        }
      }
    }

    // 3. Quick Action Buttons
    item {
      Text(
        text = "Quick Actions",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = { viewModel.navigateTo(AppScreen.BILLING) },
          modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .testTag("action_new_bill"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
          Icon(Icons.Filled.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("New Bill", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = {
            viewModel.showKhataEntryDialog.value = true
            viewModel.navigateTo(AppScreen.KHATA)
          },
          modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .testTag("action_add_khata"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Filled.PostAdd, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Udhar", fontWeight = FontWeight.Bold)
        }
      }
    }

    // 4. Metric Cards 2x2 Grid
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        MetricCard(
          title = "Customer Udhar",
          value = PaymentUtils.formatCurrency(totalDues),
          subtitle = "${customers.count { it.totalDue > 0 }} customers pending",
          icon = Icons.Filled.AccountBalanceWallet,
          iconTint = CrimsonDebt,
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.weight(1f),
          onClick = {
            viewModel.customerFilterMode.value = "DUE_ONLY"
            viewModel.navigateTo(AppScreen.KHATA)
          }
        )

        MetricCard(
          title = "Active Products",
          value = "${products.size}",
          subtitle = "${products.sumOf { it.stockQuantity.toInt() }} items in stock",
          icon = Icons.Filled.Inventory,
          iconTint = EmeraldPrimary,
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.weight(1f),
          onClick = {
            viewModel.lowStockOnlyFilter.value = false
            viewModel.navigateTo(AppScreen.INVENTORY)
          }
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        MetricCard(
          title = "Total Expenses",
          value = PaymentUtils.formatCurrency(totalExpenses),
          subtitle = "Rent, Bills & Transport",
          icon = Icons.Filled.ReceiptLong,
          iconTint = GoldAccent,
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.weight(1f),
          onClick = { viewModel.navigateTo(AppScreen.EXPENSES) }
        )

        MetricCard(
          title = "Net Profit (Est)",
          value = PaymentUtils.formatCurrency(netProfit),
          subtitle = "Calculated margin",
          icon = Icons.Filled.Savings,
          iconTint = MintReceived,
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.weight(1f),
          onClick = { viewModel.navigateTo(AppScreen.REPORTS) }
        )
      }
    }

    // 5. Weekly Sales Bar Chart Canvas
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Weekly Revenue Trend", fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Text("Last 7 Days Sales Activity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = { viewModel.navigateTo(AppScreen.REPORTS) }) {
              Text("Full Report", fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Custom 7-day Bar Chart Canvas
          val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
          val salesData = listOf(1420f, 2150f, 1890f, 2900f, 3420f, 4850f, 3920f)
          val maxVal = salesData.maxOrNull() ?: 5000f

          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp)
          ) {
            val barWidth = size.width / 9f
            val spacing = (size.width - (barWidth * 7)) / 8f
            val chartHeight = size.height - 24.dp.toPx()

            salesData.forEachIndexed { index, value ->
              val barHeight = (value / maxVal) * chartHeight
              val x = spacing + index * (barWidth + spacing)
              val y = chartHeight - barHeight

              val isPeak = value == maxVal
              val barColor = if (isPeak) Color(0xFF0D5C3A) else Color(0xFF10B981).copy(alpha = 0.5f)

              drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            days.forEach { day ->
              Text(
                text = day,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }
    }

    // 6. Recent Invoices
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Recent Invoices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        TextButton(onClick = { viewModel.navigateTo(AppScreen.BILLING) }) {
          Text("POS Counter")
        }
      }
    }

    if (invoices.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("No bills created yet. Tap 'New Bill' to generate your first invoice!", color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    } else {
      items(invoices.take(4)) { invoice ->
        RecentInvoiceItemCard(
          invoice = invoice,
          onView = { viewModel.activeInvoicePreview.value = invoice }
        )
      }
    }
  }
}

@Composable
fun RecentInvoiceItemCard(
  invoice: InvoiceEntity,
  onView: () -> Unit
) {
  val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
  val dateStr = dateFormat.format(Date(invoice.timestamp))

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onView() },
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(EmeraldContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Filled.Receipt, contentDescription = null, tint = OnEmeraldContainer, modifier = Modifier.size(22.dp))
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = invoice.customerName,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
        Text(
          text = "${invoice.invoiceNumber} • $dateStr",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = PaymentUtils.formatCurrency(invoice.grandTotal),
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Surface(
          color = if (invoice.paymentMode == "KHATA_CREDIT") CrimsonContainer else SlateContainer,
          shape = RoundedCornerShape(4.dp)
        ) {
          Text(
            text = invoice.paymentMode,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (invoice.paymentMode == "KHATA_CREDIT") OnCrimsonContainer else OnSlateContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }
    }
  }
}
