package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    UserEntity::class,
    StoreEntity::class,
    CustomerEntity::class,
    KhataEntryEntity::class,
    ProductEntity::class,
    InvoiceEntity::class,
    ExpenseEntity::class,
    BusinessProfileEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class DukaanDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
  abstract fun storeDao(): StoreDao
  abstract fun customerDao(): CustomerDao
  abstract fun khataDao(): KhataDao
  abstract fun productDao(): ProductDao
  abstract fun invoiceDao(): InvoiceDao
  abstract fun expenseDao(): ExpenseDao
  abstract fun businessProfileDao(): BusinessProfileDao

  companion object {
    @Volatile
    private var INSTANCE: DukaanDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): DukaanDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          DukaanDatabase::class.java,
          "dukaanpro_db"
        )
        .fallbackToDestructiveMigration()
        .addCallback(DukaanDatabaseCallback(scope))
        .build()
        INSTANCE = instance
        instance
      }
    }

    private class DukaanDatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(database)
          }
        }
      }
    }

    suspend fun populateInitialData(database: DukaanDatabase) {
      val userDao = database.userDao()
      val storeDao = database.storeDao()
      val profileDao = database.businessProfileDao()
      val productDao = database.productDao()
      val customerDao = database.customerDao()
      val khataDao = database.khataDao()
      val expenseDao = database.expenseDao()
      val invoiceDao = database.invoiceDao()

      // 1. Seed Accounts: Admin and Sample Merchant
      val merchantUser = UserEntity(
        id = 1,
        name = "Ramesh Kumar",
        email = "ramesh@kirana.com",
        phone = "9876543210",
        password = "password123",
        role = "MERCHANT"
      )
      val adminUser = UserEntity(
        id = 2,
        name = "Super Admin",
        email = "admin@dukaanpro.com",
        phone = "9999999999",
        password = "admin123",
        role = "ADMIN"
      )
      userDao.insertUser(merchantUser)
      userDao.insertUser(adminUser)

      // 2. Seed Initial Demo Store owned by Ramesh (User 1)
      val demoStore = StoreEntity(
        id = 1,
        userId = 1,
        storeName = "Shree Ganesh Kirana & General Store",
        category = "Kirana & Grocery",
        ownerName = "Ramesh Kumar",
        phone = "+91 98765 43210",
        email = "ramesh@kirana.com",
        address = "Shop #4, Market Complex, Station Road, Kota",
        gstin = "08AAAAA0000A1Z5",
        upiId = "rameshkirana@okaxis",
        invoiceFooter = "Thank you for shopping with us! Visit again.",
        subscriptionTier = "PRO",
        status = "APPROVED"
      )
      storeDao.insertStore(demoStore)

      // Seed Business Profile
      profileDao.insertOrUpdate(
        BusinessProfileEntity(
          id = 1,
          shopName = demoStore.storeName,
          ownerName = demoStore.ownerName,
          phone = demoStore.phone,
          email = demoStore.email,
          address = demoStore.address,
          gstin = demoStore.gstin,
          upiId = demoStore.upiId,
          invoiceFooter = demoStore.invoiceFooter,
          currencySymbol = "₹",
          subscriptionTier = demoStore.subscriptionTier
        )
      )

      // Seed Products for Store 1
      val products = listOf(
        ProductEntity(
          storeId = 1,
          name = "Aashirvaad Shudh Chakki Atta 5kg",
          category = "Atta & Flours",
          costPrice = 210.0,
          sellingPrice = 245.0,
          stockQuantity = 38.0,
          unit = "packet",
          sku = "ATT-5KG-01",
          minStockAlert = 8.0
        ),
        ProductEntity(
          storeId = 1,
          name = "Fortune Sunlite Sunflower Oil 1L",
          category = "Edible Oils",
          costPrice = 120.0,
          sellingPrice = 138.0,
          stockQuantity = 24.0,
          unit = "packet",
          sku = "OIL-SUN-01",
          minStockAlert = 6.0
        ),
        ProductEntity(
          storeId = 1,
          name = "Tata Salt Vaccum Evaporated 1kg",
          category = "Spices & Essentials",
          costPrice = 22.0,
          sellingPrice = 28.0,
          stockQuantity = 55.0,
          unit = "packet",
          sku = "SLT-TAT-01",
          minStockAlert = 15.0
        ),
        ProductEntity(
          storeId = 1,
          name = "India Gate Basmati Rice 1kg",
          category = "Rice & Grains",
          costPrice = 88.0,
          sellingPrice = 110.0,
          stockQuantity = 32.0,
          unit = "packet",
          sku = "RIC-BAS-01",
          minStockAlert = 10.0
        ),
        ProductEntity(
          storeId = 1,
          name = "Tata Sampann Toor Dal Unpolished 1kg",
          category = "Pulses & Dal",
          costPrice = 145.0,
          sellingPrice = 172.0,
          stockQuantity = 19.0,
          unit = "packet",
          sku = "DAL-TOR-01",
          minStockAlert = 5.0
        ),
        ProductEntity(
          storeId = 1,
          name = "Maggi 2-Minute Masala Noodles 280g",
          category = "Snacks & Instant",
          costPrice = 45.0,
          sellingPrice = 52.0,
          stockQuantity = 42.0,
          unit = "packet",
          sku = "MAG-NOO-01",
          minStockAlert = 10.0
        ),
        ProductEntity(
          storeId = 1,
          name = "Parle-G Original Glucose Biscuits 250g",
          category = "Biscuits & Bakery",
          costPrice = 22.0,
          sellingPrice = 25.0,
          stockQuantity = 60.0,
          unit = "packet",
          sku = "BIS-PAR-01",
          minStockAlert = 12.0
        ),
        ProductEntity(
          storeId = 1,
          name = "Amul Butter Pasteurized 100g",
          category = "Dairy & Milk",
          costPrice = 52.0,
          sellingPrice = 58.0,
          stockQuantity = 4.0, // Low stock trigger
          unit = "packet",
          sku = "DAI-BUT-01",
          minStockAlert = 8.0
        ),
        ProductEntity(
          storeId = 1,
          name = "Surf Excel Easy Wash Detergent 1kg",
          category = "Cleaning & Laundry",
          costPrice = 130.0,
          sellingPrice = 152.0,
          stockQuantity = 3.0, // Low stock trigger
          unit = "packet",
          sku = "CLN-DET-01",
          minStockAlert = 6.0
        ),
        ProductEntity(
          storeId = 1,
          name = "Brooke Bond Red Label Tea 250g",
          category = "Beverages & Tea",
          costPrice = 115.0,
          sellingPrice = 135.0,
          stockQuantity = 16.0,
          unit = "packet",
          sku = "BEV-TEA-01",
          minStockAlert = 5.0
        )
      )
      productDao.insertAll(products)

      // Seed Customers for Store 1
      val now = System.currentTimeMillis()
      val c1 = CustomerEntity(storeId = 1, name = "Rajesh Sharma", phone = "+91 98234 56789", address = "House #12, Shiv Nagar", totalDue = 1450.0, lastTransactionDate = now - 3600000L * 4)
      val c2 = CustomerEntity(storeId = 1, name = "Sunita Gupta", phone = "+91 94123 78901", address = "Flat 204, Om Sai Apt", totalDue = 620.0, lastTransactionDate = now - 3600000L * 24)
      val c3 = CustomerEntity(storeId = 1, name = "Amit Patel", phone = "+91 98987 65432", address = "Patel Vada, Station Rd", totalDue = 3180.0, lastTransactionDate = now - 3600000L * 48)

      val c1Id = customerDao.insertCustomer(c1)
      val c2Id = customerDao.insertCustomer(c2)
      val c3Id = customerDao.insertCustomer(c3)

      // Seed Khata entries for Store 1
      khataDao.insertEntry(KhataEntryEntity(storeId = 1, customerId = c1Id, type = "CREDIT_GAVE", amount = 1450.0, note = "Grocery items credit", timestamp = now - 3600000L * 4))
      khataDao.insertEntry(KhataEntryEntity(storeId = 1, customerId = c2Id, type = "CREDIT_GAVE", amount = 1120.0, note = "Monthly staples", timestamp = now - 3600000L * 30))
      khataDao.insertEntry(KhataEntryEntity(storeId = 1, customerId = c2Id, type = "PAYMENT_RECEIVED", amount = 500.0, note = "UPI payment received", timestamp = now - 3600000L * 24))

      // Seed Invoices for Store 1
      invoiceDao.insertInvoice(
        InvoiceEntity(
          storeId = 1,
          invoiceNumber = "INV-2026-001",
          customerId = c1Id,
          customerName = "Rajesh Sharma",
          customerPhone = "+91 98234 56789",
          subtotal = 628.0,
          taxAmount = 31.4,
          discountAmount = 20.0,
          grandTotal = 639.4,
          paymentMode = "UPI",
          itemsJson = """[{"productId":1,"productName":"Aashirvaad Shudh Chakki Atta 5kg","unitPrice":245.0,"quantity":2.0,"unit":"packet","total":490.0},{"productId":2,"productName":"Fortune Sunlite Sunflower Oil 1L","unitPrice":138.0,"quantity":1.0,"unit":"packet","total":138.0}]""",
          timestamp = now - 3600000L * 6
        )
      )

      // Seed Expenses for Store 1
      expenseDao.insertExpense(ExpenseEntity(storeId = 1, title = "Shop Rent (Current Month)", category = "Rent", amount = 12000.0, paymentMethod = "BANK", timestamp = now - 86400000L * 2, note = "Paid to landlord Mr. Joshi"))
      expenseDao.insertExpense(ExpenseEntity(storeId = 1, title = "Electricity Bill", category = "Electricity", amount = 2450.0, paymentMethod = "UPI", timestamp = now - 86400000L * 5, note = "State Power Corp bill"))
    }
  }
}
