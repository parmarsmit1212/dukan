package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerEntity
import com.example.data.model.KhataEntryEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.DukaanViewModel
import com.example.util.PaymentUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhataScreen(
  viewModel: DukaanViewModel,
  modifier: Modifier = Modifier
) {
  val selectedCustomer by viewModel.selectedCustomerForKhata.collectAsState()
  val context = LocalContext.current

  if (selectedCustomer != null) {
    CustomerLedgerDetailView(
      customer = selectedCustomer!!,
      viewModel = viewModel,
      onBack = { viewModel.selectedCustomerForKhata.value = null }
    )
  } else {
    CustomerKhataListView(
      viewModel = viewModel,
      modifier = modifier
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerKhataListView(
  viewModel: DukaanViewModel,
  modifier: Modifier = Modifier
) {
  val customers by viewModel.filteredCustomers.collectAsState()
  val allCustomers by viewModel.allCustomers.collectAsState()
  val searchQuery by viewModel.customerSearchQuery.collectAsState()
  val filterMode by viewModel.customerFilterMode.collectAsState()

  val totalUdhar = allCustomers.sumOf { if (it.totalDue > 0) it.totalDue else 0.0 }
  val pendingCount = allCustomers.count { it.totalDue > 0.01 }

  Scaffold(
    modifier = modifier.testTag("khata_screen"),
    floatingActionButton = {
      FloatingActionButton(
        onClick = { viewModel.showAddCustomerDialog.value = true },
        containerColor = EmeraldPrimary,
        contentColor = Color.White,
        modifier = Modifier.testTag("add_customer_fab")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Filled.PersonAdd, contentDescription = "Add Customer")
          Spacer(modifier = Modifier.width(8.dp))
          Text("Add Customer", fontWeight = FontWeight.Bold)
        }
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      // Top KPI Ribbon
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CrimsonContainer)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("TOTAL YOU WILL GET (UDHAR)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnCrimsonContainer)
            Text(
              text = PaymentUtils.formatCurrency(totalUdhar),
              fontSize = 20.sp,
              fontWeight = FontWeight.ExtraBold,
              color = CrimsonDebt
            )
          }
          Surface(
            color = CrimsonDebt.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = "$pendingCount Customers Pending",
              color = OnCrimsonContainer,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.customerSearchQuery.value = it },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp)
          .testTag("customer_search_input"),
        placeholder = { Text("Search by customer name, phone, address...") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { viewModel.customerSearchQuery.value = "" }) {
              Icon(Icons.Filled.Clear, contentDescription = "Clear")
            }
          }
        },
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      // Filter Chips (All, You'll Get, Settled)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = filterMode == "ALL",
          onClick = { viewModel.customerFilterMode.value = "ALL" },
          label = { Text("All (${allCustomers.size})") }
        )
        FilterChip(
          selected = filterMode == "DUE_ONLY",
          onClick = { viewModel.customerFilterMode.value = "DUE_ONLY" },
          label = { Text("You'll Get (${pendingCount})") }
        )
        FilterChip(
          selected = filterMode == "SETTLED",
          onClick = { viewModel.customerFilterMode.value = "SETTLED" },
          label = { Text("Settled (${allCustomers.size - pendingCount})") }
        )
      }

      // Customer List
      if (customers.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.PersonSearch, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text("No customers found", fontWeight = FontWeight.Bold)
            Text("Tap 'Add Customer' to create a digital ledger account", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(customers, key = { it.id }) { customer ->
            CustomerCard(
              customer = customer,
              onClick = { viewModel.selectedCustomerForKhata.value = customer }
            )
          }
        }
      }
    }
  }
}

@Composable
fun CustomerCard(
  customer: CustomerEntity,
  onClick: () -> Unit
) {
  val hasDue = customer.totalDue > 0.01
  val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
  val dateStr = dateFormat.format(Date(customer.lastTransactionDate))

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("customer_card_${customer.id}")
      .clickable { onClick() },
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Initials Avatar
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(if (hasDue) CrimsonContainer else EmeraldContainer),
        contentAlignment = Alignment.Center
      ) {
        val initials = customer.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
        Text(
          text = initials.ifBlank { "C" },
          fontWeight = FontWeight.Bold,
          color = if (hasDue) OnCrimsonContainer else OnEmeraldContainer,
          fontSize = 15.sp
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = customer.name,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
        Text(
          text = customer.phone,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (customer.address.isNotBlank()) {
          Text(
            text = customer.address,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            maxLines = 1
          )
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        if (hasDue) {
          Text(
            text = "YOU WILL GET",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = CrimsonDebt
          )
          Text(
            text = PaymentUtils.formatCurrency(customer.totalDue),
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CrimsonDebt
          )
        } else {
          Text(
            text = "SETTLED",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MintReceived
          )
          Text(
            text = "₹0.00",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MintReceived
          )
        }
        Text(
          text = dateStr,
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLedgerDetailView(
  customer: CustomerEntity,
  viewModel: DukaanViewModel,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val profile by viewModel.businessProfile.collectAsState()
  val entries by viewModel.khataEntriesForSelectedCustomer.collectAsState()

  var showAddEntryDialog by remember { mutableStateOf<String?>(null) } // "CREDIT_GAVE" or "PAYMENT_RECEIVED"

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(customer.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${customer.phone}")
              }
              context.startActivity(intent)
            }
          ) {
            Icon(Icons.Filled.Phone, contentDescription = "Call Customer", tint = EmeraldPrimary)
          }
          IconButton(
            onClick = {
              PaymentUtils.shareWhatsAppReminder(
                context = context,
                phone = customer.phone,
                customerName = customer.name,
                dueAmount = customer.totalDue,
                shopName = profile?.shopName ?: "Our Store",
                upiId = profile?.upiId ?: ""
              )
            }
          ) {
            Icon(Icons.Filled.Share, contentDescription = "Send WhatsApp Reminder", tint = EmeraldPrimary)
          }
        }
      )
    },
    bottomBar = {
      Surface(
        tonalElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Button(
            onClick = { showAddEntryDialog = "CREDIT_GAVE" },
            colors = ButtonDefaults.buttonColors(containerColor = CrimsonDebt),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("button_udhar_diya")
          ) {
            Icon(Icons.Filled.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("YOU GAVE (UDHAR)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }

          Button(
            onClick = { showAddEntryDialog = "PAYMENT_RECEIVED" },
            colors = ButtonDefaults.buttonColors(containerColor = MintReceived),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("button_paisa_mila")
          ) {
            Icon(Icons.Filled.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("YOU GOT (RECEIVED)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      // Outstanding Balance Header Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (customer.totalDue > 0) CrimsonContainer else EmeraldContainer
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = if (customer.totalDue > 0) "TOTAL BALANCE DUE" else "ACCOUNT STATUS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (customer.totalDue > 0) OnCrimsonContainer else OnEmeraldContainer,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = PaymentUtils.formatCurrency(customer.totalDue),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (customer.totalDue > 0) CrimsonDebt else MintReceived
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (customer.totalDue > 0) "Customer owes you this amount" else "No pending dues",
            fontSize = 12.sp,
            color = if (customer.totalDue > 0) OnCrimsonContainer.copy(alpha = 0.8f) else OnEmeraldContainer.copy(alpha = 0.8f)
          )
        }
      }

      // Chronological Statement Entries
      Text(
        text = "TRANSACTION LEDGER",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
      )

      if (entries.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Text("No transactions logged yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(entries, key = { it.id }) { entry ->
            KhataEntryRow(entry = entry)
          }
        }
      }
    }
  }

  // Dialog for Adding Khata Transaction
  if (showAddEntryDialog != null) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    val isCredit = showAddEntryDialog == "CREDIT_GAVE"

    AlertDialog(
      onDismissRequest = { showAddEntryDialog = null },
      title = {
        Text(
          text = if (isCredit) "You Gave (Udhar Diya)" else "You Got (Paisa Mila)",
          fontWeight = FontWeight.Bold,
          color = if (isCredit) CrimsonDebt else MintReceived
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Customer: ${customer.name}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount (₹)") },
            leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("khata_amount_input")
          )
          OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Note / Description (Optional)") },
            placeholder = { Text("e.g. 5kg Atta, Oil, Biscuits") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amt = amountText.toDoubleOrNull() ?: 0.0
            if (amt > 0) {
              viewModel.addKhataTransaction(
                customerId = customer.id,
                type = showAddEntryDialog!!,
                amount = amt,
                note = noteText.ifBlank { if (isCredit) "Store Credit" else "Cash/UPI Received" }
              )
              showAddEntryDialog = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = if (isCredit) CrimsonDebt else MintReceived)
        ) {
          Text("Save Entry", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddEntryDialog = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun KhataEntryRow(entry: KhataEntryEntity) {
  val isCreditGave = entry.type == "CREDIT_GAVE"
  val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
  val dateStr = dateFormat.format(Date(entry.timestamp))

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(if (isCreditGave) CrimsonContainer else MintContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          if (isCreditGave) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
          contentDescription = null,
          tint = if (isCreditGave) CrimsonDebt else MintReceived,
          modifier = Modifier.size(18.dp)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (isCreditGave) "You Gave (Credit)" else "Payment Received",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
        if (entry.note.isNotBlank()) {
          Text(text = entry.note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
      }

      Text(
        text = PaymentUtils.formatCurrency(entry.amount),
        fontWeight = FontWeight.ExtraBold,
        fontSize = 15.sp,
        color = if (isCreditGave) CrimsonDebt else MintReceived
      )
    }
  }
}
