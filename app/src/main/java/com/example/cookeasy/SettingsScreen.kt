package com.example.cookeasy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(recipeDao: RecipeDao, onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("DropwisePrefs", Context.MODE_PRIVATE)
    var showPrivacySecurity by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // State for Help and Support form
    var email by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    // Define colors
    val accentColor = Color(0xFFFF7043) // Orange accent

    fun clearAppData() {
        CoroutineScope(Dispatchers.IO).launch {
            // Clear Room database
            recipeDao.deleteAllRecipes()

            // Clear SharedPreferences
            val prefsEditor = prefs.edit()
            prefsEditor.clear()
            prefsEditor.apply()

            // Close the app
            withContext(Dispatchers.Main) {
                (context as? Activity)?.finishAffinity() // Closes all activities and exits the app
            }
        }
    }

    fun sendSupportEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@example.com")) // Replace with your support email
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, "From: $email\n\n$message")
        }
        context.startActivity(Intent.createChooser(intent, "Send Email"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFF0F0F0)
                            )
                        )
                    )
            ) {
                SettingsItem(
                    icon = Icons.Outlined.Lock,
                    title = "Privacy & Security",
                    onClick = { showPrivacySecurity = !showPrivacySecurity },
                    accentColor = accentColor
                )
                AnimatedVisibility(
                    visible = showPrivacySecurity,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    LazyColumn {
                        item {
                            SettingsSubItem(
                                icon = Icons.Outlined.Delete,
                                title = "Clear App Data",
                                onClick = { showClearDataDialog = true },
                                accentColor = accentColor
                            )
                        }
                        item {
                            SettingsSubItem(
                                icon = Icons.Outlined.Info,
                                title = "Privacy Policy",
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://privacy-policy-drop-wise.vercel.app/")
                                    }
                                    context.startActivity(intent)
                                },
                                accentColor = accentColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                SettingsItem(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Help and Support",
                    onClick = { showHelpDialog = true },
                    accentColor = accentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "About",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://lahoucines.gitbook.io/dropwise/~/changes/1"))
                        context.startActivity(intent)
                    },
                    accentColor = accentColor
                )
            }
        }
    )

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear App Data", color = Color.Black) },
            text = { Text("Are you sure you want to clear all app data? This action will delete all saved recipes and close the app.", color = Color.Black) },
            confirmButton = {
                Button(
                    onClick = {
                        clearAppData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Clear", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showClearDataDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Cancel", color = Color.Black)
                }
            },
            containerColor = Color.White
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Help and Support", color = Color.Black) },
            text = {
                Column {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Your Email", color = Color.Black) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.Black,
                            focusedLabelColor = accentColor,
                            cursorColor = accentColor
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject", color = Color.Black) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.Black,
                            focusedLabelColor = accentColor,
                            cursorColor = accentColor
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message", color = Color.Black) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.Black,
                            focusedLabelColor = accentColor,
                            cursorColor = accentColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        sendSupportEmail()
                        showHelpDialog = false
                        email = ""
                        subject = ""
                        message = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    enabled = email.isNotEmpty() && subject.isNotEmpty() && message.isNotEmpty()
                ) {
                    Text("Send", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showHelpDialog = false
                        email = ""
                        subject = ""
                        message = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Cancel", color = Color.Black)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "Arrow",
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SettingsSubItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    accentColor: Color,
    content: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF0F0F0))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(accentColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        content()
        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = "Arrow",
            tint = accentColor,
            modifier = Modifier.size(14.dp)
        )
    }
}