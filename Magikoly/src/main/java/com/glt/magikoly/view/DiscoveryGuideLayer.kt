package com.glt.magikoly.view

import android.animation.ValueAnimator
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import com.glt.magikoly.animation.AnimatorUtil
import com.glt.magikoly.pref.PrefConst
import com.glt.magikoly.pref.PrefDelegate
import com.glt.magikoly.utils.DrawUtils
import magikoly.magiccamera.R

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/5/31
 * @tips 这个类是Object的子类
 * @fuction
 */

class DiscoveryGuideLayer private constructor() {

    companion object {
        fun getInstance() = Holder.instance
    }

    object Holder {
        val instance = DiscoveryGuideLayer()
    }

    private var mGuideController: EasyGuideLayer.Controller? = null

    private var mHasShownGuide: Boolean by PrefDelegate(PrefConst.KEY_HAS_SHOWN_DISCOVERY_GUIDE, false)

    private var mIsResetLayoutParams = false


    fun show(anchorView: View, targetView: View, onClick: () -> Unit) {
        if (mHasShownGuide) {
            return
        }

        val item = GuideItem.newInstance(targetView)
                .setLayout(R.layout.popup_discovery_guide)
                .setOffsetProvider { point, rectF, view ->
                    // 在此根据具体尺寸计算出中央位置
                    val offsetX = (rectF.width() - DrawUtils.dip2px(12f) - view.width).toInt()
                    val offsetY = 15
                    point.offset(offsetX, offsetY)
                    if (mIsResetLayoutParams) {
                        //浮动动画
                        val animTransY = AnimatorUtil.animTransY(view, 1500,
                                0f,
                                -DrawUtils.dip2px(10f) * 1f,
                                0f)
                        animTransY?.apply {
                            repeatCount = ValueAnimator.INFINITE
                            repeatMode = ValueAnimator.RESTART
                            start()
                        }
                    } else {
                        //计算出三角箭头的位置
                        val widthPercent = (rectF.width() * 5 / 6 + 10 - offsetX) * 1f / view.width
                        val ivRectangle = view.findViewById<ImageView>(R.id.iv_rectangle)
                        val lp = ivRectangle.layoutParams as ConstraintLayout.LayoutParams
                        lp.horizontalBias = widthPercent
                        ivRectangle.layoutParams = lp
                        mIsResetLayoutParams = true
                    }
                }
                .setOnViewAttachedListener { view, controller ->
                    view.setOnClickListener {
                        onClick()
                    }
                    mGuideController = controller
                    mHasShownGuide = true
                }
                .setGravity(Gravity.TOP)
        EasyGuideLayer.with(anchorView)
                .setBackgroundColor(Color.TRANSPARENT)
                .setInterceptClick(false)
//                .setOnGuideShownListener()
                .setDismissOnClickOutside(false)
                .addItem(item).show()
        mIsResetLayoutParams = false
    }

    fun hide() {
        if (mGuideController != null) {
            mGuideController?.dismiss()
            mGuideController = null
        }
    }
}