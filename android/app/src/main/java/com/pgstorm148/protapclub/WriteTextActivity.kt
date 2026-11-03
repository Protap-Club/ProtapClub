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

class WriteTextActivity : NfcBaseActivity() {
    private val statusState = mutableStateOf("Tap an NFC tag to write text")
    // Keep track of the text content
    private val textContent = mutableStateOf("")

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
                    // Use the class-level state for text
                    var text by remember { textContent }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Write Text to Tag",
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
                            value = text,
                            onValueChange = { text = it },
                            label = { Text("Enter Text") },
                            placeholder = { Text("Your message here") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
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
        // Use the class-level textContent state instead of findViewById
        val textToWrite = textContent.value

        if (textToWrite.isNotEmpty()) {
            val result = NfcUtils.writeTextToTag(tag, textToWrite)
            updateStatus(result.message)
        } else {
            updateStatus("Please enter text first")
        }
    }

    private fun updateStatus(status: String) {
        statusState.value = status
    }
}