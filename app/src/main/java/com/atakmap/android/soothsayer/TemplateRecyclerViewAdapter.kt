package com.atakmap.android.soothsayer

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.atakmap.android.soothsayer.models.request.TemplateDataModel
import com.atakmap.android.soothsayer.plugin.R

data class MutableTuple<A, B, C>(
    var first: A,
    var second: B,
    var third: C,
)

class TemplateRecyclerViewAdapter(private val items: MutableList<MutableTuple<TemplateDataModel, Boolean, Bitmap?>>) :
    RecyclerView.Adapter<TemplateRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: Button = itemView.findViewById(R.id.btnLoadTemplate)
        val icon: ImageView = itemView.findViewById(R.id.ivTemplateIcon)
        val radio: CheckBox = itemView.findViewById(R.id.cbSelectTemplate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.template_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tuple = items[position]
        holder.button.text = tuple.first.template.name
        holder.icon.setImageBitmap(tuple.third)
        holder.radio.isChecked = tuple.second

        holder.radio.setOnCheckedChangeListener { _, isChecked ->
            tuple.second = isChecked
        }
    }

    override fun getItemCount(): Int = items.size

    fun addTemplate(template: TemplateDataModel, icon: Bitmap?) {
        items.add(MutableTuple(template, false, icon))
        notifyItemInserted(items.size - 1)
    }

    fun selectAll(select: Boolean) {
        items.forEach { it.second = select }
        notifyDataSetChanged()
    }

    fun deleteSelected() {
        var pos = 0
        while (pos < items.size) {
            if (items[pos].second) {
                items.removeAt(pos)
                notifyItemRemoved(pos)
            } else ++pos
        }
    }
}