package com.hsicen.fanlayoutmanager

import android.os.Bundle
import android.transition.Fade
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.hsicen.fanlayoutmanager.model.SportCardsUtils
import com.hsicen.fanlayoutmanager.transition.SharedTransitionSet
import com.hsicen.library.FanLayoutManager
import com.hsicen.library.FanLayoutManagerSettings
import com.hsicen.library.callbacks.FanChildDrawingOrderCallback
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * 作者：hsicen  2020/10/4 16:09
 * 邮箱：codinghuang@163.com
 * 作用：
 * 描述：主页Fragment
 */
class MainFragment : Fragment() {
    private var lastSelect = -1

    private val mFanLayoutManager by lazy {
        //设置默认配置
        val settings: FanLayoutManagerSettings = FanLayoutManagerSettings
            .newBuilder(requireActivity())
            .withFanRadius(true)
            .withViewHeightDp(212f)
            .withViewWidthDp(212f)
            .withItemMargin(17.dp2px)
            .withTopMargin(16.dp2px)
            .withBottomMargin(16.dp2px)
            .build()

        FanLayoutManager(requireActivity(), settings)
    }

    private val adapter by lazy {
        SportCardsAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvTemplate.layoutManager = mFanLayoutManager
        mFanLayoutManager.collapseViews()
        rvTemplate.itemAnimator = DefaultItemAnimator()

        adapter.addAll(SportCardsUtils.generateSportCards())
        rvTemplate.adapter = adapter
        rvTemplate.setChildDrawingOrderCallback(FanChildDrawingOrderCallback(mFanLayoutManager))

        //LayoutManager的Item选中回调
        mFanLayoutManager.addOnItemSelectedListener { position, _ ->
            if (lastSelect != position) {
                lastSelect = position
                Toast.makeText(activity, "当前选中：$position", Toast.LENGTH_SHORT).show()
            }
        }

        rvTemplate.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d("hsc", "监听滑动距离：$dx")
            }
        })

        btnChange.setOnClickListener {
            mFanLayoutManager.collapseViews()
        }

        //Item点击回调
        adapter.setOnItemClickListener { itemPosition, clickView ->
            if (lastSelect == itemPosition) {
                onClick(clickView, itemPosition)
            } else {
                mFanLayoutManager.smoothScrollToPosition(rvTemplate, null, itemPosition)
            }
        }
    }

    private fun onClick(view: View, pos: Int) {
        val fragment: DetailFragment = DetailFragment.newInstance(adapter.getModelByPos(pos))
        fragment.sharedElementEnterTransition = SharedTransitionSet()
        fragment.enterTransition = Fade()
        exitTransition = Fade()
        fragment.sharedElementReturnTransition = SharedTransitionSet()

        activity?.apply {
            supportFragmentManager
                .beginTransaction()
                .addSharedElement(view, "shared")
                .replace(R.id.root, fragment)
                .addToBackStack("home")
                .commit()
        }
    }

    fun deselectIfSelected(): Boolean {
        return if (mFanLayoutManager.isItemSelected) {
            mFanLayoutManager.deselectItem()
            true
        } else false
    }

    companion object {

        fun newInstance(): MainFragment {
            val args = Bundle()
            val fragment = MainFragment()
            fragment.arguments = args
            return fragment
        }
    }
}