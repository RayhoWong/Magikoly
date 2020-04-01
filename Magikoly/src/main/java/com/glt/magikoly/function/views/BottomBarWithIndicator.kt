package com.glt.magikoly.function.views

import android.animation.Animator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.glt.magikoly.function.resultreport.TabInfo
import com.glt.magikoly.utils.WindowController
import magikoly.magiccamera.R

class BottomBarWithIndicator @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr), View.OnClickListener {

    private val container: LinearLayout by lazy { findViewById<LinearLayout>(R.id.ll_indicator_container) }
    private val indicator: ImageView by lazy { findViewById<ImageView>(R.id.img_indicator_view) }
    private val screenWidth = WindowController.getScreenWidth()
    private var itemWidth = screenWidth / 3
    internal var tabListener: OnTabListener? = null
    private var mIndicatorBeans: ArrayList<IndicatorBean>? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.bottom_bar_with_indicator, this, true)
        indicator.setImageResource(R.drawable.indicator_drawable)
        indicator.isSelected = true
    }

    fun addAllIndicatorBean(indicatorBeans: ArrayList<IndicatorBean>) {
        container.removeAllViews()
        mIndicatorBeans = indicatorBeans
        mIndicatorBeans?.run {
            if (indicatorBeans.size < 3) {
                itemWidth = screenWidth / this.size
            }
        }

        indicator.layoutParams.width = itemWidth
        mIndicatorBeans?.forEach {
            addIndicatorBean(it)
        }
    }

    private fun addIndicatorBean(indicatorBean: IndicatorBean) {
        val inflate = LayoutInflater.from(context).inflate(R.layout.indicator_bean_layout, null)
        inflate.setOnClickListener(this)
        val params = LinearLayout.LayoutParams(itemWidth, LinearLayout.LayoutParams.MATCH_PARENT)
        val title: TextView = inflate.findViewById(R.id.tv_title)
        title.text = indicatorBean.title
        if (container.childCount == 0) {
            title.setTextColor(resources.getColor(R.color.tab_selected_text_color))
            title.textSize = 15f
        }
        container.addView(inflate, params)
    }


    override fun onClick(v: View?) {
        val tab = container.indexOfChild(v)
        showTab(tab, true)
    }

    fun showTab(tab: Int, fromUser: Boolean = false, animate: Boolean = true) {
        if (animate) {
            indicator.animate().translationX(tab * itemWidth * 1f)
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            doShowTab(tab, fromUser)
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                        override fun onAnimationStart(animation: Animator?) {
                        }

                    }).setDuration(200).start()
        } else {
            indicator.translationX = tab * itemWidth * 1f
            doShowTab(tab, fromUser)
        }
    }

    private fun doShowTab(tab: Int, fromUser: Boolean) {
        for (index in 0 until container.childCount) {
            val txtTitle = container.getChildAt(index)
                    .findViewById<TextView>(R.id.tv_title)
            if (index == tab) {
                txtTitle.setTextColor(
                        resources.getColor(R.color.tab_selected_text_color))
                txtTitle.textSize = 15f
            } else {
                txtTitle.setTextColor(
                        resources.getColor(R.color.tab_unselect_text_color))
                txtTitle.textSize = 14f
            }
        }
        mIndicatorBeans?.let { beans ->
            if (beans.isNotEmpty()) {
                beans[tab]?.let { bean ->
                    tabListener?.onTabSelected(bean, getChildAt(tab), fromUser)
                }
            }
        }
    }

    fun destroy() {
        mIndicatorBeans?.clear()
    }

    data class IndicatorBean(val title: String, val drawable: Drawable?, var tabInfo: TabInfo)

    interface OnTabListener {
        fun onTabSelected(indicatorBean: IndicatorBean, v: View?, fromUser: Boolean)
    }
}