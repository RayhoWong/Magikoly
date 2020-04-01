package com.glt.magikoly.animation;

import android.animation.*;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ┌───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┐
 * │Esc│ │ F1│ F2│ F3│ F4│ │ F5│ F6│ F7│ F8│ │ F9│F10│F11│F12│ │P/S│S L│P/B│ ┌┐    ┌┐    ┌┐
 * └───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┘ └┘    └┘    └┘
 * ┌──┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───────┐┌───┬───┬───┐┌───┬───┬───┬───┐
 * │~`│! 1│@ 2│# 3│$ 4│% 5│^ 6│& 7│* 8│( 9│) 0│_ -│+ =│ BacSp ││Ins│Hom│PUp││N L│ / │ * │ - │
 * ├──┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─────┤├───┼───┼───┤├───┼───┼───┼───┤
 * │Tab │ Q │ W │ E │ R │ T │ Y │ U │ I │ O │ P │{ [│} ]│ | \ ││Del│End│PDn││ 7 │ 8 │ 9 │   │
 * ├────┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴─────┤└───┴───┴───┘├───┼───┼───┤ + │
 * │Caps │ A │ S │ D │ F │ G │ H │ J │ K │ L │: ;│" '│ Enter  │             │ 4 │ 5 │ 6 │   │
 * ├─────┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴────────┤    ┌───┐    ├───┼───┼───┼───┤
 * │Shift  │ Z │ X │ C │ V │ B │ N │ M │< ,│> .│? /│  Shift   │    │ ↑ │    │ 1 │ 2 │ 3 │   │
 * ├────┬──┴─┬─┴──┬┴───┴───┴───┴───┴───┴──┬┴───┼───┴┬────┬────┤┌───┼───┼───┐├───┴───┼───┤ E││
 * │Ctrl│Ray │Alt │         Space         │ Alt│code│fuck│Ctrl││ ← │ ↓ │ → ││   0   │ . │←─┘│
 * └────┴────┴────┴───────────────────────┴────┴────┴────┴────┘└───┴───┴───┘└───────┴───┴───┘
 *
 * @author Rayhahah
 * @time 2018/4/17
 * @fuction 属性动画工具类
 */
public class AnimatorUtil {

    @Retention(RetentionPolicy.SOURCE)
    public @interface Anim {
        String ALPHA = "alpha";
        String TRANSLATION_X = "translationX";
        String TRANSLATION_Y = "translationY";
        String X = "x";
        String Y = "Y";
        String ROTATION = "rotation";
        String ROTATION_X = "rotationX";
        String ROTATION_Y = "rotationY";
        String SCALE_X = "scaleX";
        String SCALE_Y = "scaleY";
    }

    public static ValueAnimator createValueAnimator(long duration, TimeInterpolator interpolator, AnimListener listener) {
        return createValueAnimator(duration, interpolator, ValueAnimator.RESTART, 0, listener);
    }

    public static ValueAnimator createValueAnimator(long duration, TimeInterpolator interpolator, int repeatMode, int repeatConut, final AnimListener listener) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (listener != null) {
                    listener.onUpdate(animation, null);
                }
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (listener != null) {
                    listener.onStart(animation, null);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null) {
                    listener.onEnd(animation, null);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (listener != null) {
                    listener.onCancel(animation, null);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (listener != null) {
                    listener.onRepeat(animation, null);
                }
            }
        });
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.setDuration(duration);
        valueAnimator.setRepeatMode(repeatMode);
        valueAnimator.setRepeatCount(repeatConut);
        return valueAnimator;
    }

    public static void animSet(Animator... animators) {
        animSet(null, animators);
    }

    public static void animSet(TimeInterpolator timeInterpolator, Animator... animators) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        if (timeInterpolator != null) {
            animatorSet.setInterpolator(timeInterpolator);
        }
        animatorSet.start();
    }

    public static ObjectAnimator animPropertyValuesHolder(View target, long duration, AnimListener listener, PropertyValuesHolder... pvhs) {
        ObjectAnimator oa = ObjectAnimator.ofPropertyValuesHolder(target, pvhs);
        return doAnim(target, oa, duration, new AccelerateDecelerateInterpolator(), 0, ValueAnimator.RESTART, listener);
    }

    public static ObjectAnimator animRotate(View target, long duration, final AnimListener listener, float... floats) {
        return animRotate(target,duration,0,listener,floats);
    }

    public static ObjectAnimator animRotate(View target, long duration,int repeatCount, final AnimListener listener, float... floats) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(target, Anim.ROTATION, floats);
        return doAnim(target, oa, duration, new LinearInterpolator(), repeatCount, ValueAnimator.RESTART, listener);
    }

    public static ObjectAnimator animTransX(View target, long duration, float... floats) {
        return animTransX(target, duration, null, floats);
    }


    public static ObjectAnimator animTransX(View target, long duration, final AnimListener listener, float... floats) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(target, Anim.TRANSLATION_X, floats);
        return doAnim(target, oa, duration, new AccelerateDecelerateInterpolator(), 0, ValueAnimator.RESTART, listener);
    }

    public static ObjectAnimator animTransY(View target, long duration, float... floats) {
        return animTransY(target, duration, null, floats);
    }


    public static ObjectAnimator animTransY(View target, long duration, final AnimListener listener, float... floats) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(target, Anim.TRANSLATION_Y, floats);
        return doAnim(target, oa, duration, new AccelerateDecelerateInterpolator(), 0, ValueAnimator.RESTART, listener);
    }

    public static ObjectAnimator animScaleIn(View target, long duration) {
        return animScaleIn(target, duration, null);
    }

    public static ObjectAnimator animScaleIn(View target, long duration, final AnimListener listener) {
        return animScale(target, duration, listener, 0, 1);
    }

    public static ObjectAnimator animScaleOut(View target, long duration) {
        return animScaleOut(target, duration, null);
    }


    public static ObjectAnimator animScaleOut(View target, long duration, final AnimListener listener) {
        return animScale(target, duration, listener, 1, 0);
    }

    public static ObjectAnimator animScale(View target, long duration, final AnimListener listener, float... floats) {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(Anim.SCALE_X, floats);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(Anim.SCALE_Y, floats);
        ObjectAnimator oa = ObjectAnimator.ofPropertyValuesHolder(target, scaleX, scaleY);
        return doAnim(target, oa, duration, new AccelerateDecelerateInterpolator(), 0, ValueAnimator.RESTART, listener);
    }

    public static ObjectAnimator animScaleInfinite(View target, long duration, final AnimListener listener, float... floats) {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(Anim.SCALE_X, floats);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(Anim.SCALE_Y, floats);
        ObjectAnimator oa = ObjectAnimator.ofPropertyValuesHolder(target, scaleX, scaleY);
        return doAnim(target, oa, duration, new AccelerateDecelerateInterpolator(),ValueAnimator.INFINITE , ValueAnimator.REVERSE, listener);
    }

    public static ObjectAnimator animAlphaIn(View target, long duration) {
        return animAlphaIn(target, duration, null);
    }

    /**
     * 淡入效果
     *
     * @param target 动画载体
     */
    public static ObjectAnimator animAlphaIn(View target, long duration, final AnimListener listener) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(target, Anim.ALPHA, 0, 1);
        return doAnim(target, oa, duration, new AccelerateDecelerateInterpolator(), 0, ValueAnimator.RESTART, listener);
    }

    public static ObjectAnimator animAlphaOut(View target, long duration) {
        return animAlphaOut(target, duration, null);
    }

    /**
     * 淡出效果
     *
     * @param target 动画载体
     */
    public static ObjectAnimator animAlphaOut(View target, long duration, final AnimListener listener) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(target, Anim.ALPHA, 1, 0);
        return doAnim(target, oa, duration, new AccelerateDecelerateInterpolator(), 0, ValueAnimator.RESTART, listener);
    }

    private static ObjectAnimator doAnim(final View target, ObjectAnimator animator, long duration
            , TimeInterpolator timeInterpolator, int repeatCount, int repeatMode, final AnimListener listener) {
        animator = baseOption(animator
                , duration
                , timeInterpolator
                , repeatCount
                , repeatMode
                , new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        target.setVisibility(View.VISIBLE);
                        if (listener != null) {
                            listener.onStart(animation, target);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (listener != null) {
                            listener.onEnd(animation, target);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        if (listener != null) {
                            listener.onCancel(animation, target);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        if (listener != null) {
                            listener.onRepeat(animation, target);
                        }
                    }
                }, new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (listener != null) {
                            listener.onUpdate(animation, target);
                        }
                    }
                });
        return animator;
    }


    private static ObjectAnimator baseOption(ObjectAnimator animator, long duration
            , TimeInterpolator timeInterpolator, int repeatCount, int repeatMode
            , Animator.AnimatorListener animatorListener
            , ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        animator.setDuration(duration);
        animator.setInterpolator(timeInterpolator);
        animator.setRepeatCount(repeatCount);
        animator.setRepeatMode(repeatMode);
        animator.addListener(animatorListener);
        animator.addUpdateListener(animatorUpdateListener);
        return animator;
    }
}
