package com.hsicen.fanlayoutmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.hsicen.library.CustomLayoutManager
import com.hsicen.library.LinearEdgeDecoration
import com.hsicen.library.dp2px
import com.hsicen.library.screenWidth
import kotlinx.android.synthetic.main.activity_gallery.*

/**
 * 作者：hsicen  2020/11/7 14:46
 * 邮箱：codinghuang@163.com
 * 作用：
 * 描述：FanLayoutManager
 */
class SnapActivity : AppCompatActivity() {
    private var curPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_gallery)

        initView()
    }

    private fun initView() {
        val customLayoutManager = CustomLayoutManager(this)
            .enableFan(false)
            .setItemInfo(254.dp2px, 254.dp2px, 34.dp2px)
            .setStartMargin((screenWidth() - 254.dp2px) / 2)
            .onItemChange {
                curPosition = it
                //Toast.makeText(this, "当前选中： $it", Toast.LENGTH_SHORT).show()
            }

        rvGallery.layoutManager = customLayoutManager
        val snapAdapter = SnapAdapter()
        rvGallery.adapter = snapAdapter

        rvGallery.addItemDecoration(
            LinearEdgeDecoration(
                startPadding = ((screenWidth() - 254.dp2px) / 2),
                endPadding = ((screenWidth() - 254.dp2px) / 2),
                itemMargin = 34.dp2px,
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
            holder.bindTo(position)

            holder.itemView.setOnClickListener {
                if (position != curPosition) {
                    curPosition = position
                    rvGallery.smoothScrollToPosition(curPosition)
                }
            }
        }

        override fun getItemCount() = 10

        inner class SnapViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val ivTemplate: ImageView = itemView.findViewById(R.id.iv_photo)
            private val tvIndex: TextView = itemView.findViewById(R.id.tvIndex)

            /*** 数据绑定*/
            fun bindTo(position: Int) {
                ivTemplate.setImageResource(R.drawable.beauty8)
                tvIndex.text = "$position"
            }
        }
    }
}