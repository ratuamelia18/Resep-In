package com.ratu.resep_in.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ratu.resep_in.R

class SuggestionAdapter(
    private var suggestions: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {

    private var currentQuery: String = ""

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSuggestionText: TextView = view.findViewById(R.id.tvSuggestionText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val originalText = suggestions[position]

        if (currentQuery.isNotEmpty() && originalText.lowercase().contains(currentQuery.lowercase())) {
            val startIdx = originalText.lowercase().indexOf(currentQuery.lowercase())
            val endIdx = startIdx + currentQuery.length

            val matchPart = originalText.substring(startIdx, endIdx)
            val restPart = originalText.substring(endIdx)
            val beforePart = originalText.substring(0, startIdx)

            val formattedText = "$beforePart<b>$matchPart</b>$restPart"
            holder.tvSuggestionText.text = Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)
            holder.tvSuggestionText.setTextColor(android.graphics.Color.parseColor("#222222"))
        } else {
            holder.tvSuggestionText.text = originalText
            holder.tvSuggestionText.setTypeface(null, android.graphics.Typeface.NORMAL)
            holder.tvSuggestionText.setTextColor(android.graphics.Color.parseColor("#999999"))
        }

        holder.itemView.setOnClickListener {
            onItemClick(originalText)
        }
    }

    override fun getItemCount(): Int = suggestions.size

    fun updateData(newSuggestions: List<String>, query: String) {
        suggestions = newSuggestions
        currentQuery = query
        notifyDataSetChanged()
    }
}