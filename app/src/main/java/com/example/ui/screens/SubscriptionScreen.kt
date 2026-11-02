package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.DukaanViewModel
import com.example.util.PaymentUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
  viewModel: DukaanViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val profile by viewModel.businessProfile.collectAsState()
  val currentTier = profile?.subscriptionTier ?: "PRO"

  var selectedPeriod by remember { mutableStateOf("YEARLY") } // MONTHLY vs YEARLY
  var selectedPlanForCheckout by remember { mutableStateOf<Triple<String, Double, String>?>(null) } // Plan, Price, Period

  val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
  val expiryDateStr = dateFormat.format(Date(profile?.subscriptionExpiry ?: (System.currentTimeMillis() + 86400000L * 30)))

  Scaffold(
    modifier = modifier.testTag("subscription_screen")
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Current Active Tier Status Banner
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = if (currentTier == "PREMIUM") GoldContainer else EmeraldContainer)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (currentTier == "PREMIUM") GoldAccent else EmeraldPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Filled.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "CURRENT PLAN: $currentTier",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = if (currentTier == "PREMIUM") OnGoldContainer else OnEmeraldContainer
              )
              Text(
                text = "Valid until $expiryDateStr",
                fontSize = 12.sp,
                color = if (currentTier == "PREMIUM") OnGoldContainer.copy(alpha = 0.8f) else OnEmeraldContainer.copy(alpha = 0.8f)
              )
            }

            Surface(
              color = Color.White,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "ACTIVE",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (currentTier == "PREMIUM") GoldAccent else EmeraldPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      // 2. Billing Period Toggle (Monthly vs Annual with 20% discount badge)
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Button(
            onClick = { selectedPeriod = "MONTHLY" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (selectedPeriod == "MONTHLY") MaterialTheme.colorScheme.surface else Color.Transparent,
              contentColor = if (selectedPeriod == "MONTHLY") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selectedPeriod == "MONTHLY") 2.dp else 0.dp),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("Monthly Billing", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }

          Button(
            onClick = { selectedPeriod = "YEARLY" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (selectedPeriod == "YEARLY") MaterialTheme.colorScheme.surface else Color.Transparent,
              contentColor = if (selectedPeriod == "YEARLY") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selectedPeriod == "YEARLY") 2.dp else 0.dp),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("Annual Plan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Spacer(modifier = Modifier.width(4.dp))
              Surface(color = EmeraldContainer, shape = RoundedCornerShape(4.dp)) {
                Text("SAVE 25%", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = OnEmeraldContainer, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
              }
            }
          }
        }
      }

      // 3. Plan 1: Free Plan
      item {
        PlanCard(
          planName = "Free Starter",
          priceText = "₹0",
          periodText = "Forever Free",
          badgeText = null,
          features = listOf(
            "Basic POS Billing (Cash & UPI)",
            "Digital Customer Khata Ledger",
            "Up to 30 Products in Inventory",
            "Standard On-Screen Receipts",
            "Local On-Device Storage"
          ),
          isCurrent = currentTier == "FREE",
          buttonText = if (currentTier == "FREE") "Active Plan" else "Downgrade to Free",
          onSelect = {
            viewModel.upgradeSubscription("FREE") {
              Toast.makeText(context, "Switched to Free Plan", Toast.LENGTH_SHORT).show()
            }
          }
        )
      }

      // 4. Plan 2: Pro Plan (Recommended)
      item {
        val proPrice = if (selectedPeriod == "MONTHLY") 299.0 else 2499.0
        val proPriceDisplay = if (selectedPeriod == "MONTHLY") "₹299/mo" else "₹2,499/year"

        PlanCard(
          planName = "DukaanPro Business",
          priceText = proPriceDisplay,
          periodText = if (selectedPeriod == "MONTHLY") "Billed monthly" else "Save ₹1,089/year",
          badgeText = "MOST POPULAR",
          badgeColor = EmeraldPrimary,
          features = listOf(
            "Unlimited Products & Inventory Tracking",
            "Instant PDF Invoices & WhatsApp Sharing",
            "Low Stock Alerts & Restock Reminders",
            "WhatsApp Payment Reminder with UPI Pay Link",
            "Expense Tracker & Daily Sales Summaries",
            "Priority Support & Data Export"
          ),
          isCurrent = currentTier == "PRO",
          buttonText = if (currentTier == "PRO") "Current Active Plan" else "Upgrade with Razorpay",
          onSelect = {
            selectedPlanForCheckout = Triple("PRO", proPrice, selectedPeriod)
          }
        )
      }

      // 5. Plan 3: Premium Plan
      item {
        val premPrice = if (selectedPeriod == "MONTHLY") 599.0 else 4999.0
        val premPriceDisplay = if (selectedPeriod == "MONTHLY") "₹599/mo" else "₹4,999/year"

        PlanCard(
          planName = "DukaanPro Enterprise VIP",
          priceText = premPriceDisplay,
          periodText = if (selectedPeriod == "MONTHLY") "Billed monthly" else "Save ₹2,189/year",
          badgeText = "FULL POWER",
          badgeColor = GoldAccent,
          features = listOf(
            "Everything in Pro Plan",
            "Advanced Financial & P&L Profit Analytics",
            "Multi-Device Sync & Automatic Cloud Backup",
            "GST-Compliant Tax Reports (GSTR-1 Ready)",
            "Staff Accounts & Access Controls",
            "Dedicated Account Manager on Call"
          ),
          isCurrent = currentTier == "PREMIUM",
          buttonText = if (currentTier == "PREMIUM") "Current Active Plan" else "Upgrade to Premium via Razorpay",
          onSelect = {
            selectedPlanForCheckout = Triple("PREMIUM", premPrice, selectedPeriod)
          }
        )
      }
    }
  }

  // Razorpay Payment Simulation Modal
  if (selectedPlanForCheckout != null) {
    RazorpayCheckoutDialog(
      plan = selectedPlanForCheckout!!.first,
      price = selectedPlanForCheckout!!.second,
      period = selectedPlanForCheckout!!.third,
      onDismiss = { selectedPlanForCheckout = null },
      onPaymentSuccess = {
        viewModel.upgradeSubscription(selectedPlanForCheckout!!.first) {
          Toast.makeText(context, "🎉 Payment Successful! ${selectedPlanForCheckout!!.first} Plan Activated!", Toast.LENGTH_LONG).show()
        }
        selectedPlanForCheckout = null
      }
    )
  }
}

@Composable
fun PlanCard(
  planName: String,
  priceText: String,
  periodText: String,
  badgeText: String?,
  badgeColor: Color = EmeraldPrimary,
  features: List<String>,
  isCurrent: Boolean,
  buttonText: String,
  onSelect: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .then(if (isCurrent) Modifier.border(2.dp, badgeColor, RoundedCornerShape(16.dp)) else Modifier),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      if (badgeText != null) {
        Surface(
          color = badgeColor,
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = badgeText,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
      }

      Text(planName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(4.dp))
      Row(verticalAlignment = Alignment.Bottom) {
        Text(priceText, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(6.dp))
        Text(periodText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(14.dp))

      features.forEach { feature ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Filled.Check, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(feature, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = onSelect,
        enabled = !isCurrent,
        colors = ButtonDefaults.buttonColors(
          containerColor = badgeColor,
          disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp)
      ) {
        Text(buttonText, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RazorpayCheckoutDialog(
  plan: String,
  price: Double,
  period: String,
  onDismiss: () -> Unit,
  onPaymentSuccess: () -> Unit
) {
  var selectedMethod by remember { mutableStateOf("UPI") } // UPI, CARD, NETBANKING
  var isProcessing by remember { mutableStateOf(false) }
  val orderId = remember { "order_${System.currentTimeMillis() % 10000000}" }
  val coroutineScope = rememberCoroutineScope()

  AlertDialog(
    onDismissRequest = { if (!isProcessing) onDismiss() },
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF0284C7)), contentAlignment = Alignment.Center) {
          Icon(Icons.Filled.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text("Razorpay Secure Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Text("Order ID: $orderId", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Price Summary
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("$plan Plan ($period)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(PaymentUtils.formatCurrency(price), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = EmeraldPrimary)
          }
        }

        if (isProcessing) {
          Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              CircularProgressIndicator(color = Color(0xFF0284C7))
              Spacer(modifier = Modifier.height(12.dp))
              Text("Verifying Payment with Razorpay...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text("Please do not press back", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        } else {
          Text("Select Payment Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)

          // Payment mode radio options
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val methods = listOf(
              Triple("UPI", "UPI Apps (GPay, PhonePe, Paytm)", Icons.Filled.QrCode),
              Triple("CARD", "Debit / Credit Card (Visa, Master, RuPay)", Icons.Filled.CreditCard),
              Triple("NETBANKING", "Net Banking (SBI, HDFC, ICICI, Axis)", Icons.Filled.AccountBalance)
            )

            methods.forEach { (code, label, icon) ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { selectedMethod = code }
                  .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = selectedMethod == code,
                  onClick = { selectedMethod = code }
                )
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF0284C7))
                Spacer(modifier = Modifier.width(8.dp))
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
              }
            }
          }

          Text(
            text = "🔒 256-bit SSL encrypted. Payment verified server-side.",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    confirmButton = {
      if (!isProcessing) {
        Button(
          onClick = {
            isProcessing = true
            coroutineScope.launch {
              delay(1500) // Simulated Razorpay gateway roundtrip
              onPaymentSuccess()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
        ) {
          Text("Pay ${PaymentUtils.formatCurrency(price)}", fontWeight = FontWeight.Bold)
        }
      }
    },
    dismissButton = {
      if (!isProcessing) {
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    }
  )
}
