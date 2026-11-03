package com.pgstorm148.protapclub

import android.nfc.Tag
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pgstorm148.protapclub.ui.theme.ProtapClubTheme

class WriteSocialActivity : NfcBaseActivity() {
    private val statusState = mutableStateOf("Tap an NFC tag to write social media profile")

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProtapClubTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val status = remember { statusState }
                    var username by remember { mutableStateOf("") }
                    var selectedSocialMedia by remember { mutableStateOf(SocialMediaType.INSTAGRAM) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Write Social Media to Tag",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Logo
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Protap Club Logo",
                            modifier = Modifier.size(160.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))


                        Text(
                            "1. Choose Platform",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = 8.dp)
                        )

                        // Social Media Type Dropdown
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = selectedSocialMedia.displayName,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                SocialMediaType.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.displayName) },
                                        onClick = {
                                            selectedSocialMedia = option
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "2. Enter Username/Handle",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            placeholder = {
                                when (selectedSocialMedia) {
                                    SocialMediaType.TWITTER -> Text("e.g. elonmusk (without @)")
                                    SocialMediaType.INSTAGRAM -> Text("e.g. instagram")
                                    SocialMediaType.FACEBOOK -> Text("e.g. zuck")
                                    SocialMediaType.LINKEDIN -> Text("e.g. billgates")
                                    SocialMediaType.GITHUB -> Text("e.g. torvalds")
                                    SocialMediaType.TIKTOK -> Text("e.g. khaby.lame")
                                    SocialMediaType.YOUTUBE -> Text("e.g. @MrBeast")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            status.value,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                if (currentTag != null && username.isNotEmpty()) {
                                    val result = NfcUtils.writeSocialToTag(currentTag!!, selectedSocialMedia, username)
                                    updateStatus(result.message)
                                } else if (username.isEmpty()) {
                                    updateStatus("Please enter a username")
                                } else {
                                    updateStatus("Tap an NFC tag to write social media profile")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = currentTag != null && username.isNotEmpty()
                        ) {
                            Text("Write to Tag")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { finish() },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            Text("Back")
                        }
                    }
                }
            }
        }
    }

    override fun processTag(tag: Tag) {
        currentTag = tag
        updateStatus("Tag detected! You can now write your social media profile.")
    }

    private fun updateStatus(status: String) {
        statusState.value = status
    }
}