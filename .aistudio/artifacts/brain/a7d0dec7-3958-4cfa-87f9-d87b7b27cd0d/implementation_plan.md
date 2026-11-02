# DukaanPro – Smart Business Manager

DukaanPro is a modern, production-grade business management and billing platform built natively for Android using Jetpack Compose and Material Design 3. Designed specifically for shopkeepers, retailers, FMCG merchants, and local businesses, it brings together POS billing, Digital Khata credit ledgers, inventory tracking, GST invoice generation, and financial analytics into a seamless, offline-first mobile app.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were confirmed based on your preferences:

- **Framework Stack**: Native Android with modern Kotlin, Jetpack Compose, Material Design 3, and Coroutines/Flows.
- **Default Retail Sector**: Kirana, Grocery, and General FMCG presets with standard Indian retail units (`kg`, `g`, `pack`, `pcs`, `box`, `litre`) and categories (Daily Essentials, Staples, Spices, Dairy, Snacks & Beverages, Personal Care).
- **Payment & Subscriptions**: Integrated Razorpay workflow supporting UPI Intent, Dynamic QR, Debit/Credit Cards, and Net Banking, alongside offline Cash recording and Khata balance settlement.
- **Data Persistence**: Offline-first Room Database with reactive Kotlin Flow repositories ensuring zero latency at the counter even without active internet, with ready hooks for cloud sync and data export/backup.

---

## 1. Overview & Core Concept

- **What It Does**: Provides small business owners with a pocket digital POS billing counter, automatic customer credit/debit (Khata) tracking, real-time stock and low-inventory alerts, GST/tax invoice generation with PDF creation and WhatsApp sharing, expense logging, and interactive financial profit/loss analytics.
- **Target Audience / Persona**: Kirana shop owners, grocery stores, local supermarkets, hardware dealers, and independent retailers who need speed at the cash counter, accurate customer ledger accounting, and fast payment collection.
- **Key Value**: Replaces paper bahi-khata and complex desktop POS software with a fluid, accessible, high-speed Android app that prevents inventory loss, eliminates credit reconciliation errors, and accelerates customer payments.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Interactive Dashboard**:
   - Quick KPI cards: Today's Sales, Cash in Hand, Total Customer Dues (Udhari), Low Stock Alerts, and Monthly Net Profit.
   - Quick action shortcuts: "New Bill / Quick Sale", "Add Khata Transaction", "Add Product", "Record Expense".
   - Animated weekly revenue and expense bar charts with interactive tap-to-inspect tooltips.
2. **Digital Khata Ledger (Udhar / Jama)**:
   - Searchable customer directory with balance tags: `You'll Give` (Green) vs. `You'll Get` (Red).
   - Customer Detail View with chronological statement, debit/credit entries, bill attachments, and one-tap "Send WhatsApp Payment Reminder" with pre-filled message and UPI pay link.
   - Quick transaction dialog for `Gave Money / Credit (Maine Diye)` and `Got Money / Debit (Mujhe Mile)`.
3. **Inventory & Stock Management**:
   - Categorized catalog with SKU, barcode/QR input, cost price, selling price, margin calculation, and current quantity.
   - Low-stock badge alert and automatic deduction upon completed invoices.
   - Stock adjustment tools (Restock, Return, Waste).
4. **Billing Counter & Invoice Generator**:
   - Fast item selector with search, category chips, and quantity stepper.
   - Automatic subtotal, discount, GST/tax breakdown (CGST/SGST/IGST), and change calculator.
   - Payment settlement: Cash, UPI Dynamic QR, or Add to Customer Khata (Credit).
   - On completion: Instant PDF invoice preview, printing, and WhatsApp sharing.
5. **Subscription Plans & Monetization**:
   - 3-tier matrix: **Free**, **Pro** (₹299/mo or ₹2,499/yr), and **Premium** (₹599/mo or ₹4,999/yr).
   - Live feature comparison, payment modal with Razorpay checkout simulation/integration, active status badge, and invoice download for subscriptions.
6. **Business Profile & Settings**:
   - Business details: Shop Name, Logo, GSTIN, Phone, Address, Invoice Footer Note, Theme switch (Light / Dark mode), and Data Export/Backup (JSON/CSV).

### Visual Identity & Theme
- **Theme Name**: *Merchant Emerald & Deep Slate*
- **Primary Accent**: Emerald Green (`#0D5C3A` & `#10B981`) — symbolizes prosperity, trusted commerce, and settled accounts.
- **Secondary Tone**: Deep Indigo Slate (`#1E293B` & `#0F172A`) — sleek, professional fintech surface foundation.
- **Status Accents**:
  - Dues / Alerts: Warm Amber (`#F59E0B`)
  - Debit / Outstandings: Crimson Coral (`#EF4444`)
  - Credits / Received: Vibrant Mint (`#10B981`)
- **Typography Hierarchy**:
  - Display / Headlines: Bold, clean sans-serif for numbers and currency (`₹`).
  - Body: Crisp, high-legibility sans-serif with distinct weights for tabular numbers and dates.
- **Motion & Micro-interactions**:
  - Spring-animated FABs, tab transitions, sliding bottom sheets for transaction entries, and celebratory animated checkmark on bill completion.

---

## 3. Key Product Decisions & Trade-Offs

- **Offline-First Room Architecture**:
  - *Chosen Approach*: Android Room SQLite database paired with StateFlow repositories.
  - *Why*: Shopkeepers cannot afford a 3-second network loading spinner while a customer is waiting at the counter. All writes and reads happen instantly locally, with JSON backup/restore and cloud sync readiness.
- **Native Android Vector & Print Document Rendering**:
  - *Chosen Approach*: Native Android `PdfDocument` and `PrintManager` generation.
  - *Why*: Works completely offline without remote headless browser APIs or heavy external web dependencies; produces crisp, printable 80mm thermal receipts or standard A4 invoices.
- **State Architecture**:
  - *Chosen Approach*: Clean MVVM with single `DukaanProViewModel` coordinating specialized domain repositories (CustomerKhataRepository, ProductRepository, BillingRepository, BusinessProfileRepository).

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                   DukaanPro Jetpack Compose UI                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌────────────┐  │
│  │  Dashboard   │  │    Khata     │  │  Inventory   │  │  Billing   │  │
│  │  & Reports   │  │  & Customers │  │ & Stock POS  │  │ & Invoices │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └─────┬──────┘  │
│         │                 │                 │                │         │
│         └─────────────────┼─────────────────┴────────────────┘         │
│                           ▼                                            │
│            DukaanProViewModel (StateFlow & M3 Events)                  │
└───────────────────────────┬────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   Domain Repositories & Utilities                      │
│  ┌───────────────────────┐  ┌───────────────────────────────────────┐  │
│  │ DukaanRepository      │  │ InvoicePdfGenerator & WhatsApp Share  │  │
│  └───────────┬───────────┘  └───────────────────────────────────────┘  │
│              │                                                         │
│              ▼                                                         │
│  ┌───────────────────────┐  ┌───────────────────────────────────────┐  │
│  │ Room Database         │  │ Razorpay & UPI Payment Engine         │  │
│  │ (Customers, Khata,    │  │ (Dynamic QR, Checkout Intent,         │  │
│  │  Products, Invoices,  │  │  Subscription Tier State)             │  │
│  │  Expenses, Profile)   │  └───────────────────────────────────────┘  │
│  └───────────────────────┘                                             │
└────────────────────────────────────────────────────────────────────────┘
```

### Core Data Entities & Schema
1. **`BusinessProfile`**: Shop name, owner name, mobile, address, GSTIN, UPI ID, logo uri, invoice terms.
2. **`Customer`**: ID, name, phone, address, totalDue, lastTransactionDate, createdAt.
3. **`KhataTransaction`**: ID, customerId, type (`CREDIT_GAVE` vs `PAYMENT_RECEIVED`), amount, date, note, billIdRef.
4. **`Product`**: ID, name, category, costPrice, sellingPrice, stockQuantity, unit, sku, minStockAlert.
5. **`Invoice`**: ID, invoiceNumber, customerId, customerName, items (JSON/embedded), subtotal, taxAmount, discountAmount, grandTotal, paymentMode (`CASH`, `UPI`, `CREDIT`), date, status.
6. **`Expense`**: ID, category, amount, paymentMethod, date, note.
7. **`Subscription`**: Plan (`FREE`, `PRO`, `PREMIUM`), activeUntil, transactionRef, status.
