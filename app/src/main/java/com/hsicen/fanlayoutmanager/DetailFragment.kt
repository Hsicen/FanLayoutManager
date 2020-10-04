package com.hsicen.fanlayoutmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hsicen.fanlayoutmanager.model.SportCardModel
import kotlinx.android.synthetic.main.frament_detail.*

/**
 * 作者：hsicen  2020/10/4 16:13
 * 邮箱：codinghuang@163.com
 * 作用：
 * 描述：模板详情Fragment
 */
class DetailFragment : Fragment() {
  //模板数据
  private var mTemplate: SportCardModel? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    mTemplate = arguments?.getParcelable(TRANS_DATA)
    //mTemplate = savedInstanceState?.getParcelable(TRANS_DATA)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.frament_detail, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    mTemplate?.let {
      ivVideoPlay.setImageResource(it.imageResId)
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    outState.putParcelable(TRANS_DATA, mTemplate)
    super.onSaveInstanceState(outState)
  }

  companion object {
    private const val TRANS_DATA = "trans_data"

    fun newInstance(data: SportCardModel): DetailFragment {
      val args = Bundle()
      args.putParcelable(TRANS_DATA, data)
      val fragment = DetailFragment()
      fragment.arguments = args
      return fragment
    }
  }
}