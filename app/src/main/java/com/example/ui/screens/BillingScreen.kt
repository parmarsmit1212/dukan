package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvoiceCartItem
import com.example.data.model.ProductEntity
import com.example.ui.components.DynamicQrCode
import com.example.ui.theme.*
import com.example.ui.viewmodel.DukaanViewModel
import com.example.util.PaymentUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
  viewModel: DukaanViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val profile by viewModel.businessProfile.collectAsState()
  val products by viewModel.allProducts.collectAsState()
  val customers by viewModel.allCustomers.collectAsState()

  val cartItems by viewModel.cartItems.collectAsState()
  val subtotal by viewModel.cartSubtotal.collectAsState()
  val discountAmount by viewModel.cartDiscountAmount.collectAsState()
  val taxAmount by viewModel.cartTaxAmount.collectAsState()
  val grandTotal by viewModel.cartGrandTotal.collectAsState()

  val selectedCustomer by viewModel.selectedBillingCustomer.collectAsState()
  val walkInName by viewModel.walkInCustomerName.collectAsState()
  val discountPct by viewModel.discountPercent.collectAsState()
  val taxPct by viewModel.taxPercent.collectAsState()
  val paymentMode by viewModel.paymentMode.collectAsState()
  val cashReceived by viewModel.cashReceivedAmount.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var activeTab by remember { mutableStateOf(0) } // 0 = Catalog & Items, 1 = Cart & Checkout

  val filteredProducts = remember(products, searchQuery) {
    if (searchQuery.isBlank()) products
    else products.filter {
      it.name.contains(searchQuery, ignoreCase = true) ||
      it.category.contains(searchQuery, ignoreCase = true)
    }
  }

  val cashGiven = cashReceived.toDoubleOrNull() ?: 0.0
  val changeDue = (cashGiven - grandTotal).coerceAtLeast(0.0)

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("billing_screen")
  ) {
    // Mode Switch Tabs: "Items Catalog" vs "Active Cart (count)"
    PrimaryTabRow(
      selectedTabIndex = activeTab,
      containerColor = MaterialTheme.colorScheme.surface
    ) {
      Tab(
        selected = activeTab == 0,
        onClick = { activeTab = 0 },
        text = { Text("1. Select Products", fontWeight = FontWeight.Bold) },
        icon = { Icon(Icons.Filled.Storefront, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.testTag("tab_billing_catalog")
      )
      Tab(
        selected = activeTab == 1,
        onClick = { activeTab = 1 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("2. Checkout", fontWeight = FontWeight.Bold)
            if (cartItems.isNotEmpty()) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                color = EmeraldPrimary,
                shape = CircleShape
              ) {
                Text(
                  text = "${cartItems.size}",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        },
        icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.testTag("tab_billing_checkout")
      )
    }

    if (activeTab == 0) {
      // ---------------- CATALOG TAB ----------------
      Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("pos_product_search"),
          placeholder = { Text("Search items to add to bill...") },
          leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
          trailingIcon = {
            if (searchQuery.isNotBlank()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Filled.Clear, contentDescription = "Clear")
              }
            }
          },
          shape = RoundedCornerShape(12.dp),
          singleLine = true
        )

        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(bottom = 80.dp)
        ) {
          items(filteredProducts, key = { it.id }) { product ->
            val inCartItem = cartItems.find { it.productId == product.id }
            ProductPosCatalogRow(
              product = product,
              inCartQuantity = inCartItem?.quantity ?: 0.0,
              onAdd = { viewModel.addToCart(product, 1.0) },
              onRemove = {
                if (inCartItem != null) {
                  viewModel.updateCartQuantity(product.id, inCartItem.quantity - 1.0)
                }
              }
            )
          }
        }

        // Bottom floating bar showing cart items count and 'Proceed to Bill'
        if (cartItems.isNotEmpty()) {
          Surface(
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
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
                Text(
                  text = "${cartItems.sumOf { it.quantity.toInt() }} Items in Bill",
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = PaymentUtils.formatCurrency(subtotal),
                  fontSize = 18.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = EmeraldPrimary
                )
              }
              Button(
                onClick = { activeTab = 1 },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_proceed_to_checkout")
              ) {
                Text("Proceed to Bill", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }
    } else {
      // ---------------- CHECKOUT & BILLING TAB ----------------
      if (cartItems.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Your POS Cart is Empty", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Switch to 'Select Products' to add items", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { activeTab = 0 }) {
              Text("Browse Products")
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // 1. Line Items List
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("Billed Items (${cartItems.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  TextButton(onClick = { viewModel.clearCart() }) {
                    Text("Clear All", color = CrimsonDebt, fontSize = 12.sp)
                  }
                }

                cartItems.forEach { item ->
                  CartItemRow(
                    item = item,
                    onIncrease = { viewModel.updateCartQuantity(item.productId, item.quantity + 1.0) },
                    onDecrease = { viewModel.updateCartQuantity(item.productId, item.quantity - 1.0) },
                    onDelete = { viewModel.removeFromCart(item.productId) }
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                }
              }
            }
          }

          // 2. Customer Assignment
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text("Customer Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                var expandedCustomerMenu by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                  expanded = expandedCustomerMenu,
                  onExpandedChange = { expandedCustomerMenu = !expandedCustomerMenu }
                ) {
                  OutlinedTextField(
                    value = selectedCustomer?.name ?: "Walk-in Customer",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Customer (Khata)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCustomerMenu) },
                    modifier = Modifier
                      .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                      .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                  )
                  ExposedDropdownMenu(
                    expanded = expandedCustomerMenu,
                    onDismissRequest = { expandedCustomerMenu = false }
                  ) {
                    DropdownMenuItem(
                      text = { Text("Walk-in Customer (No Khata Account)") },
                      onClick = {
                        viewModel.selectedBillingCustomer.value = null
                        expandedCustomerMenu = false
                      }
                    )
                    customers.forEach { customer ->
                      DropdownMenuItem(
                        text = {
                          Column {
                            Text(customer.name, fontWeight = FontWeight.Bold)
                            Text("${customer.phone} • Balance: ${PaymentUtils.formatCurrency(customer.totalDue)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                          }
                        },
                        onClick = {
                          viewModel.selectedBillingCustomer.value = customer
                          expandedCustomerMenu = false
                        }
                      )
                    }
                  }
                }

                if (selectedCustomer == null) {
                  Spacer(modifier = Modifier.height(10.dp))
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                      value = walkInName,
                      onValueChange = { viewModel.walkInCustomerName.value = it },
                      label = { Text("Name (Optional)") },
                      modifier = Modifier.weight(1f),
                      singleLine = true,
                      shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                      value = viewModel.walkInCustomerPhone.collectAsState().value,
                      onValueChange = { viewModel.walkInCustomerPhone.value = it },
                      label = { Text("Phone") },
                      modifier = Modifier.weight(1f),
                      singleLine = true,
                      shape = RoundedCornerShape(10.dp)
                    )
                  }
                }
              }
            }
          }

          // 3. Discount & GST Taxes
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text("Discounts & Taxes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("Discount:", fontSize = 13.sp)
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0.0, 5.0, 10.0).forEach { disc ->
                      FilterChip(
                        selected = discountPct == disc,
                        onClick = { viewModel.discountPercent.value = disc },
                        label = { Text("${disc.toInt()}%") }
                      )
                    }
                  }
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("GST Tax:", fontSize = 13.sp)
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0.0, 5.0, 12.0, 18.0).forEach { tax ->
                      FilterChip(
                        selected = taxPct == tax,
                        onClick = { viewModel.taxPercent.value = tax },
                        label = { Text("${tax.toInt()}%") }
                      )
                    }
                  }
                }
              }
            }
          }

          // 4. Payment Mode Selection
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  val modes = listOf(
                    Triple("CASH", "Cash", Icons.Filled.Money),
                    Triple("UPI", "UPI QR", Icons.Filled.QrCode2),
                    Triple("KHATA_CREDIT", "Udhar", Icons.Filled.Book)
                  )
                  modes.forEach { (modeCode, modeLabel, modeIcon) ->
                    val isSelected = paymentMode == modeCode
                    OutlinedButton(
                      onClick = { viewModel.paymentMode.value = modeCode },
                      colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) EmeraldContainer else Color.Transparent,
                        contentColor = if (isSelected) OnEmeraldContainer else MaterialTheme.colorScheme.onSurface
                      ),
                      border = ButtonDefaults.outlinedButtonBorder.takeIf { !isSelected },
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier.weight(1f)
                    ) {
                      Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(modeIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(modeLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                      }
                    }
                  }
                }

                // If UPI selected -> Show Live Dynamic QR Code
                if (paymentMode == "UPI") {
                  Spacer(modifier = Modifier.height(16.dp))
                  Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    val upiUri = PaymentUtils.generateUpiUri(
                      upiId = profile?.upiId ?: "merchant@upi",
                      merchantName = profile?.shopName ?: "Store",
                      amount = grandTotal,
                      transactionNote = "Bill POS Payment"
                    )

                    DynamicQrCode(upiUri = upiUri, sizeDp = 160)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = "Scan to Pay: ${PaymentUtils.formatCurrency(grandTotal)}",
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp,
                      color = EmeraldPrimary
                    )
                    Text(
                      text = "UPI ID: ${profile?.upiId}",
                      fontSize = 12.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    FilledTonalButton(
                      onClick = {
                        PaymentUtils.openUpiPayment(
                          context = context,
                          upiId = profile?.upiId ?: "merchant@upi",
                          merchantName = profile?.shopName ?: "Store",
                          amount = grandTotal,
                          note = "DukaanPro Bill Payment"
                        )
                      }
                    ) {
                      Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("Open UPI App on Phone")
                    }
                  }
                }

                // If CASH selected -> Show Cash Received & Change Calculator
                if (paymentMode == "CASH") {
                  Spacer(modifier = Modifier.height(14.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    OutlinedTextField(
                      value = cashReceived,
                      onValueChange = { viewModel.cashReceivedAmount.value = it },
                      label = { Text("Cash Received (₹)") },
                      leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
                      modifier = Modifier.weight(1f),
                      singleLine = true,
                      shape = RoundedCornerShape(10.dp)
                    )

                    Column(
                      modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (changeDue > 0) EmeraldContainer else SlateContainer)
                        .padding(10.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      Text("Change to Return", fontSize = 10.sp, color = if (changeDue > 0) OnEmeraldContainer else OnSlateContainer)
                      Text(
                        text = PaymentUtils.formatCurrency(changeDue),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (changeDue > 0) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                }

                // If KHATA_CREDIT selected -> Check if customer selected
                if (paymentMode == "KHATA_CREDIT" && selectedCustomer == null) {
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    text = "⚠️ Please select a customer above to record this bill under their Udhar Khata ledger.",
                    color = CrimsonDebt,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }
          }

          // 5. Total Price Bill Summary Card
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Items Subtotal:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(PaymentUtils.formatCurrency(subtotal), fontSize = 13.sp)
                }

                if (discountAmount > 0) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Discount (${discountPct.toInt()}%):", fontSize = 13.sp, color = MintReceived)
                    Text("-${PaymentUtils.formatCurrency(discountAmount)}", fontSize = 13.sp, color = MintReceived, fontWeight = FontWeight.Bold)
                  }
                }

                if (taxAmount > 0) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("GST (${taxPct.toInt()}%):", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("+${PaymentUtils.formatCurrency(taxAmount)}", fontSize = 13.sp)
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("GRAND TOTAL:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                  Text(
                    text = PaymentUtils.formatCurrency(grandTotal),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = EmeraldPrimary
                  )
                }
              }
            }
          }

          // 6. Complete Bill & Generate Invoice Action Button
          item {
            Button(
              onClick = {
                viewModel.completeCurrentBill { createdInvoice ->
                  viewModel.activeInvoicePreview.value = createdInvoice
                }
              },
              enabled = grandTotal > 0 && !(paymentMode == "KHATA_CREDIT" && selectedCustomer == null),
              modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_complete_invoice"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
              Icon(Icons.Filled.CheckCircle, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("COMPLETE BILL & GENERATE INVOICE", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
fun ProductPosCatalogRow(
  product: ProductEntity,
  inCartQuantity: Double,
  onAdd: () -> Unit,
  onRemove: () -> Unit
) {
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
      Column(modifier = Modifier.weight(1f)) {
        Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(
          text = "${product.category} • Stock: ${product.stockQuantity.toInt()} ${product.unit}",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = PaymentUtils.formatCurrency(product.sellingPrice),
          fontWeight = FontWeight.ExtraBold,
          fontSize = 14.sp,
          color = EmeraldPrimary
        )
      }

      if (inCartQuantity > 0) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(EmeraldContainer)
            .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
          IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = OnEmeraldContainer, modifier = Modifier.size(16.dp))
          }
          Text(
            text = "${inCartQuantity.toInt()}",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = OnEmeraldContainer,
            modifier = Modifier.padding(horizontal = 4.dp)
          )
          IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Add, contentDescription = "Increase", tint = OnEmeraldContainer, modifier = Modifier.size(16.dp))
          }
        }
      } else {
        Button(
          onClick = onAdd,
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier.height(36.dp)
        ) {
          Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun CartItemRow(
  item: InvoiceCartItem,
  onIncrease: () -> Unit,
  onDecrease: () -> Unit,
  onDelete: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
      Text(
        text = "${PaymentUtils.formatCurrency(item.unitPrice)} × ${item.quantity.toInt()} ${item.unit}",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Text(
      text = PaymentUtils.formatCurrency(item.total),
      fontWeight = FontWeight.Bold,
      fontSize = 14.sp,
      modifier = Modifier.padding(horizontal = 8.dp)
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
        Icon(Icons.Filled.Remove, contentDescription = "Minus", modifier = Modifier.size(14.dp))
      }
      Text("${item.quantity.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
      IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) {
        Icon(Icons.Filled.Add, contentDescription = "Plus", modifier = Modifier.size(14.dp))
      }
      IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
        Icon(Icons.Outlined.Delete, contentDescription = "Remove", tint = CrimsonDebt, modifier = Modifier.size(16.dp))
      }
    }
  }
}
