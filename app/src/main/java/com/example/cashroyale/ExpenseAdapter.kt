package com.example.cashroyale

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

// Adapter for list ---> recycler
class ExpenseAdapter(private val expenseList: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    // ViewHolder holds the view basically
    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.expenseDescription)
        val amountTextView: TextView = itemView.findViewById(R.id.expenseAmount)
        val dateTextView: TextView = itemView.findViewById(R.id.expenseDate)
        val imageView: ImageView = itemView.findViewById(R.id.expenseImage)  // Clickable image for downlaod
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.expense_item, parent, false)
        return ExpenseViewHolder(itemView)
    }

    // This binds each expense to a ViewHolder
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.descriptionTextView.text = expense.description
        holder.amountTextView.text = "$${expense.amount}"
        holder.dateTextView.text = expense.date

        // If theres an image itll load it and save
        if (expense.imageUri != null) {
            val uri = Uri.parse(expense.imageUri)
            holder.imageView.setImageURI(uri)

            holder.imageView.setOnClickListener {
                val context = holder.itemView.context
                downloadImageToDownloadsFolder(context, uri)
            }
        } else {
            // If no image, show default pic and cant click
            holder.imageView.setImageResource(android.R.drawable.ic_menu_camera)
            holder.imageView.setOnClickListener(null)
        }
    }

    // Returns item num
    override fun getItemCount(): Int {
        return expenseList.size
    }

    // Func for downlaoding the pic, getting permissions error , smtn abt new verison of Android
    private fun downloadImageToDownloadsFolder(context: Context, imageUri: Uri) {
        try {
            val resolver = context.contentResolver
            val inputStream = resolver.openInputStream(imageUri) ?: return

            // Give the image a name to then call
            val fileName = "expense_${System.currentTimeMillis()}.jpg"

            // Code to save data, but no permission so cant test
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // code for new version, doesnt work
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }

            // Add image
            val imageOutUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageOutUri != null) {
                resolver.openOutputStream(imageOutUri).use { outputStream ->
                    inputStream.copyTo(outputStream!!)
                }
                Toast.makeText(context, "Image saved to Downloads", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // CEror handling
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
