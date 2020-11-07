package com.hsicen.fanlayoutmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.hsicen.library.widget.CustomLayoutManager
import com.hsicen.library.widget.LinearEdgeDecoration
import kotlinx.android.synthetic.main.activity_gallery.*

/**
 * 作者：hsicen  2020/11/7 14:46
 * 邮箱：codinghuang@163.com
 * 作用：
 * 描述：FanLayoutManager
 */
class SnapActivity : AppCompatActivity() {
    private var curPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_gallery)

        initView()
    }

    private fun initView() {
        val customLayoutManager = CustomLayoutManager(this)
        customLayoutManager.itemWidthPx = 212.dp2px
        rvGallery.layoutManager = customLayoutManager
        val snapAdapter = SnapAdapter()
        rvGallery.adapter = snapAdapter

        rvGallery.addItemDecoration(
            LinearEdgeDecoration(
                startPadding = ((screenWidth() - 212.dp2px) / 2),
                endPadding = ((screenWidth() - 212.dp2px) / 2),
                itemMargin = 16.dp2px,
                orientation = RecyclerView.HORIZONTAL
            )
        )
    }


    inner class SnapAdapter : RecyclerView.Adapter<SnapAdapter.SnapViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapViewHolder {
            val root =
                LayoutInflater.from(parent.context).inflate(R.layout.item_gallery, parent, false)

            return SnapViewHolder(root)
        }

        override fun onBindViewHolder(holder: SnapViewHolder, position: Int) {
            holder.bindTo()

            curPosition = position
            holder.itemView.setOnClickListener {
                Toast.makeText(this@SnapActivity, "当前点击： $position", Toast.LENGTH_SHORT).show()
            }
        }

        override fun getItemCount() = 30


        inner class SnapViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivTemplate: ImageView = itemView.findViewById(R.id.iv_photo)

            /*** 数据绑定*/
            fun bindTo() {
                ivTemplate.setImageResource(R.drawable.beauty7)
            }
        }
    }
}