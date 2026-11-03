package com.pgstorm148.protapclub

import android.app.AlertDialog
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pgstorm148.protapclub.ui.theme.ProtapClubTheme

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Check NFC status on app launch
        checkNfcStatus()

        setContent {
            ProtapClubTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onReadButtonClick = {
                            startActivity(Intent(this, ReadTagActivity::class.java))
                        },
                        onWriteButtonClick = {
                            startActivity(Intent(this, WriteOptionsActivity::class.java))
                        }
                    )
                }
            }
        }
    }

    private fun checkNfcStatus() {
        when {
            // Case 1: Device doesn't support NFC
            nfcAdapter == null -> {
                showAlertDialog(
                    "NFC Not Supported",
                    "Your device doesn't support NFC."
                )
            }

            // Case 2: NFC is disabled
            !nfcAdapter!!.isEnabled -> {
                showAlertDialog(
                    "NFC Disabled",
                    "Please turn on NFC."
                )
            }

            // Case 3: NFC is enabled
            else -> {
                Toast.makeText(
                    this,
                    "Welcome to ProTap Club, buddy!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Check NFC status again when the app resumes
        checkNfcStatus()
    }
}

@Composable
fun MainScreen(
    onReadButtonClick: () -> Unit,
    onWriteButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Protap Club Logo",
            modifier = Modifier.size(160.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Main navigation buttons
        Button(
            onClick = onReadButtonClick,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
        ) {
            Text("Read NFC Tag", style = MaterialTheme.typography.titleMedium)
        }

        Button(
            onClick = onWriteButtonClick,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
        ) {
            Text("Write NFC Tag", style = MaterialTheme.typography.titleMedium)
        }
    }
}
