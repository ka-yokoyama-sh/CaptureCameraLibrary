package jp.co.shcl.capture_camera.ui

import android.view.View
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch

/**
 * Viewをフェードイン
 * @param lifecycleCoroutineScope   コルーチンスコープ
 */
fun View.fadeIn(lifecycleCoroutineScope: LifecycleCoroutineScope) {
    lifecycleCoroutineScope.launch {
        alpha = 0f
        visibility = View.VISIBLE
        animate()
            .alpha(1f)
            .setDuration(200)
            .start()
    }
}

/**
 * Viewをフェードアウト
 * @param lifecycleCoroutineScope   コルーチンスコープ
 * @param toGone                    フェードアウト後に可視性を[View.GONE]に設定する場合true
 */
fun View.fadeOut(lifecycleCoroutineScope: LifecycleCoroutineScope, toGone: Boolean) {
    lifecycleCoroutineScope.launch {
        alpha = 1f
        animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction { visibility = if (toGone) View.GONE else View.INVISIBLE }
            .start()
    }
}