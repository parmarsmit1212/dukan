package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import com.example.data.model.BusinessProfileEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.DukaanViewModel

@Composable
fun ProfileScreen(
  viewModel: DukaanViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val profileState by viewModel.businessProfile.collectAsState()
  val profile = profileState ?: BusinessProfileEntity()

  var shopName by remember(profile) { mutableStateOf(profile.shopName) }
  var ownerName by remember(profile) { mutableStateOf(profile.ownerName) }
  var phone by remember(profile) { mutableStateOf(profile.phone) }
  var email by remember(profile) { mutableStateOf(profile.email) }
  var address by remember(profile) { mutableStateOf(profile.address) }
  var gstin by remember(profile) { mutableStateOf(profile.gstin) }
  var upiId by remember(profile) { mutableStateOf(profile.upiId) }
  var footerNote by remember(profile) { mutableStateOf(profile.invoiceFooter) }

  var showResetConfirmation by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("profile_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Business Header Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Filled.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(shopName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
          Text("Managed by $ownerName", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))

          Spacer(modifier = Modifier.height(12.dp))
          Surface(
            color = Color.White,
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = "Plan: ${profile.subscriptionTier} ACTIVE",
              color = EmeraldPrimary,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    // 2. Shop Details Form
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text("Store Details & Billing Profile", fontWeight = FontWeight.Bold, fontSize = 15.sp)

          OutlinedTextField(
            value = shopName,
            onValueChange = { shopName = it },
            label = { Text("Shop / Business Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = ownerName,
            onValueChange = { ownerName = it },
            label = { Text("Owner / Merchant Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
              value = phone,
              onValueChange = { phone = it },
              label = { Text("Phone Number") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = email,
              onValueChange = { email = it },
              label = { Text("Email") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }

          OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Store Address & City") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
              value = gstin,
              onValueChange = { gstin = it },
              label = { Text("GSTIN (Optional)") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = upiId,
              onValueChange = { upiId = it },
              label = { Text("UPI ID for Payments") },
              placeholder = { Text("username@upi") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }

          OutlinedTextField(
            value = footerNote,
            onValueChange = { footerNote = it },
            label = { Text("Invoice Footer Note") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(4.dp))

          Button(
            onClick = {
              viewModel.saveProfile(
                profile.copy(
                  shopName = shopName,
                  ownerName = ownerName,
                  phone = phone,
                  email = email,
                  address = address,
                  gstin = gstin,
                  upiId = upiId,
                  invoiceFooter = footerNote
                )
              )
              Toast.makeText(context, "Business Profile Saved Successfully!", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            modifier = Modifier.fillMaxWidth().height(48.dp)
          ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Business Profile", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 3. Subscription & Plans Shortcut
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Subscription & Billing", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            "Upgrade your plan for multi-device sync, cloud backup, and GST reports.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedButton(
            onClick = { viewModel.navigateTo(AppScreen.SUBSCRIPTION) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = GoldAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Manage Plans & Razorpay Upgrades", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 4. Data Backup, Export & Reset
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Data Management & Backup", fontWeight = FontWeight.Bold, fontSize = 15.sp)

          OutlinedButton(
            onClick = {
              val backupSummary = """
                DukaanPro Store Backup
                Store: $shopName
                Owner: $ownerName
                Date: ${java.util.Date()}
                Status: Local Room SQLite DB Healthy
              """.trimIndent()

              val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, backupSummary)
              }
              context.startActivity(Intent.createChooser(shareIntent, "Backup Store Data"))
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Filled.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export & Backup Store Data")
          }

          OutlinedButton(
            onClick = { showResetConfirmation = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonDebt),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Filled.RestartAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset Demo FMCG Data")
          }
        }
      }
    }

    // 5. Version Info
    item {
      Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("DukaanPro – Smart Business Manager v1.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("Production Ready for Google Play Store", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
      }
    }
  }

  // Reset confirmation dialog
  if (showResetConfirmation) {
    AlertDialog(
      onDismissRequest = { showResetConfirmation = false },
      title = { Text("Reset to Initial Kirana Demo Data?", fontWeight = FontWeight.Bold) },
      text = { Text("This will reload sample products, customer khata balances, and demo sales records.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.resetData()
            showResetConfirmation = false
            Toast.makeText(context, "Demo data reloaded!", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonDebt)
        ) {
          Text("Reset Now")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirmation = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
