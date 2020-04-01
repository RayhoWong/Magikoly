package com.glt.magikoly.function.poster

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import com.glt.magikoly.utils.DrawUtils
import magikoly.magiccamera.R


class FacePosterContainer @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private lateinit var backgroundImage: ImageView
    private lateinit var iconImage: ImageView
    private lateinit var originalImage: ImageView
    private lateinit var resultImage: ImageView
    private lateinit var waterMarkImage: ImageView
    private lateinit var appName: TextView
    private lateinit var appDesc: TextView
    private lateinit var posterResultTitle: TextView
    private lateinit var posterResultDesc: TextView
    private lateinit var posterGPDesc: TextView


    override fun onFinishInflate() {
        super.onFinishInflate()
        backgroundImage = findViewById(R.id.poster_background)
        iconImage = findViewById(R.id.poster_app_icon)
        originalImage = findViewById(R.id.poster_original_image)
        resultImage = findViewById(R.id.poster_result_image)
        waterMarkImage = findViewById(R.id.poster_water_mask)
        appName = findViewById(R.id.poster_app_name)
        appDesc = findViewById(R.id.poster_app_desc)
        posterResultTitle = findViewById(R.id.poster_result_title)
        posterResultDesc = findViewById(R.id.poster_result_desc)
        posterGPDesc = findViewById(R.id.poster_gp_desc)

    }

    fun setGPColor(color: Int) {
        // 从资源获取字体大小
        val pixelSize = DrawUtils.sp2px(16f)
        // 第一个参数:包含占位符字符串
        // 第二个可变参数:替换字符串的占位符,按数据类型填写,不然会报错
        var appName = context.getString(R.string.app_name)
        val playUserPhotoTip = String.format(context.getString(R.string.poster_gp_desc), appName)
        val index = playUserPhotoTip.indexOf(appName)
        // 字体颜色
        val redColors = ColorStateList.valueOf(color)
        // 使文本以指定的字体、大小、样式和颜色绘制。0表示使用默认的大小和字体
        val textAppearanceSpan = TextAppearanceSpan(null, 0, pixelSize, redColors, null)
        // 使用SpannableStringBuilder设置字体大小和颜色
        val spanBuilder = SpannableStringBuilder(playUserPhotoTip)
        spanBuilder.setSpan(textAppearanceSpan, index, appName.length + index, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        posterGPDesc.text = spanBuilder
    }


    fun setPosterTitle(title: String) {
        posterResultTitle.text = title
    }

    fun setPosterBackground(background: Bitmap) {
        backgroundImage.setImageBitmap(background)
    }

    fun setPosterDesc(desc: String) {
        posterResultDesc.text = desc
    }

    fun setOriginalImage(original: Bitmap) {
        originalImage.setImageBitmap(original)
    }

    fun setResultImage(result: Bitmap) {
        resultImage.setImageBitmap(result)
    }
}