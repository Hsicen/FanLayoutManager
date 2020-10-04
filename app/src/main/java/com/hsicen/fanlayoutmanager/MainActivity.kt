package com.hsicen.fanlayoutmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * 作者：hsicen  2020/10/4 16:13
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：主页Activity
 */
class MainActivity : AppCompatActivity() {
  private var mMainFragment: MainFragment? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    if (savedInstanceState == null) {
      supportFragmentManager
        .beginTransaction()
        .add(R.id.root, MainFragment.newInstance().also { mMainFragment = it })
        .commit()
    } else {
      val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.root)
      if (fragment is MainFragment) {
        mMainFragment = fragment
      }
    }
  }

  override fun onBackPressed() {
    val fragment = supportFragmentManager.findFragmentById(R.id.root)
    if (fragment is MainFragment) {
      mMainFragment = fragment
    }

    mMainFragment?.let {
      if (!it.isAdded || !it.deselectIfSelected()) {
        super.onBackPressed()
      }
    } ?: super.onBackPressed()
  }
}