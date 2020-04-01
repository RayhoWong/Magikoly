package com.xfeng.beautyfacelib.Filter.baseFilter;

import android.content.Context;

import com.xfeng.beautyfacelib.base.GLImageFilter;
import com.xfeng.beautyfacelib.utils.shaderUtils.OpenGLUtils;

/**
 * 加载一张图片，需要倒过来
 */
public class GLImageInputFilter extends GLImageFilter {

    public GLImageInputFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/base/fragment_image_input.glsl"));
    }

    public GLImageInputFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }
}
