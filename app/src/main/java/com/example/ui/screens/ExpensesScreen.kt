package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.DukaanViewModel
import com.example.util.PaymentUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpensesScreen(
  viewModel: DukaanViewModel,
  modifier: Modifier = Modifier
) {
  val expenses by viewModel.allExpenses.collectAsState()
  var selectedCategory by remember { mutableStateOf("All") }
  var showAddDialog by remember { mutableStateOf(false) }

  val categories = listOf("All", "Rent", "Electricity", "Staff Salary", "Packaging", "Restock", "Logistics", "Miscellaneous")

  val filteredExpenses = remember(expenses, selectedCategory) {
    if (selectedCategory == "All") expenses
    else expenses.filter { it.category.equals(selectedCategory, ignoreCase = true) }
  }

  val totalAmount = expenses.sumOf { it.amount }

  Scaffold(
    modifier = modifier.testTag("expenses_screen"),
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = EmeraldPrimary,
        contentColor = Color.White,
        modifier = Modifier.testTag("add_expense_fab")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Filled.Add, contentDescription = "Add Expense")
          Spacer(modifier = Modifier.width(8.dp))
          Text("Record Expense", fontWeight = FontWeight.Bold)
        }
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      // Top KPI Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("TOTAL BUSINESS EXPENSES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
              text = PaymentUtils.formatCurrency(totalAmount),
              fontSize = 24.sp,
              fontWeight = FontWeight.ExtraBold,
              color = GoldAccent
            )
          }
          Surface(
            color = GoldContainer,
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = "${expenses.size} Entries",
              color = OnGoldContainer,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      // Category Chips
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(categories) { cat ->
          FilterChip(
            selected = selectedCategory == cat,
            onClick = { selectedCategory = cat },
            label = { Text(cat) }
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Expense list
      if (filteredExpenses.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.ReceiptLong, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text("No expense entries logged", fontWeight = FontWeight.Bold)
            Text("Track rent, bills, staff pay, and supply purchases", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredExpenses, key = { it.id }) { expense ->
            ExpenseItemCard(
              expense = expense,
              onDelete = { viewModel.deleteExpense(expense) }
            )
          }
        }
      }
    }
  }

  // Add Expense Dialog
  if (showAddDialog) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Rent") }
    var amountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("Record Expense", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Expense Title") },
            placeholder = { Text("e.g. Shop Rent, Staff Bonus") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount (₹)") },
            leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          // Category Selector
          Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories.filter { it != "All" }) { cat ->
              FilterChip(
                selected = category == cat,
                onClick = { category = cat },
                label = { Text(cat, fontSize = 11.sp) }
              )
            }
          }

          // Payment Method Selector
          Text("Payment Method", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("CASH", "UPI", "BANK").forEach { method ->
              FilterChip(
                selected = paymentMethod == method,
                onClick = { paymentMethod = method },
                label = { Text(method) }
              )
            }
          }

          OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Notes (Optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amt = amountText.toDoubleOrNull() ?: 0.0
            if (title.isNotBlank() && amt > 0) {
              viewModel.addExpense(title, category, amt, paymentMethod, note)
              showAddDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
          Text("Save Expense", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun ExpenseItemCard(
  expense: ExpenseEntity,
  onDelete: () -> Unit
) {
  val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
  val dateStr = dateFormat.format(Date(expense.timestamp))

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
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
          .size(40.dp)
          .clip(CircleShape)
          .background(GoldContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = OnGoldContainer, modifier = Modifier.size(20.dp))
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(expense.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(
          text = "${expense.category} • Paid via ${expense.paymentMethod} • $dateStr",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (expense.note.isNotBlank()) {
          Text(expense.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
        }
      }

      Text(
        text = PaymentUtils.formatCurrency(expense.amount),
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = GoldAccent
      )

      IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
      }
    }
  }
}
