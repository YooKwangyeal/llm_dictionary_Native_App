package com.example.openary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DictionaryAdapter(private var items: List<DictionaryWord>) : RecyclerView.Adapter<DictionaryAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val wordTitle: TextView = view.findViewById(R.id.wordTitle)
        val posTag: TextView = view.findViewById(R.id.posTag)
        val scoreBadge: TextView = view.findViewById(R.id.scoreBadge)
        val lengthBadge: TextView = view.findViewById(R.id.lengthBadge)
        val rarityBadge: TextView = view.findViewById(R.id.rarityBadge)
        val confidenceBadge: TextView = view.findViewById(R.id.confidenceBadge)
        val definition: TextView = view.findViewById(R.id.definition)
        val example: TextView = view.findViewById(R.id.example)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_word, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = items[position]
        holder.wordTitle.text = word.word
        holder.posTag.text = word.pos ?: "unknown"
        holder.scoreBadge.text = "점수: %.2f".format(word.score ?: 0.0)
        holder.lengthBadge.text = "길이: %d글자".format(word.length ?: word.word.length)
        holder.rarityBadge.text = "희귀도: %s".format(word.rarity ?: "N/A")
        holder.confidenceBadge.text = "신뢰도: %d%%".format(((word.confidence ?: 0.0) * 100).toInt())
        holder.definition.text = word.definition ?: "정의가 없습니다"
        holder.example.text = word.example?.let { "\"$it\"" } ?: ""
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<DictionaryWord>) {
        items = newItems
        notifyDataSetChanged()
    }
}
