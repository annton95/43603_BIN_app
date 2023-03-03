package com.annton.mrbincardinfo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListItem(val title: String, val description: String)

class MyAdapter(private val itemList: List<ListItem>, private val onClickListener: (item: ListItem) -> Unit) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleView: TextView = itemView.findViewById(R.id.text_item_title)
        val descriptionView: TextView = itemView.findViewById(R.id.text_item_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.titleView.text = item.title
        holder.descriptionView.text = item.description

        //
        holder.itemView.setOnClickListener { onClickListener(item) }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}