package com.hsicen.library.gallery;

import android.os.Handler;
import android.os.Looper;

/**
 * 作者：hsicen  2020/11/5 18:06
 * 邮箱：codinghuang@163.com
 * 作用：
 * 描述：FanLayoutManager
 */
public class ThreadUtils {
    public static void runOnUiThread(Runnable r) {
        if (isMainThread()) {
            r.run();
        } else {
            LazyHolder.sUiThreadHandler.post(r);
        }
    }

    public static void runOnUiThread(Runnable r, long delay) {
        LazyHolder.sUiThreadHandler.postDelayed(r, delay);
    }

    public static void removeCallbacks(Runnable r) {
        LazyHolder.sUiThreadHandler.removeCallbacks(r);
    }

    private static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    private static class LazyHolder {
        private static final Handler sUiThreadHandler = new Handler(Looper.getMainLooper());
    }

}
