package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.DukaanDatabase
import com.example.data.model.*
import com.example.data.repository.DukaanRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class AppScreen(val title: String) {
  DASHBOARD("Dashboard"),
  KHATA("Digital Khata"),
  INVENTORY("Stock & Products"),
  BILLING("POS Counter"),
  EXPENSES("Expenses"),
  REPORTS("Analytics & P&L"),
  SUBSCRIPTION("Plans & Billing"),
  PROFILE("Shop Settings"),
  ADMIN_PANEL("Platform Admin")
}

class DukaanViewModel(application: Application) : AndroidViewModel(application) {

  private val database = DukaanDatabase.getDatabase(application, viewModelScope)
  val repository = DukaanRepository(database)

  // ---------------- AUTHENTICATION & SESSION ----------------
  val currentUser = MutableStateFlow<UserEntity?>(null)
  val currentStore = MutableStateFlow<StoreEntity?>(null)

  // Stores owned by currently logged-in user
  val userStores = currentUser.flatMapLatest { user ->
    if (user == null) flowOf(emptyList())
    else repository.getStoresForUser(user.id)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Admin access to all stores and users
  val allStoresForAdmin = repository.getAllStores().stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    emptyList()
  )
  val allUsersForAdmin = repository.getAllUsers().stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    emptyList()
  )

  init {
    // Monitor userStores and assign currentStore if user has at least one store and none selected
    viewModelScope.launch {
      userStores.collect { stores ->
        if (currentUser.value != null && currentStore.value == null && stores.isNotEmpty()) {
          currentStore.value = stores.first()
        }
      }
    }
  }

  fun login(identifier: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    viewModelScope.launch {
      val user = repository.login(identifier, pass)
      if (user != null) {
        if (user.status == "SUSPENDED") {
          onError("This account has been suspended by the platform administrator.")
          return@launch
        }
        currentUser.value = user
        // Stores will be observed by userStores Flow
        onSuccess()
      } else {
        onError("Invalid email/phone or password. Please try again.")
      }
    }
  }

  fun register(name: String, email: String, phone: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    viewModelScope.launch {
      try {
        val newUserId = repository.register(name, email, phone, pass, role = "MERCHANT")
        currentUser.value = UserEntity(
          id = newUserId,
          name = name,
          email = email,
          phone = phone,
          password = pass,
          role = "MERCHANT"
        )
        // Newly registered user has NO store!
        currentStore.value = null
        onSuccess()
      } catch (e: Exception) {
        onError(e.localizedMessage ?: "Registration failed.")
      }
    }
  }

  fun loginDemoMerchant() {
    viewModelScope.launch {
      val user = repository.login("ramesh@kirana.com", "password123")
      currentUser.value = user ?: UserEntity(id = 1, name = "Ramesh Kumar", email = "ramesh@kirana.com", phone = "9876543210", password = "password123", role = "MERCHANT")
    }
  }

  fun loginDemoAdmin() {
    viewModelScope.launch {
      val admin = repository.login("admin@dukaanpro.com", "admin123")
      currentUser.value = admin ?: UserEntity(id = 2, name = "Super Admin", email = "admin@dukaanpro.com", phone = "9999999999", password = "admin123", role = "ADMIN")
      _currentScreen.value = AppScreen.ADMIN_PANEL
    }
  }

  fun logout() {
    currentUser.value = null
    currentStore.value = null
    _currentScreen.value = AppScreen.DASHBOARD
  }

  // ---------------- CREATE STORE (USER-OWNED) ----------------
  fun createStore(
    storeName: String,
    category: String,
    ownerName: String,
    phone: String,
    address: String,
    gstin: String,
    upiId: String,
    onSuccess: () -> Unit
  ) {
    viewModelScope.launch {
      val user = currentUser.value ?: return@launch
      val newStore = StoreEntity(
        userId = user.id,
        storeName = storeName,
        category = category,
        ownerName = ownerName.ifBlank { user.name },
        phone = phone.ifBlank { user.phone },
        email = user.email,
        address = address,
        gstin = gstin,
        upiId = upiId,
        subscriptionTier = "PRO",
        status = "APPROVED"
      )
      val storeId = repository.createStore(newStore)
      val savedStore = newStore.copy(id = storeId)
      currentStore.value = savedStore

      // Seed standard category starter pack for the brand new store
      seedStorePresets(storeId, category)
      onSuccess()
    }
  }

  private suspend fun seedStorePresets(storeId: Long, category: String) {
    val starterProducts = listOf(
      ProductEntity(storeId = storeId, name = "Standard Package Item A", category = category, costPrice = 80.0, sellingPrice = 99.0, stockQuantity = 30.0, unit = "pcs"),
      ProductEntity(storeId = storeId, name = "Standard Package Item B", category = category, costPrice = 140.0, sellingPrice = 175.0, stockQuantity = 20.0, unit = "packet"),
      ProductEntity(storeId = storeId, name = "Premium Quality Essentials", category = category, costPrice = 220.0, sellingPrice = 260.0, stockQuantity = 15.0, unit = "packet")
    )
    for (p in starterProducts) {
      repository.insertProduct(p)
    }
  }

  fun selectStore(store: StoreEntity) {
    currentStore.value = store
  }

  // ---------------- SCREEN NAVIGATION ----------------
  private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  fun navigateTo(screen: AppScreen) {
    _currentScreen.value = screen
  }

  // ---------------- STORE-SCOPED DATA FLOWS ----------------
  val activeStoreId: StateFlow<Long> = currentStore.map { it?.id ?: 1L }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1L)

  val allCustomers = activeStoreId.flatMapLatest { storeId ->
    repository.getCustomersByStore(storeId)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allProducts = activeStoreId.flatMapLatest { storeId ->
    repository.getProductsByStore(storeId)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allInvoices = activeStoreId.flatMapLatest { storeId ->
    repository.getInvoicesByStore(storeId)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allExpenses = activeStoreId.flatMapLatest { storeId ->
    repository.getExpensesByStore(storeId)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Customer search & filters
  val customerSearchQuery = MutableStateFlow("")
  val customerFilterMode = MutableStateFlow("ALL")

  val filteredCustomers = combine(allCustomers, customerSearchQuery, customerFilterMode) { customers, query, mode ->
    customers.filter { customer ->
      val matchesQuery = customer.name.contains(query, ignoreCase = true) ||
                         customer.phone.contains(query, ignoreCase = true) ||
                         customer.address.contains(query, ignoreCase = true)
      val matchesMode = when (mode) {
        "DUE_ONLY" -> customer.totalDue > 0.01
        "SETTLED" -> customer.totalDue <= 0.01
        else -> true
      }
      matchesQuery && matchesMode
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val selectedCustomerForKhata = MutableStateFlow<CustomerEntity?>(null)

  val khataEntriesForSelectedCustomer = selectedCustomerForKhata.flatMapLatest { customer ->
    if (customer == null) flowOf(emptyList())
    else repository.getKhataForCustomer(customer.id)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Product search & filters
  val productSearchQuery = MutableStateFlow("")
  val productCategoryFilter = MutableStateFlow("All")
  val lowStockOnlyFilter = MutableStateFlow(false)

  val filteredProducts = combine(
    allProducts,
    productSearchQuery,
    productCategoryFilter,
    lowStockOnlyFilter
  ) { products, query, category, lowStockOnly ->
    products.filter { product ->
      val matchesQuery = product.name.contains(query, ignoreCase = true) ||
                         product.category.contains(query, ignoreCase = true) ||
                         product.sku.contains(query, ignoreCase = true)
      val matchesCategory = (category == "All") || product.category.equals(category, ignoreCase = true)
      val matchesStock = if (lowStockOnly) product.stockQuantity <= product.minStockAlert else true
      matchesQuery && matchesCategory && matchesStock
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Business Profile
  val businessProfile = currentStore.map { store ->
    if (store != null) {
      BusinessProfileEntity(
        id = store.id.toInt(),
        shopName = store.storeName,
        ownerName = store.ownerName,
        phone = store.phone,
        email = store.email,
        address = store.address,
        gstin = store.gstin,
        upiId = store.upiId,
        invoiceFooter = store.invoiceFooter,
        subscriptionTier = store.subscriptionTier,
        subscriptionExpiry = store.subscriptionExpiry
      )
    } else {
      BusinessProfileEntity()
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessProfileEntity())

  // ---------------- POS BILLING CART STATE ----------------
  val cartItems = MutableStateFlow<List<InvoiceCartItem>>(emptyList())
  val selectedBillingCustomer = MutableStateFlow<CustomerEntity?>(null)
  val walkInCustomerName = MutableStateFlow("")
  val walkInCustomerPhone = MutableStateFlow("")
  val discountPercent = MutableStateFlow(0.0)
  val taxPercent = MutableStateFlow(5.0)
  val paymentMode = MutableStateFlow("CASH")
  val cashReceivedAmount = MutableStateFlow("")

  val cartSubtotal = cartItems.map { items ->
    items.sumOf { it.total }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  val cartDiscountAmount = combine(cartSubtotal, discountPercent) { subtotal, disc ->
    (subtotal * (disc / 100.0))
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  val cartTaxAmount = combine(cartSubtotal, cartDiscountAmount, taxPercent) { subtotal, disc, tax ->
    val taxable = (subtotal - disc).coerceAtLeast(0.0)
    (taxable * (tax / 100.0))
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  val cartGrandTotal = combine(cartSubtotal, cartDiscountAmount, cartTaxAmount) { subtotal, disc, tax ->
    (subtotal - disc + tax).coerceAtLeast(0.0)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  fun addToCart(product: ProductEntity, quantity: Double = 1.0) {
    val current = cartItems.value.toMutableList()
    val existingIndex = current.indexOfFirst { it.productId == product.id }
    if (existingIndex >= 0) {
      val existing = current[existingIndex]
      val updatedQty = existing.quantity + quantity
      current[existingIndex] = existing.copy(quantity = updatedQty, total = updatedQty * existing.unitPrice)
    } else {
      current.add(
        InvoiceCartItem(
          productId = product.id,
          productName = product.name,
          unitPrice = product.sellingPrice,
          quantity = quantity,
          unit = product.unit,
          total = product.sellingPrice * quantity
        )
      )
    }
    cartItems.value = current
  }

  fun updateCartQuantity(productId: Long, newQuantity: Double) {
    val current = cartItems.value.toMutableList()
    val index = current.indexOfFirst { it.productId == productId }
    if (index >= 0) {
      if (newQuantity <= 0.0) {
        current.removeAt(index)
      } else {
        val existing = current[index]
        current[index] = existing.copy(quantity = newQuantity, total = newQuantity * existing.unitPrice)
      }
      cartItems.value = current
    }
  }

  fun removeFromCart(productId: Long) {
    cartItems.value = cartItems.value.filter { it.productId != productId }
  }

  fun clearCart() {
    cartItems.value = emptyList()
    selectedBillingCustomer.value = null
    walkInCustomerName.value = ""
    walkInCustomerPhone.value = ""
    discountPercent.value = 0.0
    cashReceivedAmount.value = ""
  }

  fun completeCurrentBill(onSuccess: (InvoiceEntity) -> Unit) {
    viewModelScope.launch {
      val items = cartItems.value
      if (items.isEmpty()) return@launch

      val storeId = activeStoreId.value
      val subtotal = cartSubtotal.value
      val disc = cartDiscountAmount.value
      val tax = cartTaxAmount.value
      val grand = cartGrandTotal.value

      val customer = selectedBillingCustomer.value
      val name = customer?.name ?: walkInCustomerName.value.ifBlank { "Walk-in Customer" }
      val phone = customer?.phone ?: walkInCustomerPhone.value

      val invNumber = "INV-${System.currentTimeMillis() % 1000000}"

      val jsonArray = JSONArray()
      for (item in items) {
        val obj = JSONObject()
        obj.put("productId", item.productId)
        obj.put("productName", item.productName)
        obj.put("unitPrice", item.unitPrice)
        obj.put("quantity", item.quantity)
        obj.put("unit", item.unit)
        obj.put("total", item.total)
        jsonArray.put(obj)
      }

      val mode = paymentMode.value
      val invoice = InvoiceEntity(
        storeId = storeId,
        invoiceNumber = invNumber,
        customerId = customer?.id,
        customerName = name,
        customerPhone = phone,
        subtotal = subtotal,
        taxAmount = tax,
        discountAmount = disc,
        grandTotal = grand,
        paymentMode = mode,
        itemsJson = jsonArray.toString(),
        timestamp = System.currentTimeMillis(),
        status = if (mode == "KHATA_CREDIT") "DUE" else "PAID"
      )

      val invoiceId = repository.completeSale(
        invoice = invoice,
        cartItems = items,
        customerCurrentDue = customer?.totalDue ?: 0.0
      )

      val createdInvoice = invoice.copy(id = invoiceId)
      clearCart()
      onSuccess(createdInvoice)
    }
  }

  // ---------------- KHATA ACTIONS ----------------
  fun addKhataTransaction(customerId: Long, type: String, amount: Double, note: String) {
    viewModelScope.launch {
      val storeId = activeStoreId.value
      val customer = allCustomers.value.find { it.id == customerId } ?: return@launch
      repository.recordKhataTransaction(storeId, customerId, type, amount, note, customer.totalDue)
      selectedCustomerForKhata.value = customer.copy(
        totalDue = if (type == "CREDIT_GAVE") customer.totalDue + amount else customer.totalDue - amount
      )
    }
  }

  fun addCustomer(name: String, phone: String, address: String, initialDue: Double) {
    viewModelScope.launch {
      val storeId = activeStoreId.value
      val id = repository.insertCustomer(
        CustomerEntity(
          storeId = storeId,
          name = name,
          phone = phone,
          address = address,
          totalDue = initialDue
        )
      )
      if (initialDue > 0) {
        repository.recordKhataTransaction(storeId, id, "CREDIT_GAVE", initialDue, "Initial Balance", 0.0)
      }
    }
  }

  fun updateCustomer(customer: CustomerEntity) {
    viewModelScope.launch {
      repository.updateCustomer(customer)
      if (selectedCustomerForKhata.value?.id == customer.id) {
        selectedCustomerForKhata.value = customer
      }
    }
  }

  fun deleteCustomer(customer: CustomerEntity) {
    viewModelScope.launch {
      repository.deleteCustomer(customer)
      if (selectedCustomerForKhata.value?.id == customer.id) {
        selectedCustomerForKhata.value = null
      }
    }
  }

  // ---------------- PRODUCT & INVENTORY ACTIONS ----------------
  fun saveProduct(product: ProductEntity) {
    viewModelScope.launch {
      val storeId = activeStoreId.value
      val productWithStore = if (product.storeId == 0L) product.copy(storeId = storeId) else product
      if (productWithStore.id == 0L) {
        repository.insertProduct(productWithStore)
      } else {
        repository.updateProduct(productWithStore)
      }
    }
  }

  fun deleteProduct(product: ProductEntity) {
    viewModelScope.launch {
      repository.deleteProduct(product)
    }
  }

  fun restockProduct(productId: Long, addedQuantity: Double) {
    viewModelScope.launch {
      repository.adjustStock(productId, addedQuantity, isAdd = true)
    }
  }

  // ---------------- EXPENSE ACTIONS ----------------
  fun addExpense(title: String, category: String, amount: Double, paymentMethod: String, note: String) {
    viewModelScope.launch {
      val storeId = activeStoreId.value
      repository.insertExpense(
        ExpenseEntity(
          storeId = storeId,
          title = title,
          category = category,
          amount = amount,
          paymentMethod = paymentMethod,
          note = note
        )
      )
    }
  }

  fun deleteExpense(expense: ExpenseEntity) {
    viewModelScope.launch {
      repository.deleteExpense(expense)
    }
  }

  // ---------------- SUBSCRIPTION ACTIONS ----------------
  fun upgradeSubscription(tier: String, onSuccess: () -> Unit) {
    viewModelScope.launch {
      val store = currentStore.value
      if (store != null) {
        repository.updateStoreTier(store.id, tier)
        currentStore.value = store.copy(subscriptionTier = tier)
      }
      onSuccess()
    }
  }

  // ---------------- ADMIN PANEL ACTIONS ----------------
  fun adminUpdateStoreStatus(storeId: Long, status: String) {
    viewModelScope.launch {
      repository.updateStoreStatus(storeId, status)
      if (currentStore.value?.id == storeId) {
        currentStore.value = currentStore.value?.copy(status = status)
      }
    }
  }

  fun adminUpdateStoreTier(storeId: Long, tier: String) {
    viewModelScope.launch {
      repository.updateStoreTier(storeId, tier)
      if (currentStore.value?.id == storeId) {
        currentStore.value = currentStore.value?.copy(subscriptionTier = tier)
      }
    }
  }

  fun adminToggleUserStatus(userId: Long, currentStatus: String) {
    viewModelScope.launch {
      val newStatus = if (currentStatus == "ACTIVE") "SUSPENDED" else "ACTIVE"
      repository.updateUserStatus(userId, newStatus)
    }
  }

  // ---------------- PROFILE & SETTINGS ACTIONS ----------------
  fun saveProfile(profile: BusinessProfileEntity) {
    viewModelScope.launch {
      val store = currentStore.value
      if (store != null) {
        val updatedStore = store.copy(
          storeName = profile.shopName,
          ownerName = profile.ownerName,
          phone = profile.phone,
          email = profile.email,
          address = profile.address,
          gstin = profile.gstin,
          upiId = profile.upiId,
          invoiceFooter = profile.invoiceFooter
        )
        repository.updateStore(updatedStore)
        currentStore.value = updatedStore
      }
    }
  }

  fun resetData() {
    viewModelScope.launch {
      repository.resetDemoData()
    }
  }

  // ---------------- DIALOG AND UI MODAL STATES ----------------
  val showAddCustomerDialog = MutableStateFlow(false)
  val showKhataEntryDialog = MutableStateFlow(false)
  val showAddProductDialog = MutableStateFlow<ProductEntity?>(null)
  val showAddExpenseDialog = MutableStateFlow(false)
  val activeInvoicePreview = MutableStateFlow<InvoiceEntity?>(null)
  val showSubscriptionCheckout = MutableStateFlow<Triple<String, Double, String>?>(null)
}
