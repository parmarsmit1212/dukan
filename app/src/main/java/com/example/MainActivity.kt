package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DukaanProBottomNav
import com.example.ui.components.DukaanProTopBar
import com.example.ui.dialogs.*
import com.example.ui.screens.*
import com.example.ui.theme.DukaanProTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.DukaanViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      DukaanProTheme {
        val viewModel: DukaanViewModel = viewModel()
        val currentUser by viewModel.currentUser.collectAsState()
        val currentStore by viewModel.currentStore.collectAsState()
        val currentScreen by viewModel.currentScreen.collectAsState()
        val profile by viewModel.businessProfile.collectAsState()
        val cartItems by viewModel.cartItems.collectAsState()
        val products by viewModel.allProducts.collectAsState()
        val customers by viewModel.allCustomers.collectAsState()

        val showAddCustomer by viewModel.showAddCustomerDialog.collectAsState()
        val showAddProduct by viewModel.showAddProductDialog.collectAsState()
        val activeInvoice by viewModel.activeInvoicePreview.collectAsState()

        var showStoreManager by remember { mutableStateOf(false) }
        var showNotificationsDialog by remember { mutableStateOf(false) }

        // Condition 1: User is NOT logged in -> Show Authentication Screen
        if (currentUser == null) {
          AuthScreen(viewModel = viewModel)
        }
        // Condition 2: User is logged in as Merchant but has NOT created a store yet -> Show Store Creation Screen
        else if (currentStore == null && currentUser?.role != "ADMIN") {
          CreateStoreScreen(viewModel = viewModel)
        }
        // Condition 3: User opened Store Manager manually
        else if (showStoreManager) {
          BackHandler { showStoreManager = false }
          CreateStoreScreen(viewModel = viewModel)
        }
        // Condition 4: Active Store or Admin -> Show Store / Platform interface
        else {
          val isAdmin = currentUser?.role == "ADMIN"

          // BackHandler to navigate back to Dashboard if in sub-screen
          if (currentScreen != AppScreen.DASHBOARD && currentScreen != AppScreen.ADMIN_PANEL) {
            BackHandler {
              viewModel.navigateTo(AppScreen.DASHBOARD)
            }
          }

          Scaffold(
            modifier = Modifier
              .fillMaxSize()
              .testTag("main_scaffold"),
            topBar = {
              DukaanProTopBar(
                shopName = currentStore?.storeName ?: (if (isAdmin) "Admin Console" else "DukaanPro"),
                subscriptionTier = currentStore?.subscriptionTier ?: "PRO",
                currentScreen = currentScreen,
                isAdmin = isAdmin,
                onOpenProfile = { viewModel.navigateTo(AppScreen.PROFILE) },
                onOpenNotifications = { showNotificationsDialog = true },
                onOpenAdminPanel = { viewModel.navigateTo(AppScreen.ADMIN_PANEL) },
                onOpenStoreManager = { showStoreManager = true },
                onLogout = { viewModel.logout() }
              )
            },
            bottomBar = {
              DukaanProBottomNav(
                currentScreen = currentScreen,
                cartItemCount = cartItems.sumOf { it.quantity.toInt() },
                onNavigate = { targetScreen -> viewModel.navigateTo(targetScreen) }
              )
            }
          ) { innerPadding ->
            Crossfade(
              targetState = currentScreen,
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
              label = "ScreenTransition"
            ) { screen ->
              when (screen) {
                AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppScreen.KHATA -> KhataScreen(viewModel = viewModel)
                AppScreen.BILLING -> BillingScreen(viewModel = viewModel)
                AppScreen.INVENTORY -> InventoryScreen(viewModel = viewModel)
                AppScreen.EXPENSES -> ExpensesScreen(viewModel = viewModel)
                AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
                AppScreen.SUBSCRIPTION -> SubscriptionScreen(viewModel = viewModel)
                AppScreen.PROFILE -> ProfileScreen(viewModel = viewModel)
                AppScreen.ADMIN_PANEL -> AdminPanelScreen(viewModel = viewModel)
              }
            }
          }

          // Global Dialog: Add Customer
          if (showAddCustomer) {
            AddCustomerDialog(
              onDismiss = { viewModel.showAddCustomerDialog.value = false },
              onSave = { name, phone, address, initialDue ->
                viewModel.addCustomer(name, phone, address, initialDue)
                viewModel.showAddCustomerDialog.value = false
              }
            )
          }

          // Global Dialog: Add / Edit Product
          if (showAddProduct != null) {
            AddEditProductDialog(
              product = showAddProduct!!,
              onDismiss = { viewModel.showAddProductDialog.value = null },
              onSave = { updatedProduct ->
                viewModel.saveProduct(updatedProduct)
                viewModel.showAddProductDialog.value = null
              }
            )
          }

          // Global Dialog: Generated Invoice Receipt Preview
          if (activeInvoice != null && profile != null) {
            InvoicePreviewDialog(
              invoice = activeInvoice!!,
              profile = profile!!,
              onDismiss = { viewModel.activeInvoicePreview.value = null }
            )
          }

          // Global Dialog: Notifications & Alerts
          if (showNotificationsDialog) {
            val lowStock = products.filter { it.stockQuantity <= it.minStockAlert }
            val customersWithDue = customers.filter { it.totalDue > 0.01 }
            NotificationsDialog(
              lowStockProducts = lowStock,
              customersWithDue = customersWithDue,
              onDismiss = { showNotificationsDialog = false }
            )
          }
        }
      }
    }
  }
}
