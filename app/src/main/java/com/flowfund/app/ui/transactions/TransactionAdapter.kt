package com.flowfund.app.ui.transactions

import android.graphics.Color
import android.net.Uri
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flowfund.app.R
import com.flowfund.app.data.entities.Category
import com.flowfund.app.data.entities.Transaction
import com.flowfund.app.utils.DateUtils
import java.text.NumberFormat
import java.util.Locale

class TransactionAdapter(
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit,
    private val onPhotoClick: (String) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.VH>(DIFF) {

    var categories: List<Category> = emptyList()
    private val currency = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvAmount: TextView       = view.findViewById(R.id.tvAmount)
        val tvDescription: TextView  = view.findViewById(R.id.tvDescription)
        val tvDate: TextView         = view.findViewById(R.id.tvDate)
        val tvCategory: TextView     = view.findViewById(R.id.tvCategory)
        val tvType: TextView         = view.findViewById(R.id.tvType)
        val ivPhoto: ImageView       = view.findViewById(R.id.ivPhoto)
        val btnEdit: View            = view.findViewById(R.id.btnEdit)
        val btnDelete: View          = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val tx = getItem(position)
        val cat = categories.find { it.id == tx.categoryId }

        holder.tvAmount.text = currency.format(tx.amount)
        holder.tvAmount.setTextColor(if (tx.type == "INCOME") Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
        holder.tvDescription.text = tx.description.ifBlank { "No description" }
        holder.tvDate.text = DateUtils.format(tx.date)
        holder.tvCategory.text = cat?.name ?: "Uncategorised"
        holder.tvType.text = tx.type
        holder.tvType.setBackgroundColor(if (tx.type == "INCOME") Color.parseColor("#E8F5E9") else Color.parseColor("#FFEBEE"))
        holder.tvType.setTextColor(if (tx.type == "INCOME") Color.parseColor("#2E7D32") else Color.parseColor("#C62828"))

        if (tx.photoPath != null) {
            holder.ivPhoto.visibility = View.VISIBLE
            Glide.with(holder.itemView).load(Uri.parse(tx.photoPath)).centerCrop()
                .placeholder(R.drawable.ic_photo).into(holder.ivPhoto)
            holder.ivPhoto.setOnClickListener { onPhotoClick(tx.photoPath) }
        } else {
            holder.ivPhoto.visibility = View.GONE
        }

        holder.btnEdit.setOnClickListener { onEdit(tx) }
        holder.btnDelete.setOnClickListener { onDelete(tx) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(a: Transaction, b: Transaction) = a.id == b.id
            override fun areContentsTheSame(a: Transaction, b: Transaction) = a == b
        }
    }
}
