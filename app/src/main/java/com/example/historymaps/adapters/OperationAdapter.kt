package com.example.historymaps.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.historymaps.R
import com.example.historymaps.models.MilitaryOperation

class OperationAdapter(
    private val operations: List<MilitaryOperation>,
    private val onItemClick: (MilitaryOperation) -> Unit
) : RecyclerView.Adapter<OperationAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val subtitleTextView: TextView = itemView.findViewById(R.id.subtitleTextView)
        val periodTextView: TextView = itemView.findViewById(R.id.periodTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_operation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val operation = operations[position]

        holder.titleTextView.text = operation.title
        holder.subtitleTextView.text = operation.subtitle
        holder.periodTextView.text = operation.period

        holder.itemView.setOnClickListener {
            onItemClick(operation)
        }
    }

    override fun getItemCount(): Int = operations.size
}