package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val email: String,
  val phone: String,
  val password: String,
  val role: String = "MERCHANT", // "MERCHANT" or "ADMIN"
  val status: String = "ACTIVE", // "ACTIVE" or "SUSPENDED"
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stores")
data class StoreEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val userId: Long, // Owner user ID
  val storeName: String,
  val category: String, // "Kirana & Grocery", "Apparel", "Hardware", etc.
  val ownerName: String,
  val phone: String,
  val email: String = "",
  val address: String = "",
  val gstin: String = "",
  val upiId: String = "",
  val invoiceFooter: String = "Thank you for shopping with us! Visit again.",
  val subscriptionTier: String = "PRO", // "FREE", "PRO", "PREMIUM"
  val subscriptionExpiry: Long = System.currentTimeMillis() + (30L * 24 * 3600 * 1000),
  val status: String = "APPROVED", // "APPROVED", "PENDING", "REJECTED"
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class CustomerEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val storeId: Long = 1,
  val name: String,
  val phone: String,
  val address: String = "",
  val totalDue: Double = 0.0, // positive = customer owes us (Udhar), negative = advance
  val lastTransactionDate: Long = System.currentTimeMillis(),
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "khata_entries")
data class KhataEntryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val storeId: Long = 1,
  val customerId: Long,
  val type: String, // "CREDIT_GAVE" (Udhar diya) or "PAYMENT_RECEIVED" (Paisa mila)
  val amount: Double,
  val note: String = "",
  val invoiceId: Long? = null,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class ProductEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val storeId: Long = 1,
  val name: String,
  val category: String,
  val costPrice: Double,
  val sellingPrice: Double,
  val stockQuantity: Double,
  val unit: String = "pcs", // kg, g, pcs, packet, litre, box
  val sku: String = "",
  val barcode: String = "",
  val minStockAlert: Double = 5.0,
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val storeId: Long = 1,
  val invoiceNumber: String,
  val customerId: Long? = null,
  val customerName: String,
  val customerPhone: String = "",
  val subtotal: Double,
  val taxAmount: Double,
  val discountAmount: Double,
  val grandTotal: Double,
  val paymentMode: String, // "CASH", "UPI", "KHATA_CREDIT", "CARD"
  val itemsJson: String, // serialized JSON list of InvoiceCartItem
  val timestamp: Long = System.currentTimeMillis(),
  val status: String = "PAID" // PAID, DUE, PARTIAL
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val storeId: Long = 1,
  val title: String,
  val category: String, // Rent, Electricity, Staff Salary, Logistics, Packaging, Restock, Miscellaneous
  val amount: Double,
  val paymentMethod: String = "CASH", // CASH, UPI, BANK
  val timestamp: Long = System.currentTimeMillis(),
  val note: String = ""
)

@Entity(tableName = "business_profile")
data class BusinessProfileEntity(
  @PrimaryKey val id: Int = 1,
  val shopName: String = "Shree Ganesh Kirana & General Store",
  val ownerName: String = "Ramesh Kumar",
  val phone: String = "+91 98765 43210",
  val email: String = "ramesh.store@dukaanpro.in",
  val address: String = "Shop #4, Market Complex, Station Road",
  val gstin: String = "27AAAAA0000A1Z5",
  val upiId: String = "rameshkirana@okaxis",
  val invoiceFooter: String = "Thank you for shopping with us! Visit again.",
  val currencySymbol: String = "₹",
  val subscriptionTier: String = "PRO", // FREE, PRO, PREMIUM
  val subscriptionExpiry: Long = System.currentTimeMillis() + (30L * 24 * 3600 * 1000)
)

// Non-entity domain transfer object for Cart & Invoice line items
data class InvoiceCartItem(
  val productId: Long,
  val productName: String,
  val unitPrice: Double,
  val quantity: Double,
  val unit: String,
  val total: Double = unitPrice * quantity
)
