package me.eugeniomarletti.renderthread;

import android.animation.Animator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import me.eugeniomarletti.renderthread.typeannotation.DisplayListCanvas;
import me.eugeniomarletti.renderthread.typeannotation.RenderNodeAnimator;

@SuppressWarnings("unused")     // All unused methods are just public APIs
public final class RenderThread {

    private static RenderThreadDelegate DELEGATE;

    static {
        init();
    }

    private static void init() {
        RenderThreadDelegate delegate = DELEGATE;
        if (delegate == null || !delegate.isSupported()) {
            RenderThreadMethods methods = RenderThreadMethods.create(false);
            DELEGATE = methods != null
                    ? new RenderThreadDelegateHw(methods)
                    : new RenderThreadDelegate();
        }
    }

    public static boolean isSupported() {
        return DELEGATE.isSupported();
    }

    private RenderThread() {
        // Not instantiable
    }

    @NonNull
    public static CanvasProperty<Float> createCanvasProperty(
            @NonNull Canvas canvas,
            float initialValue
    ) {
        return createCanvasProperty(canvas, initialValue, true);
    }

    @NonNull
    public static CanvasProperty<Float> createCanvasProperty(
            @NonNull Canvas canvas,
            float initialValue,
            boolean useRenderThread
    ) {
        return DELEGATE.createCanvasProperty(canvas, initialValue, useRenderThread);
    }

    @NonNull
    public static CanvasProperty<Paint> createCanvasProperty(
            @NonNull Canvas canvas,
            @NonNull Paint initialValue
    ) {
        return createCanvasProperty(canvas, initialValue, true);
    }

    @NonNull
    public static CanvasProperty<Paint> createCanvasProperty(
            @NonNull Canvas canvas,
            @NonNull Paint initialValue,
            boolean useRenderThread
    ) {
        return DELEGATE.createCanvasProperty(canvas, initialValue, useRenderThread);
    }

    public static boolean isDisplayListCanvas(@NonNull Canvas canvas) {
        return DELEGATE.isDisplayListCanvas(canvas);
    }

    public static void setAnimatorTarget(
            @RenderNodeAnimator @NonNull Animator animator,
            @DisplayListCanvas @NonNull Canvas target
    ) {
        DELEGATE.setTarget(animator, target);
    }

    public static void drawCircle(
            @NonNull Canvas canvas,
            @NonNull CanvasProperty<Float> cx,
            @NonNull CanvasProperty<Float> cy,
            @NonNull CanvasProperty<Float> radius,
            @NonNull CanvasProperty<Paint> paint
    ) {
        DELEGATE.drawCircle(canvas, cx, cy, radius, paint);
    }

    public static void drawRoundRect(
            @NonNull Canvas canvas,
            @NonNull CanvasProperty<Float> left,
            @NonNull CanvasProperty<Float> top,
            @NonNull CanvasProperty<Float> right,
            @NonNull CanvasProperty<Float> bottom,
            @NonNull CanvasProperty<Float> rx,
            @NonNull CanvasProperty<Float> ry,
            @NonNull CanvasProperty<Paint> paint
    ) {
        DELEGATE.drawRoundRect(canvas, left, top, right, bottom, rx, ry, paint);
    }

    @NonNull
    public static Animator createFloatAnimator(
            @NonNull View targetView,
            @NonNull Canvas canvas,
            @NonNull CanvasProperty<Float> property,
            float targetValue
    ) {
        return DELEGATE.createFloatAnimator(targetView, canvas, property, targetValue);
    }

    @NonNull
    public static Animator createPaintAlphaAnimator(
            @Nullable View targetView,
            @NonNull Canvas canvas,
            @NonNull CanvasProperty<Paint> property,
            @FloatRange(from = 0f, to = 255f) float targetValue
    ) {
        return DELEGATE.createPaintAlphaAnimator(targetView, canvas, property, targetValue);
    }

    @NonNull
    public static Animator createPaintStrokeWidthAnimator(
            @Nullable View targetView,
            @NonNull Canvas canvas,
            @NonNull CanvasProperty<Paint> property,
            float targetValue
    ) {
        return DELEGATE.createPaintStrokeWidthAnimator(targetView, canvas, property, targetValue);
    }
}
