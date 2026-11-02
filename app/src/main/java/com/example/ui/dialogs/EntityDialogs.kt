package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerEntity
import com.example.data.model.ProductEntity
import com.example.ui.theme.*
import com.example.util.PaymentUtils

@Composable
fun AddCustomerDialog(
  onDismiss: () -> Unit,
  onSave: (name: String, phone: String, address: String, initialDue: Double) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var address by remember { mutableStateOf("") }
  var initialDueText by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("dialog_add_customer"),
    title = { Text("Add Customer Account", fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Customer Name *") },
          placeholder = { Text("e.g. Ramesh Bhai") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_customer_name")
        )

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Mobile Number *") },
          placeholder = { Text("+91 98765 43210") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_customer_phone")
        )

        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text("Address / Colony (Optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = initialDueText,
          onValueChange = { initialDueText = it },
          label = { Text("Previous Pending Udhar (₹, Optional)") },
          placeholder = { Text("0.00") },
          leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && phone.isNotBlank()) {
            val due = initialDueText.toDoubleOrNull() ?: 0.0
            onSave(name, phone, address, due)
          }
        },
        enabled = name.isNotBlank() && phone.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        modifier = Modifier.testTag("btn_save_customer")
      ) {
        Text("Save Customer", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
  product: ProductEntity,
  onDismiss: () -> Unit,
  onSave: (ProductEntity) -> Unit
) {
  var name by remember { mutableStateOf(product.name) }
  var category by remember { mutableStateOf(product.category) }
  var costPriceText by remember { mutableStateOf(if (product.costPrice > 0) product.costPrice.toString() else "") }
  var sellingPriceText by remember { mutableStateOf(if (product.sellingPrice > 0) product.sellingPrice.toString() else "") }
  var stockText by remember { mutableStateOf(product.stockQuantity.toInt().toString()) }
  var unit by remember { mutableStateOf(product.unit) }
  var sku by remember { mutableStateOf(product.sku) }
  var minStockText by remember { mutableStateOf(product.minStockAlert.toInt().toString()) }

  val isNew = product.id == 0L

  val categories = listOf(
    "Atta & Flours", "Edible Oils", "Spices & Essentials",
    "Rice & Grains", "Pulses & Dal", "Snacks & Instant",
    "Biscuits & Bakery", "Dairy & Milk", "Cleaning & Laundry",
    "Beverages & Tea", "Personal Care", "Chocolates & Sweets", "General FMCG"
  )
  val units = listOf("packet", "pcs", "kg", "g", "litre", "bottle", "box")

  var expandedCategory by remember { mutableStateOf(false) }
  var expandedUnit by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("dialog_add_product"),
    title = { Text(if (isNew) "Add New Product" else "Edit Product", fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Product Name *") },
          placeholder = { Text("e.g. Basmati Rice 1kg") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_product_name")
        )

        // Category dropdown
        ExposedDropdownMenuBox(
          expanded = expandedCategory,
          onExpandedChange = { expandedCategory = !expandedCategory }
        ) {
          OutlinedTextField(
            value = category,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = expandedCategory,
            onDismissRequest = { expandedCategory = false }
          ) {
            categories.forEach { cat ->
              DropdownMenuItem(
                text = { Text(cat) },
                onClick = {
                  category = cat
                  expandedCategory = false
                }
              )
            }
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = sellingPriceText,
            onValueChange = { sellingPriceText = it },
            label = { Text("Sell Price (₹) *") },
            leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("input_product_sell_price")
          )
          OutlinedTextField(
            value = costPriceText,
            onValueChange = { costPriceText = it },
            label = { Text("Cost Price (₹)") },
            leadingIcon = { Text("₹") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = stockText,
            onValueChange = { stockText = it },
            label = { Text("Stock Qty *") },
            singleLine = true,
            modifier = Modifier.weight(1.2f).testTag("input_product_stock")
          )

          // Unit dropdown
          ExposedDropdownMenuBox(
            expanded = expandedUnit,
            onExpandedChange = { expandedUnit = !expandedUnit },
            modifier = Modifier.weight(1f)
          ) {
            OutlinedTextField(
              value = unit,
              onValueChange = {},
              readOnly = true,
              label = { Text("Unit") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
              modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = expandedUnit,
              onDismissRequest = { expandedUnit = false }
            ) {
              units.forEach { u ->
                DropdownMenuItem(
                  text = { Text(u) },
                  onClick = {
                    unit = u
                    expandedUnit = false
                  }
                )
              }
            }
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = sku,
            onValueChange = { sku = it },
            label = { Text("SKU / Barcode") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = minStockText,
            onValueChange = { minStockText = it },
            label = { Text("Min Alert Level") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val sell = sellingPriceText.toDoubleOrNull() ?: 0.0
          val cost = costPriceText.toDoubleOrNull() ?: (sell * 0.8)
          val stock = stockText.toDoubleOrNull() ?: 0.0
          val minStock = minStockText.toDoubleOrNull() ?: 5.0

          if (name.isNotBlank() && sell > 0) {
            onSave(
              product.copy(
                name = name,
                category = category,
                sellingPrice = sell,
                costPrice = cost,
                stockQuantity = stock,
                unit = unit,
                sku = sku,
                minStockAlert = minStock
              )
            )
          }
        },
        enabled = name.isNotBlank() && sellingPriceText.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        modifier = Modifier.testTag("btn_save_product")
      ) {
        Text(if (isNew) "Add to Stock" else "Save Changes", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun NotificationsDialog(
  lowStockProducts: List<ProductEntity>,
  customersWithDue: List<CustomerEntity>,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Store Alerts & Reminders", fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Low stock section
        Text("Low Inventory Stock Alerts (${lowStockProducts.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CrimsonDebt)
        if (lowStockProducts.isEmpty()) {
          Text("All items are adequately stocked!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          lowStockProducts.forEach { p ->
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(p.name, fontSize = 12.sp, modifier = Modifier.weight(1f))
              Text("Only ${p.stockQuantity.toInt()} left", fontSize = 12.sp, color = CrimsonDebt, fontWeight = FontWeight.Bold)
            }
          }
        }

        HorizontalDivider()

        // Outstanding Customer Dues section
        Text("Top Pending Customer Dues (${customersWithDue.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldAccent)
        customersWithDue.take(4).forEach { c ->
          Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(c.name, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text(PaymentUtils.formatCurrency(c.totalDue), fontSize = 12.sp, color = CrimsonDebt, fontWeight = FontWeight.Bold)
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)) {
        Text("Got It")
      }
    }
  )
}
