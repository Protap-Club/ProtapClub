package com.pgstorm148.protapclub

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pgstorm148.protapclub.ui.theme.ProtapClubTheme
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.NotificationManager
import android.provider.Settings

class ReadTagActivity : NfcBaseActivity() {
    private val tagInfoState = mutableStateOf("Scan an NFC tag to see details")

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
                    val tagInfo = remember { tagInfoState }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Read NFC Tag",
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
                            "Hold your phone near an NFC tag to read its contents",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "Tag Information",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(tagInfo.value)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { finish() },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            Text("Back to Main Menu")
                        }
                    }
                }
            }
        }
    }

    override fun processTag(tag: Tag) {
        updateTagInfo(buildTagInfo(tag))

        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                val ndefMessage = ndef.ndefMessage
                if (ndefMessage != null) {
                    val tagInfo = parseNdefMessage(ndefMessage)
                    updateTagInfo(tagInfo)

                    // --- Automation logic ---
                    for (record in ndefMessage.records) {
                        val payload = record.payload
                        if (payload != null && payload.size > 1) {
                            // Skip language code (first byte)
                            val languageCodeLength = payload[0].toInt() and 0x3F
                            val text = String(payload, 1 + languageCodeLength, payload.size - 1 - languageCodeLength, Charsets.UTF_8)
                            Toast.makeText(this, "Payload: $text", Toast.LENGTH_LONG).show() // Debugging
                            when {
                                text.startsWith("SILENT:true") -> {
                                    try {
                                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
                                            !notificationManager.isNotificationPolicyAccessGranted) {
                                            // Prompt user to grant access
                                            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                            startActivity(intent)
                                            Toast.makeText(this, "Please grant Do Not Disturb access and try again.", Toast.LENGTH_LONG).show()
                                        } else {
                                            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                                            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                                            Toast.makeText(this, "Device set to silent mode!", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(this, "Failed to set silent mode: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                                text.startsWith("EMERGENCY:") -> {
                                    val phone = text.removePrefix("EMERGENCY:").trim()
                                    if (phone.isNotEmpty()) {
                                        // Request permission if not granted
                                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 1)
                                            Toast.makeText(this, "Grant call permission and try again.", Toast.LENGTH_LONG).show()
                                        } else {
                                            val intent = Intent(Intent.ACTION_CALL)
                                            intent.data = Uri.parse("tel:$phone")
                                            try {
                                                startActivity(intent)
                                            } catch (e: SecurityException) {
                                                Toast.makeText(this, "Permission denied to make calls.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }
                                text.startsWith("MESSAGE:") -> {
                                    val message = text.removePrefix("MESSAGE:").trim()
                                    if (message.isNotEmpty()) {
                                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    }
                } else {
                    updateTagInfo("No NDEF data found on this tag.")
                }
            } catch (e: Exception) {
                updateTagInfo("Error reading tag: ${e.message}")
            } finally {
                try {
                    ndef.close()
                } catch (e: Exception) {
                    // Handle closing error
                }
            }
        } else {
            updateTagInfo("Tag is not NDEF formatted.")
        }
    }

    private fun updateTagInfo(info: String) {
        tagInfoState.value = info
    }
}
