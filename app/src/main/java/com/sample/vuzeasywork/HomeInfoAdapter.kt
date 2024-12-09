package com.sample.vuzeasywork

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeInfoAdapter(private val infoList: List<HomeInfo>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_PARAGRAPH = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (infoList[position].isHeader) TYPE_HEADER else TYPE_PARAGRAPH
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_header, parent, false) // Используем кастомный макет для заголовков
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_paragraph, parent, false) // Используем кастомный макет для абзацев
            ParagraphViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = infoList[position]
        if (holder is HeaderViewHolder) {
            holder.headerTextView.text = item.text
        } else if (holder is ParagraphViewHolder) {
            holder.paragraphTextView.text = item.text
        }
    }

    override fun getItemCount(): Int = infoList.size

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTextView: TextView = itemView.findViewById(R.id.headerTextView)
    }

    class ParagraphViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paragraphTextView: TextView = itemView.findViewById(R.id.paragraphTextView)
    }
}

data class HomeInfo(
    val text: String,
    val isHeader: Boolean = false
)
