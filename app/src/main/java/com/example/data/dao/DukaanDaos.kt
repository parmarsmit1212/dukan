package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
  suspend fun getUserByEmail(email: String): UserEntity?

  @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
  suspend fun getUserByPhone(phone: String): UserEntity?

  @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
  fun getUserById(id: Long): Flow<UserEntity?>

  @Query("SELECT * FROM users ORDER BY createdAt DESC")
  fun getAllUsers(): Flow<List<UserEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity): Long

  @Update
  suspend fun updateUser(user: UserEntity)

  @Query("UPDATE users SET status = :status WHERE id = :userId")
  suspend fun updateUserStatus(userId: Long, status: String)
}

@Dao
interface StoreDao {
  @Query("SELECT * FROM stores WHERE userId = :userId ORDER BY createdAt DESC")
  fun getStoresForUser(userId: Long): Flow<List<StoreEntity>>

  @Query("SELECT * FROM stores ORDER BY createdAt DESC")
  fun getAllStores(): Flow<List<StoreEntity>>

  @Query("SELECT * FROM stores WHERE id = :id LIMIT 1")
  fun getStoreById(id: Long): Flow<StoreEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStore(store: StoreEntity): Long

  @Update
  suspend fun updateStore(store: StoreEntity)

  @Delete
  suspend fun deleteStore(store: StoreEntity)

  @Query("UPDATE stores SET status = :status WHERE id = :storeId")
  suspend fun updateStoreStatus(storeId: Long, status: String)

  @Query("UPDATE stores SET subscriptionTier = :tier WHERE id = :storeId")
  suspend fun updateStoreTier(storeId: Long, tier: String)
}

@Dao
interface CustomerDao {
  @Query("SELECT * FROM customers WHERE storeId = :storeId ORDER BY lastTransactionDate DESC")
  fun getCustomersByStore(storeId: Long): Flow<List<CustomerEntity>>

  @Query("SELECT * FROM customers ORDER BY lastTransactionDate DESC")
  fun getAllCustomers(): Flow<List<CustomerEntity>>

  @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
  fun getCustomerById(id: Long): Flow<CustomerEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomer(customer: CustomerEntity): Long

  @Update
  suspend fun updateCustomer(customer: CustomerEntity)

  @Delete
  suspend fun deleteCustomer(customer: CustomerEntity)

  @Query("UPDATE customers SET totalDue = :newDue, lastTransactionDate = :timestamp WHERE id = :id")
  suspend fun updateBalance(id: Long, newDue: Double, timestamp: Long)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(customers: List<CustomerEntity>)
}

@Dao
interface KhataDao {
  @Query("SELECT * FROM khata_entries WHERE customerId = :customerId ORDER BY timestamp DESC")
  fun getEntriesForCustomer(customerId: Long): Flow<List<KhataEntryEntity>>

  @Query("SELECT * FROM khata_entries WHERE storeId = :storeId ORDER BY timestamp DESC")
  fun getEntriesByStore(storeId: Long): Flow<List<KhataEntryEntity>>

  @Query("SELECT * FROM khata_entries ORDER BY timestamp DESC")
  fun getAllEntries(): Flow<List<KhataEntryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEntry(entry: KhataEntryEntity): Long

  @Delete
  suspend fun deleteEntry(entry: KhataEntryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(entries: List<KhataEntryEntity>)
}

@Dao
interface ProductDao {
  @Query("SELECT * FROM products WHERE storeId = :storeId ORDER BY name ASC")
  fun getProductsByStore(storeId: Long): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products ORDER BY name ASC")
  fun getAllProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
  fun getProductById(id: Long): Flow<ProductEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: ProductEntity): Long

  @Update
  suspend fun updateProduct(product: ProductEntity)

  @Delete
  suspend fun deleteProduct(product: ProductEntity)

  @Query("UPDATE products SET stockQuantity = CASE WHEN (stockQuantity - :quantity) < 0 THEN 0 ELSE (stockQuantity - :quantity) END, updatedAt = :timestamp WHERE id = :productId")
  suspend fun reduceStock(productId: Long, quantity: Double, timestamp: Long = System.currentTimeMillis())

  @Query("UPDATE products SET stockQuantity = stockQuantity + :quantity, updatedAt = :timestamp WHERE id = :productId")
  suspend fun addStock(productId: Long, quantity: Double, timestamp: Long = System.currentTimeMillis())

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(products: List<ProductEntity>)
}

@Dao
interface InvoiceDao {
  @Query("SELECT * FROM invoices WHERE storeId = :storeId ORDER BY timestamp DESC")
  fun getInvoicesByStore(storeId: Long): Flow<List<InvoiceEntity>>

  @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
  fun getAllInvoices(): Flow<List<InvoiceEntity>>

  @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
  fun getInvoiceById(id: Long): Flow<InvoiceEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInvoice(invoice: InvoiceEntity): Long

  @Delete
  suspend fun deleteInvoice(invoice: InvoiceEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(invoices: List<InvoiceEntity>)
}

@Dao
interface ExpenseDao {
  @Query("SELECT * FROM expenses WHERE storeId = :storeId ORDER BY timestamp DESC")
  fun getExpensesByStore(storeId: Long): Flow<List<ExpenseEntity>>

  @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
  fun getAllExpenses(): Flow<List<ExpenseEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpense(expense: ExpenseEntity): Long

  @Delete
  suspend fun deleteExpense(expense: ExpenseEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(expenses: List<ExpenseEntity>)
}

@Dao
interface BusinessProfileDao {
  @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
  fun getProfile(): Flow<BusinessProfileEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(profile: BusinessProfileEntity)
}
