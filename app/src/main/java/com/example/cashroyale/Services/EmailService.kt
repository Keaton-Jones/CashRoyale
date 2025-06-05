package com.example.cashroyale.Services

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi


class EmailService {



    @RequiresApi(Build.VERSION_CODES.O)
    fun sendSpendBreakdownEmail(context: Context, recipientEmail: String, breakdownText: String) {
        val emailSubject = "Your Monthly Spend Breakdown - ${java.time.LocalDate.now().month.name}"
        val emailBody = "Dear User,\n\nHere's your spend breakdown for the past month:\n\n" +
                "${breakdownText}\n\n" +
                "Thanks for using our app!"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // Use "message/rfc822" for a more robust email type
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail)) // Recipients
            putExtra(Intent.EXTRA_SUBJECT, emailSubject)
            putExtra(Intent.EXTRA_TEXT, emailBody)
            // Optionally, add a BCC or CC:
            // putExtra(Intent.EXTRA_CC, arrayOf("cc@example.com"))
            // putExtra(Intent.EXTRA_BCC, arrayOf("bcc@example.com"))
        }

        // Always check if there's an app that can handle the intent
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(intent, "Send budget report via..."))
        } else {
            Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
        }
    }
}