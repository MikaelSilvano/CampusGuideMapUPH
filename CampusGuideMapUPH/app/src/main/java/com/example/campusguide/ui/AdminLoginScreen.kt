package com.example.campusguide.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.campusguide.R
import com.example.campusguide.data.AdminAuth
import com.example.campusguide.data.CredentialStore
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType

private val UPH_Navy  = Color(0xFF16224C)
private val UPH_White = Color(0xFFFFFFFF)

// di file yang sama (di bawah import), tambah:
private fun mapLoginError(t: Throwable): String {
    val raw = t.message?.lowercase().orEmpty()

    return when {
        t is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
            "Incorrect email or password."

        t is com.google.firebase.auth.FirebaseAuthInvalidUserException &&
                t.errorCode == "ERROR_USER_NOT_FOUND" ->
            "No account found for this email."

        t is com.google.firebase.auth.FirebaseAuthInvalidUserException &&
                t.errorCode == "ERROR_USER_DISABLED" ->
            "This account has been disabled. Please contact the administrator."

        raw.contains("invalid email") || raw.contains("error_invalid_email") ->
            "Please enter a valid email address."

        raw.contains("too many requests") || raw.contains("too-many-requests") ->
            "Too many attempts. Please try again later."

        t is com.google.firebase.FirebaseNetworkException ||
                raw.contains("network error") ->
            "Network error. Please check your internet connection and try again."

        raw.contains("operation not allowed") ->
            "Password sign-in is not enabled for this project."

        raw.contains("has expired") || raw.contains("malformed") ->
            "The supplied authentication credential is invalid or has expired."

        else ->
            "Authentication failed. Please try again."
    }
}

// Admin login screen
@Composable
fun AdminLoginScreen(onSuccess: () -> Unit) {
    val ctx = LocalContext.current
    val creds = remember { CredentialStore.get(ctx) }

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var remember by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var passVisible by remember { mutableStateOf(false) }

    // Jika pernah disimpan, maka isi password otomatis
    LaunchedEffect(Unit) {
        if (creds.isSaved()) {
            email = creds.email()
            pass = creds.password()
            remember = true
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            colors  = CardDefaults.cardColors(containerColor = UPH_Navy),
            shape   = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.uph_logo),
                    contentDescription = null,
                    modifier = Modifier.height(60.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text("Admin Login", style = MaterialTheme.typography.headlineSmall, color = UPH_White)
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email", color = UPH_White) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = UPH_White,
                        unfocusedIndicatorColor = UPH_White.copy(alpha = 0.5f),
                        focusedLabelColor       = UPH_White,
                        unfocusedLabelColor     = UPH_White.copy(alpha = 0.8f),
                        cursorColor             = UPH_White,
                        focusedTextColor        = UPH_White,
                        unfocusedTextColor      = UPH_White
                    )
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Password", color = UPH_White) },
                    singleLine = true,
                    visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passVisible = !passVisible }) {
                            val icon = if (passVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility
                            val desc = if (passVisible) "Hide password" else "Show password"
                            Icon(icon, contentDescription = desc, tint = UPH_White)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = UPH_White,
                        unfocusedIndicatorColor = UPH_White.copy(alpha = 0.5f),
                        focusedLabelColor       = UPH_White,
                        unfocusedLabelColor     = UPH_White.copy(alpha = 0.8f),
                        cursorColor             = UPH_White,
                        focusedTextColor        = UPH_White,
                        unfocusedTextColor      = UPH_White
                    )
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = remember,
                        onCheckedChange = { remember = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = UPH_White,
                            checkmarkColor = UPH_Navy,
                            uncheckedColor = UPH_White.copy(alpha = 0.7f)
                        )
                    )
                    Text("Remember me", color = UPH_White)
                }

                if (error != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(20.dp))

                // Tombol login: validasi input, loading screen, dan simpan kredensial (optional)
                Button(
                    onClick = {
                        if (email.isBlank() || pass.isBlank()) {
                            error = "Email & password required"; return@Button
                        }
                        loading = true; error = null
                        scope.launch {
                            val res = AdminAuth.signIn(email.trim(), pass)
                            loading = false
                            res.onSuccess {
                                if (remember) creds.save(email.trim(), pass) else creds.clear()
                                onSuccess()
                            }.onFailure { th ->
                                error = mapLoginError(th)
                            }
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UPH_White,
                        contentColor = UPH_Navy,
                        disabledContainerColor = UPH_White,
                        disabledContentColor = UPH_Navy.copy(alpha = 0.6f)
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                            color = UPH_Navy
                        )
                    } else {
                        Text("Login")
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
