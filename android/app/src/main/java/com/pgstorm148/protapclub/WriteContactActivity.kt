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

class WriteContactActivity : NfcBaseActivity() {
    private val statusState = mutableStateOf("Tap an NFC tag to write contact details")

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
                    var name by remember { mutableStateOf("") }
                    var phone by remember { mutableStateOf("") }
                    var email by remember { mutableStateOf("") }
                    var company by remember { mutableStateOf("") }
                    var title by remember { mutableStateOf("") }
                    var address by remember { mutableStateOf("") }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Write Contact to Tag",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Logo
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Protap Club Logo",
                            modifier = Modifier.size(100.dp)
                        )



                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = company,
                                onValueChange = { company = it },
                                label = { Text("Company") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Job Title") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Address") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                        }

                        Text(
                            status.value,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Button(
                            onClick = {
                                if (currentTag != null) {
                                    val contactInfo = mapOf(
                                        "name" to name,
                                        "phone" to phone,
                                        "email" to email,
                                        "company" to company,
                                        "title" to title,
                                        "address" to address
                                    )
                                    val result = NfcUtils.writeContactToTag(currentTag!!, contactInfo)
                                    updateStatus(result.message)
                                } else {
                                    updateStatus("Tap an NFC tag to write contact")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = currentTag != null
                        ) {
                            Text("Write Contact")
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
        updateStatus("Tag detected! You can now write your contact.")
    }

    private fun updateStatus(status: String) {
        statusState.value = status
    }
}