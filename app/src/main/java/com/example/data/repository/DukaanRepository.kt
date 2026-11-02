package com.example.data.repository

import com.example.data.database.DukaanDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DukaanRepository(private val database: DukaanDatabase) {
  private val userDao = database.userDao()
  private val storeDao = database.storeDao()
  private val customerDao = database.customerDao()
  private val khataDao = database.khataDao()
  private val productDao = database.productDao()
  private val invoiceDao = database.invoiceDao()
  private val expenseDao = database.expenseDao()
  private val profileDao = database.businessProfileDao()

  // ---------------- USER & AUTH ----------------
  suspend fun login(identifier: String, password: String): UserEntity? = withContext(Dispatchers.IO) {
    val clean = identifier.trim()
    val user = if (clean.contains("@")) {
      userDao.getUserByEmail(clean)
    } else {
      userDao.getUserByPhone(clean)
    }
    if (user != null && user.password == password) user else null
  }

  suspend fun register(name: String, email: String, phone: String, password: String, role: String = "MERCHANT"): Long = withContext(Dispatchers.IO) {
    userDao.insertUser(
      UserEntity(
        name = name,
        email = email.trim(),
        phone = phone.trim(),
        password = password,
        role = role
      )
    )
  }

  fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

  suspend fun updateUserStatus(userId: Long, status: String) = withContext(Dispatchers.IO) {
    userDao.updateUserStatus(userId, status)
  }

  // ---------------- STORES ----------------
  fun getStoresForUser(userId: Long): Flow<List<StoreEntity>> = storeDao.getStoresForUser(userId)

  fun getAllStores(): Flow<List<StoreEntity>> = storeDao.getAllStores()

  fun getStoreById(storeId: Long): Flow<StoreEntity?> = storeDao.getStoreById(storeId)

  suspend fun createStore(store: StoreEntity): Long = withContext(Dispatchers.IO) {
    storeDao.insertStore(store)
  }

  suspend fun updateStore(store: StoreEntity) = withContext(Dispatchers.IO) {
    storeDao.updateStore(store)
  }

  suspend fun updateStoreStatus(storeId: Long, status: String) = withContext(Dispatchers.IO) {
    storeDao.updateStoreStatus(storeId, status)
  }

  suspend fun updateStoreTier(storeId: Long, tier: String) = withContext(Dispatchers.IO) {
    storeDao.updateStoreTier(storeId, tier)
  }

  // ---------------- STORE-SCOPED DATA ----------------
  fun getCustomersByStore(storeId: Long): Flow<List<CustomerEntity>> = customerDao.getCustomersByStore(storeId)
  val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

  fun getProductsByStore(storeId: Long): Flow<List<ProductEntity>> = productDao.getProductsByStore(storeId)
  val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

  fun getInvoicesByStore(storeId: Long): Flow<List<InvoiceEntity>> = invoiceDao.getInvoicesByStore(storeId)
  val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()

  fun getExpensesByStore(storeId: Long): Flow<List<ExpenseEntity>> = expenseDao.getExpensesByStore(storeId)
  val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

  fun getKhataByStore(storeId: Long): Flow<List<KhataEntryEntity>> = khataDao.getEntriesByStore(storeId)
  fun getKhataForCustomer(customerId: Long): Flow<List<KhataEntryEntity>> = khataDao.getEntriesForCustomer(customerId)

  // Profile
  val businessProfile: Flow<BusinessProfileEntity?> = profileDao.getProfile()

  suspend fun updateBusinessProfile(profile: BusinessProfileEntity) = withContext(Dispatchers.IO) {
    profileDao.insertOrUpdate(profile)
  }

  // Customer Actions
  suspend fun insertCustomer(customer: CustomerEntity): Long = withContext(Dispatchers.IO) {
    customerDao.insertCustomer(customer)
  }

  suspend fun updateCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
    customerDao.updateCustomer(customer)
  }

  suspend fun deleteCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
    customerDao.deleteCustomer(customer)
  }

  // Khata Actions
  suspend fun recordKhataTransaction(
    storeId: Long,
    customerId: Long,
    type: String, // "CREDIT_GAVE" or "PAYMENT_RECEIVED"
    amount: Double,
    note: String,
    currentDue: Double
  ): Long = withContext(Dispatchers.IO) {
    val entryId = khataDao.insertEntry(
      KhataEntryEntity(
        storeId = storeId,
        customerId = customerId,
        type = type,
        amount = amount,
        note = note,
        timestamp = System.currentTimeMillis()
      )
    )
    val newDue = if (type == "CREDIT_GAVE") currentDue + amount else currentDue - amount
    customerDao.updateBalance(customerId, newDue, System.currentTimeMillis())
    entryId
  }

  // Product Actions
  suspend fun insertProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
    productDao.insertProduct(product)
  }

  suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
    productDao.updateProduct(product)
  }

  suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
    productDao.deleteProduct(product)
  }

  suspend fun adjustStock(productId: Long, quantity: Double, isAdd: Boolean) = withContext(Dispatchers.IO) {
    if (isAdd) {
      productDao.addStock(productId, quantity)
    } else {
      productDao.reduceStock(productId, quantity)
    }
  }

  // POS Complete Sale
  suspend fun completeSale(
    invoice: InvoiceEntity,
    cartItems: List<InvoiceCartItem>,
    customerCurrentDue: Double = 0.0
  ): Long = withContext(Dispatchers.IO) {
    val invoiceId = invoiceDao.insertInvoice(invoice)

    for (item in cartItems) {
      productDao.reduceStock(item.productId, item.quantity)
    }

    if (invoice.paymentMode == "KHATA_CREDIT" && invoice.customerId != null) {
      khataDao.insertEntry(
        KhataEntryEntity(
          storeId = invoice.storeId,
          customerId = invoice.customerId,
          type = "CREDIT_GAVE",
          amount = invoice.grandTotal,
          note = "Bill #${invoice.invoiceNumber}",
          invoiceId = invoiceId,
          timestamp = System.currentTimeMillis()
        )
      )
      val newDue = customerCurrentDue + invoice.grandTotal
      customerDao.updateBalance(invoice.customerId, newDue, System.currentTimeMillis())
    }

    invoiceId
  }

  // Expenses
  suspend fun insertExpense(expense: ExpenseEntity): Long = withContext(Dispatchers.IO) {
    expenseDao.insertExpense(expense)
  }

  suspend fun deleteExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
    expenseDao.deleteExpense(expense)
  }

  suspend fun resetDemoData() = withContext(Dispatchers.IO) {
    DukaanDatabase.populateInitialData(database)
  }
}
