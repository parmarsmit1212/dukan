package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.model.ProductEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.DukaanViewModel
import com.example.util.PaymentUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
  viewModel: DukaanViewModel,
  modifier: Modifier = Modifier
) {
  val products by viewModel.filteredProducts.collectAsState()
  val allProducts by viewModel.allProducts.collectAsState()
  val searchQuery by viewModel.productSearchQuery.collectAsState()
  val selectedCategory by viewModel.productCategoryFilter.collectAsState()
  val lowStockOnly by viewModel.lowStockOnlyFilter.collectAsState()

  val categories = listOf(
    "All", "Atta & Flours", "Edible Oils", "Spices & Essentials",
    "Rice & Grains", "Pulses & Dal", "Snacks & Instant",
    "Biscuits & Bakery", "Dairy & Milk", "Cleaning & Laundry",
    "Beverages & Tea", "Personal Care", "Chocolates & Sweets"
  )

  var quickRestockProduct by remember { mutableStateOf<ProductEntity?>(null) }

  Scaffold(
    modifier = modifier.testTag("inventory_screen"),
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          // Open new product dialog with empty ProductEntity
          viewModel.showAddProductDialog.value = ProductEntity(
            name = "",
            category = "Daily Essentials",
            costPrice = 0.0,
            sellingPrice = 0.0,
            stockQuantity = 10.0,
            unit = "packet"
          )
        },
        containerColor = EmeraldPrimary,
        contentColor = Color.White,
        modifier = Modifier.testTag("add_product_fab")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Filled.Add, contentDescription = "Add Item")
          Spacer(modifier = Modifier.width(8.dp))
          Text("Add Product", fontWeight = FontWeight.Bold)
        }
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.productSearchQuery.value = it },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("product_search_input"),
        placeholder = { Text("Search by product name, category, SKU...") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { viewModel.productSearchQuery.value = "" }) {
              Icon(Icons.Filled.Clear, contentDescription = "Clear")
            }
          }
        },
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      // Category Horizontal Filter List
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          FilterChip(
            selected = lowStockOnly,
            onClick = { viewModel.lowStockOnlyFilter.value = !lowStockOnly },
            leadingIcon = {
              Icon(Icons.Filled.Warning, contentDescription = null, tint = CrimsonDebt, modifier = Modifier.size(16.dp))
            },
            label = { Text("Low Stock Alert (${allProducts.count { it.stockQuantity <= it.minStockAlert }})") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CrimsonContainer,
              selectedLabelColor = OnCrimsonContainer
            )
          )
        }

        items(categories) { category ->
          FilterChip(
            selected = selectedCategory == category && !lowStockOnly,
            onClick = {
              viewModel.lowStockOnlyFilter.value = false
              viewModel.productCategoryFilter.value = category
            },
            label = { Text(category) }
          )
        }
      }

      // Products Count & Summary
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${products.size} Products Listed",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "Total Valuation: " + PaymentUtils.formatCurrency(products.sumOf { it.stockQuantity * it.sellingPrice }),
          style = MaterialTheme.typography.labelSmall,
          color = EmeraldPrimary,
          fontWeight = FontWeight.Bold
        )
      }

      // Product List
      if (products.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.Inventory2, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text("No products match your criteria", fontWeight = FontWeight.Bold)
            Text("Tap 'Add Product' to stock new items in your inventory", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(products, key = { it.id }) { product ->
            ProductItemCard(
              product = product,
              onEdit = { viewModel.showAddProductDialog.value = product },
              onQuickRestock = { quickRestockProduct = product }
            )
          }
        }
      }
    }
  }

  // Quick Restock Dialog
  if (quickRestockProduct != null) {
    var restockAmountText by remember { mutableStateOf("10") }
    AlertDialog(
      onDismissRequest = { quickRestockProduct = null },
      title = { Text("Restock: ${quickRestockProduct!!.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Current Stock: ${quickRestockProduct!!.stockQuantity} ${quickRestockProduct!!.unit}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          OutlinedTextField(
            value = restockAmountText,
            onValueChange = { restockAmountText = it },
            label = { Text("Units to Add (${quickRestockProduct!!.unit})") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val added = restockAmountText.toDoubleOrNull() ?: 0.0
            if (added > 0) {
              viewModel.restockProduct(quickRestockProduct!!.id, added)
              quickRestockProduct = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
          Text("Update Stock", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { quickRestockProduct = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun ProductItemCard(
  product: ProductEntity,
  onEdit: () -> Unit,
  onQuickRestock: () -> Unit
) {
  val isLowStock = product.stockQuantity <= product.minStockAlert
  val margin = if (product.sellingPrice > 0) {
    ((product.sellingPrice - product.costPrice) / product.sellingPrice * 100).coerceAtLeast(0.0)
  } else 0.0

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("product_card_${product.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = product.name,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${product.category} • SKU: ${product.sku.ifBlank { "N/A" }}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Low stock or available badge
        Surface(
          color = if (isLowStock) CrimsonContainer else EmeraldContainer,
          shape = RoundedCornerShape(8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (isLowStock) {
              Icon(Icons.Filled.Warning, contentDescription = null, tint = CrimsonDebt, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
              text = "${product.stockQuantity.toInt()} ${product.unit}",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 12.sp,
              color = if (isLowStock) OnCrimsonContainer else OnEmeraldContainer
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          Column {
            Text("Selling Price", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(PaymentUtils.formatCurrency(product.sellingPrice), fontWeight = FontWeight.Bold, fontSize = 14.sp)
          }
          Column {
            Text("Cost Price", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(PaymentUtils.formatCurrency(product.costPrice), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Column {
            Text("Margin", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(String.format(java.util.Locale.US, "%.1f%%", margin), fontSize = 13.sp, color = MintReceived, fontWeight = FontWeight.Bold)
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          FilledTonalButton(
            onClick = onQuickRestock,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Restock", fontSize = 11.sp)
          }

          IconButton(
            onClick = onEdit,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(Icons.Outlined.Edit, contentDescription = "Edit Product", modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  }
}
