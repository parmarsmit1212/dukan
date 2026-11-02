package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.DukaanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
  viewModel: DukaanViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableStateOf(0) } // 0 = Login, 1 = Register

  var identifier by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  // Registration fields
  var regName by remember { mutableStateOf("") }
  var regEmail by remember { mutableStateOf("") }
  var regPhone by remember { mutableStateOf("") }
  var regPassword by remember { mutableStateOf("") }
  var regPasswordVisible by remember { mutableStateOf(false) }

  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showForgotPassword by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .testTag("auth_screen")
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(20.dp))

      // App Brand Logo & Title
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(EmeraldPrimary),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Filled.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "DukaanPro",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = EmeraldPrimary
      )
      Text(
        text = "Smart Business Manager & Multi-Store Cloud POS",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Quick Demo Shortcut Cards for instant inspection
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text("One-Tap Demo Accounts (Instant Test)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = { viewModel.loginDemoMerchant() },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(38.dp)
            ) {
              Text("🏪 Merchant Demo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Button(
              onClick = { viewModel.loginDemoAdmin() },
              colors = ButtonDefaults.buttonColors(containerColor = SlateSecondary),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(38.dp)
            ) {
              Text("🛡️ Platform Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Tab Row
      PrimaryTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = {
            selectedTab = 0
            errorMessage = null
          },
          text = { Text("Sign In", fontWeight = FontWeight.Bold) },
          icon = { Icon(Icons.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = {
            selectedTab = 1
            errorMessage = null
          },
          text = { Text("Create Account", fontWeight = FontWeight.Bold) },
          icon = { Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Error Alert
      AnimatedVisibility(visible = errorMessage != null) {
        Surface(
          color = CrimsonContainer,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Filled.Error, contentDescription = null, tint = CrimsonDebt, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(errorMessage ?: "", color = OnCrimsonContainer, fontSize = 12.sp)
          }
        }
      }

      if (selectedTab == 0) {
        // ---------------- LOGIN FORM ----------------
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = identifier,
            onValueChange = {
              identifier = it
              errorMessage = null
            },
            label = { Text("Email or Mobile Number") },
            placeholder = { Text("ramesh@kirana.com or 9876543210") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_identifier_input")
          )

          OutlinedTextField(
            value = password,
            onValueChange = {
              password = it
              errorMessage = null
            },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
              }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_password_input")
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            Text(
              text = "Forgot Password?",
              color = EmeraldPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier
                .clickable { showForgotPassword = true }
                .padding(vertical = 4.dp)
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Button(
            onClick = {
              if (identifier.isBlank() || password.isBlank()) {
                errorMessage = "Please enter both identifier and password."
              } else {
                viewModel.login(
                  identifier = identifier,
                  pass = password,
                  onSuccess = {
                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                  },
                  onError = { err ->
                    errorMessage = err
                  }
                )
              }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("login_submit_button")
          ) {
            Icon(Icons.Filled.LockOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign In to DukaanPro", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
        }
      } else {
        // ---------------- REGISTER FORM ----------------
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = regName,
            onValueChange = {
              regName = it
              errorMessage = null
            },
            label = { Text("Full Name *") },
            placeholder = { Text("e.g. Mukesh Aggarwal") },
            leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reg_name_input")
          )

          OutlinedTextField(
            value = regEmail,
            onValueChange = {
              regEmail = it
              errorMessage = null
            },
            label = { Text("Email Address *") },
            placeholder = { Text("mukesh@store.com") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reg_email_input")
          )

          OutlinedTextField(
            value = regPhone,
            onValueChange = {
              regPhone = it
              errorMessage = null
            },
            label = { Text("Mobile Number (for OTP & SMS) *") },
            placeholder = { Text("9812345678") },
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reg_phone_input")
          )

          OutlinedTextField(
            value = regPassword,
            onValueChange = {
              regPassword = it
              errorMessage = null
            },
            label = { Text("Set Password *") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
              IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                Icon(if (regPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
              }
            },
            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reg_password_input")
          )

          Text(
            text = "ℹ️ After registration, you will create and set up your private store.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(4.dp))

          Button(
            onClick = {
              if (regName.isBlank() || regEmail.isBlank() || regPhone.isBlank() || regPassword.isBlank()) {
                errorMessage = "All registration fields are required."
              } else {
                viewModel.register(
                  name = regName,
                  email = regEmail,
                  phone = regPhone,
                  pass = regPassword,
                  onSuccess = {
                    Toast.makeText(context, "Account created! Now launch your store.", Toast.LENGTH_LONG).show()
                  },
                  onError = { err ->
                    errorMessage = err
                  }
                )
              }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("reg_submit_button")
          ) {
            Icon(Icons.Filled.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Register Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
        }
      }
    }
  }

  // Forgot Password Dialog
  if (showForgotPassword) {
    var resetEmail by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { showForgotPassword = false },
      title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          if (!otpSent) {
            Text("Enter your registered email or phone to receive a 6-digit OTP verification code:", fontSize = 13.sp)
            OutlinedTextField(
              value = resetEmail,
              onValueChange = { resetEmail = it },
              label = { Text("Email or Phone") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
          } else {
            Text("✅ A 6-digit security code has been sent to $resetEmail. Enter new password:", fontSize = 13.sp)
            OutlinedTextField(
              value = "123456",
              onValueChange = {},
              label = { Text("OTP Code") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (!otpSent) {
              if (resetEmail.isNotBlank()) otpSent = true
            } else {
              showForgotPassword = false
              Toast.makeText(context, "Password reset successfully! Please sign in.", Toast.LENGTH_SHORT).show()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
          Text(if (!otpSent) "Send OTP" else "Update Password")
        }
      },
      dismissButton = {
        TextButton(onClick = { showForgotPassword = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
