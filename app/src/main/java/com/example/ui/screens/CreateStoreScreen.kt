package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.StoreEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.DukaanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoreScreen(
  viewModel: DukaanViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val currentUser by viewModel.currentUser.collectAsState()
  val userStores by viewModel.userStores.collectAsState()

  var storeName by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Kirana & Grocery") }
  var ownerName by remember(currentUser) { mutableStateOf(currentUser?.name ?: "") }
  var phone by remember(currentUser) { mutableStateOf(currentUser?.phone ?: "") }
  var address by remember { mutableStateOf("") }
  var gstin by remember { mutableStateOf("") }
  var upiId by remember { mutableStateOf("") }

  val categories = listOf(
    "Kirana & Grocery", "Garments & Apparel", "Electronics & Mobile",
    "Hardware & Electricals", "Pharmacy & Wellness", "General Retail Store"
  )

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .testTag("create_store_screen"),
    topBar = {
      TopAppBar(
        title = {
          Text(
            if (userStores.isEmpty()) "Launch Your Store" else "My Stores & Add Store",
            fontWeight = FontWeight.Bold
          )
        },
        actions = {
          IconButton(onClick = { viewModel.logout() }) {
            Icon(Icons.Filled.Logout, contentDescription = "Logout")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // If user already has stores, let them select or create a new one
      if (userStores.isNotEmpty()) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Your Existing Stores", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))
            userStores.forEach { store ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(store.storeName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  Text("${store.category} • ${store.address}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                  onClick = { viewModel.selectStore(store) },
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                  Text("Open Store")
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
      }

      // Store Creation Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(EmeraldContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Filled.AddBusiness, contentDescription = null, tint = OnEmeraldContainer, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text("Register New Store / Dukaan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
              Text("Enter your shop information to start billing", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          OutlinedTextField(
            value = storeName,
            onValueChange = { storeName = it },
            label = { Text("Store / Shop Name *") },
            placeholder = { Text("e.g. Mahalaxmi Supermarket") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_create_store_name")
          )

          // Category Chips
          Text("Business Category *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
              FilterChip(
                selected = category == cat,
                onClick = { category = cat },
                label = { Text(cat, fontSize = 12.sp) }
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
              value = ownerName,
              onValueChange = { ownerName = it },
              label = { Text("Owner Name *") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = phone,
              onValueChange = { phone = it },
              label = { Text("Shop Mobile *") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }

          OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Shop Address & City *") },
            placeholder = { Text("Shop #12, Gandhi Market, Main Road") },
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
              placeholder = { Text("store@upi") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Button(
            onClick = {
              if (storeName.isNotBlank() && phone.isNotBlank()) {
                viewModel.createStore(
                  storeName = storeName,
                  category = category,
                  ownerName = ownerName,
                  phone = phone,
                  address = address,
                  gstin = gstin,
                  upiId = upiId.ifBlank { "${storeName.lowercase().replace(" ", "")}@okaxis" },
                  onSuccess = {
                    Toast.makeText(context, "🎉 $storeName created successfully!", Toast.LENGTH_SHORT).show()
                  }
                )
              }
            },
            enabled = storeName.isNotBlank() && phone.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("btn_launch_store")
          ) {
            Icon(Icons.Filled.Store, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("LAUNCH & ACTIVATE STORE", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
          }
        }
      }
    }
  }
}
