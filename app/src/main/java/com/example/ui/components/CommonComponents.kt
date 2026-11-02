package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import java.util.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DukaanProTopBar(
  shopName: String,
  subscriptionTier: String,
  currentScreen: AppScreen,
  isAdmin: Boolean = false,
  onOpenProfile: () -> Unit,
  onOpenNotifications: () -> Unit,
  onOpenAdminPanel: (() -> Unit)? = null,
  onOpenStoreManager: (() -> Unit)? = null,
  onLogout: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  TopAppBar(
    title = {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = shopName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            color = if (subscriptionTier == "PREMIUM") GoldContainer else EmeraldContainer,
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = subscriptionTier,
              color = if (subscriptionTier == "PREMIUM") OnGoldContainer else OnEmeraldContainer,
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
        Text(
          text = currentScreen.title,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    actions = {
      if (isAdmin && onOpenAdminPanel != null) {
        IconButton(
          onClick = onOpenAdminPanel,
          modifier = Modifier.testTag("topbar_admin_button")
        ) {
          Icon(Icons.Filled.AdminPanelSettings, contentDescription = "Admin Panel", tint = SlateSecondary)
        }
      }

      if (onOpenStoreManager != null) {
        IconButton(
          onClick = onOpenStoreManager,
          modifier = Modifier.testTag("topbar_store_switcher")
        ) {
          Icon(Icons.Filled.Store, contentDescription = "Switch / Add Store")
        }
      }

      IconButton(
        onClick = onOpenNotifications,
        modifier = Modifier.testTag("topbar_notifications_button")
      ) {
        BadgedBox(
          badge = {
            Badge(containerColor = CrimsonDebt) {
              Text("2", color = Color.White)
            }
          }
        ) {
          Icon(Icons.Outlined.Notifications, contentDescription = "Stock & Payment Alerts")
        }
      }

      Box {
        IconButton(
          onClick = { showMenu = true },
          modifier = Modifier.testTag("topbar_overflow_menu")
        ) {
          Icon(Icons.Filled.MoreVert, contentDescription = "More Options")
        }

        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false }
        ) {
          DropdownMenuItem(
            text = { Text("Shop Profile & Settings") },
            leadingIcon = { Icon(Icons.Filled.Storefront, contentDescription = null) },
            onClick = {
              showMenu = false
              onOpenProfile()
            }
          )
          if (isAdmin && onOpenAdminPanel != null) {
            DropdownMenuItem(
              text = { Text("Admin Panel") },
              leadingIcon = { Icon(Icons.Filled.AdminPanelSettings, contentDescription = null) },
              onClick = {
                showMenu = false
                onOpenAdminPanel()
              }
            )
          }
          if (onOpenStoreManager != null) {
            DropdownMenuItem(
              text = { Text("Switch / Add Store") },
              leadingIcon = { Icon(Icons.Filled.AddBusiness, contentDescription = null) },
              onClick = {
                showMenu = false
                onOpenStoreManager()
              }
            )
          }
          HorizontalDivider()
          DropdownMenuItem(
            text = { Text("Sign Out", color = CrimsonDebt, fontWeight = FontWeight.Bold) },
            leadingIcon = { Icon(Icons.Filled.Logout, contentDescription = null, tint = CrimsonDebt) },
            onClick = {
              showMenu = false
              onLogout()
            }
          )
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface,
      titleContentColor = MaterialTheme.colorScheme.onSurface
    )
  )
}

@Composable
fun DukaanProBottomNav(
  currentScreen: AppScreen,
  cartItemCount: Int,
  onNavigate: (AppScreen) -> Unit
) {
  NavigationBar(
    modifier = Modifier
      .windowInsetsPadding(WindowInsets.navigationBars)
      .testTag("main_bottom_nav"),
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 6.dp
  ) {
    NavigationBarItem(
      selected = currentScreen == AppScreen.DASHBOARD,
      onClick = { onNavigate(AppScreen.DASHBOARD) },
      icon = {
        Icon(
          if (currentScreen == AppScreen.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
          contentDescription = "Dashboard"
        )
      },
      label = { Text("Home", fontSize = 11.sp) },
      modifier = Modifier.testTag("nav_dashboard")
    )

    NavigationBarItem(
      selected = currentScreen == AppScreen.KHATA,
      onClick = { onNavigate(AppScreen.KHATA) },
      icon = {
        Icon(
          if (currentScreen == AppScreen.KHATA) Icons.Filled.Book else Icons.Outlined.Book,
          contentDescription = "Digital Khata"
        )
      },
      label = { Text("Khata", fontSize = 11.sp) },
      modifier = Modifier.testTag("nav_khata")
    )

    NavigationBarItem(
      selected = currentScreen == AppScreen.BILLING,
      onClick = { onNavigate(AppScreen.BILLING) },
      icon = {
        if (cartItemCount > 0) {
          BadgedBox(
            badge = {
              Badge(containerColor = EmeraldPrimary) {
                Text("$cartItemCount", color = Color.White)
              }
            }
          ) {
            Icon(Icons.Filled.PointOfSale, contentDescription = "POS Billing")
          }
        } else {
          Icon(
            if (currentScreen == AppScreen.BILLING) Icons.Filled.PointOfSale else Icons.Outlined.PointOfSale,
            contentDescription = "POS Billing"
          )
        }
      },
      label = { Text("Billing", fontSize = 11.sp) },
      modifier = Modifier.testTag("nav_billing")
    )

    NavigationBarItem(
      selected = currentScreen == AppScreen.INVENTORY,
      onClick = { onNavigate(AppScreen.INVENTORY) },
      icon = {
        Icon(
          if (currentScreen == AppScreen.INVENTORY) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
          contentDescription = "Inventory Stock"
        )
      },
      label = { Text("Inventory", fontSize = 11.sp) },
      modifier = Modifier.testTag("nav_inventory")
    )

    NavigationBarItem(
      selected = currentScreen in listOf(AppScreen.EXPENSES, AppScreen.REPORTS, AppScreen.SUBSCRIPTION, AppScreen.PROFILE),
      onClick = {
        // Toggle or open reports / menu
        onNavigate(if (currentScreen == AppScreen.REPORTS) AppScreen.EXPENSES else AppScreen.REPORTS)
      },
      icon = {
        Icon(
          if (currentScreen == AppScreen.REPORTS) Icons.Filled.Analytics else Icons.Outlined.Analytics,
          contentDescription = "Business Reports"
        )
      },
      label = { Text("Reports", fontSize = 11.sp) },
      modifier = Modifier.testTag("nav_reports")
    )
  }
}

@Composable
fun MetricCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  iconTint: Color,
  containerColor: Color,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  Card(
    modifier = modifier
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(iconTint.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        if (onClick != null) {
          Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun DynamicQrCode(
  upiUri: String,
  modifier: Modifier = Modifier,
  sizeDp: Int = 180
) {
  // Draw an authentic-looking QR matrix with position markers
  Box(
    modifier = modifier
      .size(sizeDp.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(Color.White)
      .padding(12.dp),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val cols = 21
      val cellSize = w / cols

      val paintDark = Color(0xFF0F172A)
      val paintLight = Color.White

      // Draw finder patterns (top-left, top-right, bottom-left)
      fun drawFinder(startX: Float, startY: Float) {
        // Outer 7x7
        drawRect(paintDark, Offset(startX, startY), Size(cellSize * 7, cellSize * 7))
        // Inner 5x5 white
        drawRect(paintLight, Offset(startX + cellSize, startY + cellSize), Size(cellSize * 5, cellSize * 5))
        // Core 3x3 dark
        drawRect(paintDark, Offset(startX + cellSize * 2, startY + cellSize * 2), Size(cellSize * 3, cellSize * 3))
      }

      drawFinder(0f, 0f)
      drawFinder(cellSize * 14, 0f)
      drawFinder(0f, cellSize * 14)

      // Deterministic pseudo-random fill based on hash of upiUri
      val random = Random(upiUri.hashCode().toLong())
      for (r in 0 until cols) {
        for (c in 0 until cols) {
          val inFinderTL = r < 7 && c < 7
          val inFinderTR = r < 7 && c >= 14
          val inFinderBL = r >= 14 && c < 7
          val inCenterBadge = r in 8..12 && c in 8..12

          if (!inFinderTL && !inFinderTR && !inFinderBL && !inCenterBadge) {
            if (random.nextBoolean()) {
              drawRect(
                paintDark,
                Offset(c * cellSize, r * cellSize),
                Size(cellSize, cellSize)
              )
            }
          }
        }
      }

      // Center logo background circle
      val center = Offset(w / 2f, h / 2f)
      drawCircle(Color(0xFF0D5C3A), radius = cellSize * 2.4f, center = center)
      drawCircle(Color.White, radius = cellSize * 2.0f, center = center)
      drawCircle(Color(0xFF0D5C3A), radius = cellSize * 1.6f, center = center)
    }

    // Rupee symbol inside QR
    Text(
      text = "₹",
      color = Color.White,
      fontWeight = FontWeight.Bold,
      fontSize = 18.sp
    )
  }
}
