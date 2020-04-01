package com.glt.magikoly.event

import com.glt.magikoly.function.FaceFunctionBean

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/4/2
 * @tips 这个类是Object的子类
 * @fuction
 */

/**
 * 图片扫描完毕事件
 */
class ImageDetectEvent(val tag: String, val originalPath: String, val faceFunctionBean: FaceFunctionBean, var progressBarHandled:Boolean = false) : BaseEvent()