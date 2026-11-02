package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.StoreEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.DukaanViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
  viewModel: DukaanViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val allStores by viewModel.allStoresForAdmin.collectAsState()
  val allUsers by viewModel.allUsersForAdmin.collectAsState()

  var adminTab by remember { mutableStateOf(0) } // 0 = Stores, 1 = Merchants/Users
  var storeSearchQuery by remember { mutableStateOf("") }

  val filteredStores = remember(allStores, storeSearchQuery) {
    if (storeSearchQuery.isBlank()) allStores
    else allStores.filter {
      it.storeName.contains(storeSearchQuery, ignoreCase = true) ||
      it.ownerName.contains(storeSearchQuery, ignoreCase = true) ||
      it.phone.contains(storeSearchQuery, ignoreCase = true)
    }
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .testTag("admin_panel_screen"),
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(SlateSecondary), contentAlignment = Alignment.Center) {
              Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Platform Admin Panel", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
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
    ) {
      // Platform Stats Header Cards
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = EmeraldContainer),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text("TOTAL STORES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnEmeraldContainer)
            Text("${allStores.size}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
          }
        }

        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = SlateContainer),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text("MERCHANTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnSlateContainer)
            Text("${allUsers.count { it.role == "MERCHANT" }}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = SlateSecondary)
          }
        }

        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = GoldContainer),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text("PRO / VIP TIERS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnGoldContainer)
            Text("${allStores.count { it.subscriptionTier != "FREE" }}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = GoldAccent)
          }
        }
      }

      // Switch Tab: Stores vs Users
      PrimaryTabRow(
        selectedTabIndex = adminTab,
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        Tab(
          selected = adminTab == 0,
          onClick = { adminTab = 0 },
          text = { Text("Registered Stores (${allStores.size})", fontWeight = FontWeight.Bold) },
          icon = { Icon(Icons.Filled.Store, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
        Tab(
          selected = adminTab == 1,
          onClick = { adminTab = 1 },
          text = { Text("Merchants & Users (${allUsers.size})", fontWeight = FontWeight.Bold) },
          icon = { Icon(Icons.Filled.People, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
      }

      if (adminTab == 0) {
        // ---------------- STORES LIST & CONTROLS ----------------
        OutlinedTextField(
          value = storeSearchQuery,
          onValueChange = { storeSearchQuery = it },
          placeholder = { Text("Filter stores by name, owner, phone...") },
          leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          singleLine = true,
          shape = RoundedCornerShape(10.dp)
        )

        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredStores, key = { it.id }) { store ->
            AdminStoreCard(
              store = store,
              onToggleStatus = { newStatus ->
                viewModel.adminUpdateStoreStatus(store.id, newStatus)
                Toast.makeText(context, "${store.storeName} status updated to $newStatus", Toast.LENGTH_SHORT).show()
              },
              onChangeTier = { newTier ->
                viewModel.adminUpdateStoreTier(store.id, newTier)
                Toast.makeText(context, "${store.storeName} tier upgraded to $newTier", Toast.LENGTH_SHORT).show()
              },
              onInspectAsMerchant = {
                viewModel.selectStore(store)
                viewModel.navigateTo(AppScreen.DASHBOARD)
                Toast.makeText(context, "Switched to ${store.storeName}", Toast.LENGTH_SHORT).show()
              }
            )
          }
        }
      } else {
        // ---------------- USERS LIST ----------------
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(allUsers, key = { it.id }) { user ->
            AdminUserCard(
              user = user,
              onToggleStatus = {
                viewModel.adminToggleUserStatus(user.id, user.status)
                Toast.makeText(context, "User status toggled", Toast.LENGTH_SHORT).show()
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun AdminStoreCard(
  store: StoreEntity,
  onToggleStatus: (String) -> Unit,
  onChangeTier: (String) -> Unit,
  onInspectAsMerchant: () -> Unit
) {
  val isApproved = store.status == "APPROVED"
  val isSuspended = store.status == "SUSPENDED"

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(store.storeName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Text("Owner: ${store.ownerName} • ${store.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("Category: ${store.category} • ${store.address}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Status Badge
        Surface(
          color = when {
            isSuspended -> CrimsonContainer
            isApproved -> EmeraldContainer
            else -> GoldContainer
          },
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = store.status,
            color = when {
              isSuspended -> OnCrimsonContainer
              isApproved -> OnEmeraldContainer
              else -> OnGoldContainer
            },
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(color = GoldContainer, shape = RoundedCornerShape(6.dp)) {
          Text(
            text = "Plan: ${store.subscriptionTier}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = OnGoldContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          // Approve / Suspend Toggle
          OutlinedButton(
            onClick = {
              onToggleStatus(if (isApproved) "SUSPENDED" else "APPROVED")
            },
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(32.dp)
          ) {
            Text(if (isApproved) "Suspend" else "Approve", fontSize = 11.sp)
          }

          // Tier Upgrade shortcut
          OutlinedButton(
            onClick = {
              onChangeTier(if (store.subscriptionTier == "PREMIUM") "PRO" else "PREMIUM")
            },
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(32.dp)
          ) {
            Text(if (store.subscriptionTier == "PREMIUM") "Set Pro" else "Set VIP", fontSize = 11.sp)
          }

          // Open as Store
          Button(
            onClick = onInspectAsMerchant,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(32.dp)
          ) {
            Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun AdminUserCard(
  user: UserEntity,
  onToggleStatus: () -> Unit
) {
  val isActive = user.status == "ACTIVE"

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(if (user.role == "ADMIN") SlateSecondary else EmeraldContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          if (user.role == "ADMIN") Icons.Filled.Shield else Icons.Filled.Person,
          contentDescription = null,
          tint = if (user.role == "ADMIN") Color.White else OnEmeraldContainer,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            color = if (user.role == "ADMIN") SlateContainer else EmeraldContainer,
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              user.role,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = if (user.role == "ADMIN") OnSlateContainer else OnEmeraldContainer,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
          }
        }
        Text("${user.email} • ${user.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      OutlinedButton(
        onClick = onToggleStatus,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = if (isActive) CrimsonDebt else MintReceived
        ),
        modifier = Modifier.height(34.dp)
      ) {
        Text(if (isActive) "Suspend" else "Activate", fontSize = 11.sp)
      }
    }
  }
}
