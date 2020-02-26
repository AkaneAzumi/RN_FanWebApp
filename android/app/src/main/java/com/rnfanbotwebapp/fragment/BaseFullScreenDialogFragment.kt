package com.rnfanbotwebapp.fragment

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout



import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import android.view.*
import com.rnfanbotwebapp.R


abstract class BaseFullScreenDialogFragment : BottomSheetDialogFragment() {

    var behavior: BottomSheetBehavior<FrameLayout>? = null
        private set
    /**
     * 获取屏幕高度
     *
     * @return height
     */
    private// 使用Point已经减去了状态栏高度
    val height: Int
        get() {
            var height = 1920
            if (context != null) {
                val wm = context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val point = Point()
                if (wm != null) {
                    wm.defaultDisplay.getSize(point)
                    height = point.y
                }
            }
            return height
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return if (context == null) {
                super.onCreateDialog(savedInstanceState)
            } else
                BottomSheetDialog(context!!, R.style.TransparentBottomSheetStyle)

        }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as BottomSheetDialog
        dialog.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window.setBackgroundDrawableResource(R.color.colorTans)
        // 设置软键盘不自动弹出
        dialog.window?.setDimAmount(0.0f)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        val bottomSheet = dialog.delegate.findViewById<FrameLayout>(R.id.design_bottom_sheet)

        if (bottomSheet != null) {
            val layoutParams = bottomSheet.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.height = height
            behavior = BottomSheetBehavior.from(bottomSheet)

            // 初始为展开状态
            behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            behavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(p0: View, p1: Float) {
                }

                override fun onStateChanged(p0: View, p1: Int) {
                    if (p1===BottomSheetBehavior.STATE_DRAGGING){
                        behavior?.state=BottomSheetBehavior.STATE_EXPANDED
                    }
                }

            })

        }
    }

}
