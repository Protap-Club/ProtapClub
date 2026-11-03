package com.pgstorm148.protapclub

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import java.nio.charset.Charset

abstract class NfcBaseActivity : ComponentActivity() {
    protected var nfcAdapter: NfcAdapter? = null
    protected var currentTag: Tag? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.let { adapter ->
            val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
            adapter.enableForegroundDispatch(this, pendingIntent, null, null)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    protected open fun handleNfcIntent(intent: Intent) {
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        if (tag != null) {
            currentTag = tag
            processTag(tag)
        }
    }

    protected abstract fun processTag(tag: Tag)

    protected fun buildTagInfo(tag: Tag): String {
        return """
            NFC Tag Detected!
            ID: ${tag.id.toHexString()}
            Technologies: ${tag.techList.joinToString()}
        """.trimIndent()
    }

    protected fun parseNdefMessage(ndefMessage: NdefMessage): String {
        val records = ndefMessage.records
        return records.joinToString("\n") { record ->
            when {
                record.toUri() != null -> "URI: ${record.toUri()}"
                record.toMimeType() != null -> {
                    // Check if it's a vCard
                    if (record.toMimeType() == "text/vcard") {
                        val payload = String(record.payload, Charset.forName("UTF-8"))
                        "Contact: $payload"
                    } else {
                        "MIME: ${String(record.payload)}"
                    }
                }
                else -> {
                    // Try to parse as plain text
                    try {
                        val payload = record.payload
                        // Skip language code (first byte indicates its length)
                        val languageCodeLength = payload[0].toInt() and 0x3F
                        val text = String(payload, 1 + languageCodeLength, payload.size - 1 - languageCodeLength,
                            Charset.forName("UTF-8"))
                        "Text: $text"
                    } catch (e: Exception) {
                        "Unknown Data Format"
                    }
                }
            }
        }
    }

    protected fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }
}
