package com.pgstorm148.protapclub

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pgstorm148.protapclub.ui.theme.ProtapClubTheme
import java.nio.charset.Charset

class WriteCallToActionActivity : NfcBaseActivity() {
    private var selectedAction: String? = null
    private var actionData: String? = null
    private var messageBody: String? = null // For SMS body
    private var waitingForTag: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentAction by remember { mutableStateOf<String?>(null) }
            var currentData by remember { mutableStateOf<String?>(null) }
            var currentBody by remember { mutableStateOf<String?>(null) }
            var isWaiting by remember { mutableStateOf(false) }

            ProtapClubTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CallToActionScreen(
                        waitingForTag = isWaiting,
                        currentAction = currentAction,
                        currentData = currentData,
                        currentBody = currentBody,
                        onActionSelected = { action, data, body ->
                            selectedAction = action
                            actionData = data
                            messageBody = body
                            currentAction = action
                            currentData = data
                            currentBody = body
                            waitingForTag = true
                            isWaiting = true
                            Toast.makeText(this, "Tap an NFC tag to write.", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        }
    }

    override fun processTag(tag: Tag) {
        if (!waitingForTag || selectedAction == null) return
        waitingForTag = false
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                val record = when (selectedAction) {
                    "emergency" -> NdefRecord.createUri("tel:${actionData}")
                    "message" -> {
                        val smsUri = if (!messageBody.isNullOrBlank()) {
                            "sms:${actionData}?body=" + Uri.encode(messageBody)
                        } else {
                            "sms:${actionData}"
                        }
                        NdefRecord.createUri(smsUri)
                    }
                    "facetime" -> NdefRecord.createUri("facetime:${actionData}")
                    "facetime_audio" -> NdefRecord.createUri("facetime-audio:${actionData}")
                    "address" -> {
                        // Use a maps URI for best compatibility
                        NdefRecord.createUri("https://maps.google.com/?q=" + Uri.encode(actionData))
                    }
                    "location" -> {
                        // actionData should be "lat,lon"
                        NdefRecord.createUri("geo:${actionData}")
                    }
                    else -> null
                }
                if (record != null) {
                    val message = NdefMessage(arrayOf(record))
                    ndef.writeNdefMessage(message)
                    ndef.close()
                    runOnUiThread {
                        Toast.makeText(this, "Call to Action written successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Nothing written to tag.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error writing Call to Action: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            runOnUiThread {
                Toast.makeText(this, "NFC tag is not NDEF compatible.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun CallToActionScreen(
    waitingForTag: Boolean,
    currentAction: String?,
    currentData: String?,
    currentBody: String?,
    onActionSelected: (String, String, String?) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var inputText by remember { mutableStateOf("") }
    var smsNumber by remember { mutableStateOf("") }
    var smsBody by remember { mutableStateOf("") }
    var facetimeNumber by remember { mutableStateOf("") }
    var facetimeAudioNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Main content column (centered)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Protap Club Logo (top)
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Protap Club Logo",
                modifier = Modifier
                    .size(200.dp)
                    .padding(top = 24.dp, bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Centered content
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Call to Action",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Select Action")
                }
            }

            // Show the input to be written if waiting for tag
            if (waitingForTag && currentAction != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Input to be written:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        when (currentAction) {
                            "emergency" -> Text("Phone Number: ${currentData}")
                            "message" -> {
                                Text("Phone Number: ${currentData}")
                                if (!currentBody.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Message: ${currentBody}")
                                }
                            }
                            "facetime" -> Text("FaceTime: ${currentData}")
                            "facetime_audio" -> Text("FaceTime Audio: ${currentData}")
                            "address" -> Text("Address: ${currentData}")
                            "location" -> Text("Location: ${currentData}")
                            else -> Text("Unknown action")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Friendly message
            Text(
                text = "Empower your NFC tags with Protap Club!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Back button anchored at the bottom
        Button(
            onClick = { (context as? android.app.Activity)?.finish() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.7f)
                .padding(bottom = 16.dp)
        ) {
            Text("Back")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Action Type") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            selectedOption = "emergency"
                            showDialog = false
                        }
                    ) {
                        Text("Add Emergency Phone")
                    }
                    TextButton(
                        onClick = {
                            selectedOption = "message"
                            showDialog = false
                        }
                    ) {
                        Text("Add a Message")
                    }
                    TextButton(
                        onClick = {
                            selectedOption = "facetime"
                            showDialog = false
                        }
                    ) {
                        Text("FaceTime Number")
                    }
                    TextButton(
                        onClick = {
                            selectedOption = "facetime_audio"
                            showDialog = false
                        }
                    ) {
                        Text("FaceTime Audio Only")
                    }
                    TextButton(
                        onClick = {
                            selectedOption = "address"
                            showDialog = false
                        }
                    ) {
                        Text("Address")
                    }
                    TextButton(
                        onClick = {
                            selectedOption = "location"
                            showDialog = false
                        }
                    ) {
                        Text("Location (Lat, Lon)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (selectedOption == "emergency") {
        var showInputDialog by remember { mutableStateOf(true) }
        if (showInputDialog) {
            AlertDialog(
                onDismissRequest = { showInputDialog = false },
                title = { Text("Enter Emergency Phone") },
                text = {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Phone Number") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (inputText.isNotEmpty()) {
                                onActionSelected("emergency", inputText, null)
                                showInputDialog = false
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showInputDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (selectedOption == "message") {
        var showInputDialog by remember { mutableStateOf(true) }
        if (showInputDialog) {
            AlertDialog(
                onDismissRequest = { showInputDialog = false },
                title = { Text("Send SMS") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = smsNumber,
                            onValueChange = { smsNumber = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Phone Number") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = smsBody,
                            onValueChange = { smsBody = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Message Body") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (smsNumber.isNotEmpty()) {
                                onActionSelected("message", smsNumber, smsBody)
                                showInputDialog = false
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showInputDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (selectedOption == "facetime") {
        var showInputDialog by remember { mutableStateOf(true) }
        if (showInputDialog) {
            AlertDialog(
                onDismissRequest = { showInputDialog = false },
                title = { Text("FaceTime Number or Email") },
                text = {
                    OutlinedTextField(
                        value = facetimeNumber,
                        onValueChange = { facetimeNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Number or Email") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (facetimeNumber.isNotEmpty()) {
                                onActionSelected("facetime", facetimeNumber, null)
                                showInputDialog = false
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showInputDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (selectedOption == "facetime_audio") {
        var showInputDialog by remember { mutableStateOf(true) }
        if (showInputDialog) {
            AlertDialog(
                onDismissRequest = { showInputDialog = false },
                title = { Text("FaceTime Audio Number or Email") },
                text = {
                    OutlinedTextField(
                        value = facetimeAudioNumber,
                        onValueChange = { facetimeAudioNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Number or Email") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (facetimeAudioNumber.isNotEmpty()) {
                                onActionSelected("facetime_audio", facetimeAudioNumber, null)
                                showInputDialog = false
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showInputDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (selectedOption == "address") {
        var showInputDialog by remember { mutableStateOf(true) }
        if (showInputDialog) {
            AlertDialog(
                onDismissRequest = { showInputDialog = false },
                title = { Text("Enter Address") },
                text = {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Address") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (address.isNotEmpty()) {
                                onActionSelected("address", address, null)
                                showInputDialog = false
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showInputDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (selectedOption == "location") {
        var showInputDialog by remember { mutableStateOf(true) }
        if (showInputDialog) {
            AlertDialog(
                onDismissRequest = { showInputDialog = false },
                title = { Text("Enter Location") },
                text = {
                    Row {
                        OutlinedTextField(
                            value = latitude,
                            onValueChange = { latitude = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Latitude") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = longitude,
                            onValueChange = { longitude = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Longitude") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (latitude.isNotEmpty() && longitude.isNotEmpty()) {
                                onActionSelected("location", "$latitude,$longitude", null)
                                showInputDialog = false
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showInputDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
} 