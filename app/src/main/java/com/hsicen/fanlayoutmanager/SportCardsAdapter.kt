package com.hsicen.fanlayoutmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.hsicen.fanlayoutmanager.SportCardsAdapter.SportCardViewHolder
import com.hsicen.fanlayoutmanager.model.SportCardModel
import java.util.*

class SportCardsAdapter : RecyclerView.Adapter<SportCardViewHolder>() {
  private val mItems: MutableList<SportCardModel> = ArrayList()
  private var onItemClickListener: ((pos: Int, view: View) -> Unit)? = null

  fun setOnItemClickListener(listener: (pos: Int, view: View) -> Unit) {
    onItemClickListener = listener
  }

  fun addAll(items: Collection<SportCardModel>): Boolean {
    val isAdded = mItems.addAll(items)
    if (isAdded) {
      notifyDataSetChanged()
    }
    return isAdded
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SportCardViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)

    return SportCardViewHolder(view)
  }

  override fun onBindViewHolder(holder: SportCardViewHolder, position: Int) {
    val data = mItems[position]
    holder.bindTo(data, position)

    holder.itemView.setOnClickListener {
      onItemClickListener?.invoke(holder.adapterPosition, holder.ivTemplate)
    }
  }

  override fun getItemCount(): Int = mItems.size

  fun getModelByPos(pos: Int): SportCardModel = mItems[pos]

  inner class SportCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val ivTemplate: ImageView = itemView.findViewById(R.id.ivTemplate)

    /*** 数据绑定*/
    fun bindTo(data: SportCardModel, position: Int) {
      ivTemplate.setImageResource(data.imageResId)
      ivTemplate.transitionName = "shared$position"
    }
  }
}