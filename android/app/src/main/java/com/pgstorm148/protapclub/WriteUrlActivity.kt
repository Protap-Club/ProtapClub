package com.pgstorm148.protapclub

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
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

class WriteUrlActivity : NfcBaseActivity() {
    private val statusState = mutableStateOf("Tap an NFC tag to write URL")
    private val urlState = mutableStateOf("")

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
                    var url by remember { urlState }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Write URL to Tag",
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

                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("Enter URL") },
                            placeholder = { Text("https://example.com") },
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
        val urlToWrite = urlState.value
        if (urlToWrite.isNotEmpty()) {
            // Ensure the URL has a proper scheme (http:// or https://)
            val formattedUrl = formatUrl(urlToWrite)
            writeUrlToTag(tag, formattedUrl)
        } else {
            updateStatus("Please enter a URL first")
        }
    }

    private fun formatUrl(url: String): String {
        return if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
    }

    private fun writeUrlToTag(tag: Tag, url: String) {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                val uriRecord = NdefRecord.createUri(url)
                val ndefMessage = NdefMessage(arrayOf(uriRecord))
                ndef.writeNdefMessage(ndefMessage)
                updateStatus("URL written successfully! The link will open when tapped.")
            } catch (e: Exception) {
                updateStatus("Error writing to tag: ${e.message}")
            } finally {
                try {
                    ndef.close()
                } catch (e: Exception) {
                    // Handle closing error
                }
            }
        } else {
            updateStatus("Tag is not NDEF formatted.")
        }
    }

    private fun updateStatus(status: String) {
        statusState.value = status
    }
}