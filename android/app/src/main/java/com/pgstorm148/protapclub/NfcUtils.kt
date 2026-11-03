package com.pgstorm148.protapclub

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NfcUtils {
    fun writeTextToTag(tag: Tag, text: String): WriteResult {
        val ndef = Ndef.get(tag)
        return try {
            ndef?.connect()
            val textRecord = NdefRecord.createTextRecord("en", text)
            val ndefMessage = NdefMessage(arrayOf(textRecord))
            ndef?.writeNdefMessage(ndefMessage)
            WriteResult(true, "Text written successfully!")
        } catch (e: Exception) {
            WriteResult(false, "Error writing to tag: ${e.message}")
        } finally {
            try {
                ndef?.close()
            } catch (e: Exception) {
                // Handle closing error
            }
        }
    }

    fun writeSocialToTag(tag: Tag, socialType: SocialMediaType, username: String): WriteResult {
        val ndef = Ndef.get(tag)
        return try {
            ndef?.connect()

            // Generate the appropriate URL based on social media type
            val url = when (socialType) {
                SocialMediaType.FACEBOOK -> "https://facebook.com/$username"
                SocialMediaType.INSTAGRAM -> "https://instagram.com/$username"
                SocialMediaType.TWITTER -> "https://twitter.com/$username"
                SocialMediaType.LINKEDIN -> "https://linkedin.com/in/$username"
                SocialMediaType.GITHUB -> "https://github.com/$username"
                SocialMediaType.TIKTOK -> "https://tiktok.com/@$username"
                SocialMediaType.YOUTUBE -> "https://youtube.com/$username"
            }

            val uriRecord = NdefRecord.createUri(url)
            val ndefMessage = NdefMessage(arrayOf(uriRecord))
            ndef?.writeNdefMessage(ndefMessage)
            WriteResult(true, "Social media profile written successfully!\n\nPlatform: ${socialType.displayName}\nProfile: $url")
        } catch (e: Exception) {
            WriteResult(false, "Error writing social media profile to tag: ${e.message}")
        } finally {
            try {
                ndef?.close()
            } catch (e: Exception) {
                // Handle closing error
            }
        }
    }

    fun writeContactToTag(tag: Tag, contactInfo: Map<String, String>): WriteResult {
        val ndef = Ndef.get(tag)
        return try {
            ndef?.connect()

            // Create vCard content
            val vCardBuilder = StringBuilder()
            vCardBuilder.append("BEGIN:VCARD\n")
            vCardBuilder.append("VERSION:3.0\n")

            // Add contact fields if they exist
            contactInfo["name"]?.takeIf { it.isNotEmpty() }?.let { vCardBuilder.append("FN:$it\n") }
            contactInfo["phone"]?.takeIf { it.isNotEmpty() }?.let { vCardBuilder.append("TEL:$it\n") }
            contactInfo["email"]?.takeIf { it.isNotEmpty() }?.let { vCardBuilder.append("EMAIL:$it\n") }
            contactInfo["company"]?.takeIf { it.isNotEmpty() }?.let { vCardBuilder.append("ORG:$it\n") }
            contactInfo["title"]?.takeIf { it.isNotEmpty() }?.let { vCardBuilder.append("TITLE:$it\n") }
            contactInfo["address"]?.takeIf { it.isNotEmpty() }?.let { vCardBuilder.append("ADR:;;$it\n") }

            vCardBuilder.append("END:VCARD")

            // Create MIME record for vCard
            val mimeRecord = NdefRecord.createMime(
                "text/vcard",
                vCardBuilder.toString().toByteArray(Charset.forName("UTF-8"))
            )

            val ndefMessage = NdefMessage(arrayOf(mimeRecord))
            ndef?.writeNdefMessage(ndefMessage)
            WriteResult(true, "Contact information written successfully!")
        } catch (e: Exception) {
            WriteResult(false, "Error writing contact to tag: ${e.message}")
        } finally {
            try {
                ndef?.close()
            } catch (e: Exception) {
                // Handle closing error
            }
        }
    }

    fun resetTag(tag: Tag): WriteResult {
        val ndef = Ndef.get(tag)
        return try {
            ndef?.connect()

            // Get current date and time
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentDateTime = Date()
            val formattedDateTime = dateFormat.format(currentDateTime)
            val timestamp = currentDateTime.time

            // Get device info
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            val deviceName = "$manufacturer $model"

            // Create reset message
            val resetInfo = """
                TAG RESET
                Date: $formattedDateTime
                Timestamp: $timestamp
                Device: $deviceName
            """.trimIndent()

            val textRecord = NdefRecord.createTextRecord("en", resetInfo)
            val ndefMessage = NdefMessage(arrayOf(textRecord))
            ndef?.writeNdefMessage(ndefMessage)
            WriteResult(true, "Tag reset successfully!\n\n$resetInfo")
        } catch (e: Exception) {
            WriteResult(false, "Error resetting tag: ${e.message}")
        } finally {
            try {
                ndef?.close()
            } catch (e: Exception) {
                // Handle closing error
            }
        }
    }
}

data class WriteResult(val success: Boolean, val message: String)

// Common model classes
enum class WriteOption {
    URL, TEXT, CONTACT, SOCIAL, RESET, CALL_TO_ACTION
}

enum class SocialMediaType(val displayName: String) {
    FACEBOOK("Facebook"),
    INSTAGRAM("Instagram"),
    TWITTER("Twitter / X"),
    LINKEDIN("LinkedIn"),
    GITHUB("GitHub"),
    TIKTOK("TikTok"),
    YOUTUBE("YouTube")
}