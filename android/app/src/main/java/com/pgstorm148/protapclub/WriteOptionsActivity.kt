package com.pgstorm148.protapclub

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pgstorm148.protapclub.ui.theme.ProtapClubTheme

class WriteOptionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ProtapClubTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Choose Write Option",
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

                        WriteOptionButton("Write URL", WriteOption.URL)
                        WriteOptionButton("Write Text", WriteOption.TEXT)
                        WriteOptionButton("Write Contact", WriteOption.CONTACT)
                        WriteOptionButton("Write Social Media", WriteOption.SOCIAL)
                        WriteOptionButton("Call to Action", WriteOption.CALL_TO_ACTION)
                        WriteOptionButton("Reset Tag", WriteOption.RESET)

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

    @Composable
    fun WriteOptionButton(text: String, option: WriteOption) {
        Button(
            onClick = {
                val intent = when (option) {
                    WriteOption.URL -> Intent(this, WriteUrlActivity::class.java)
                    WriteOption.TEXT -> Intent(this, WriteTextActivity::class.java)
                    WriteOption.CONTACT -> Intent(this, WriteContactActivity::class.java)
                    WriteOption.SOCIAL -> Intent(this, WriteSocialActivity::class.java)
                    WriteOption.RESET -> Intent(this, ResetTagActivity::class.java)
                    WriteOption.CALL_TO_ACTION -> Intent(this, WriteCallToActionActivity::class.java)
                }
                startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text)
        }
    }
}
