package me.eugeniomarletti.renderthread;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import me.eugeniomarletti.renderthread.typeannotation.DisplayListCanvas;
import me.eugeniomarletti.renderthread.typeannotation.Property;
import me.eugeniomarletti.renderthread.typeannotation.RenderNodeAnimator;

final class RenderThreadMethods {

    private static final int MAX_SUPPORTED_ANDROID_VERSION = 25;
    private static final int MIN_SUPPORTED_ANDROID_VERSION = 21;

    @NonNull
    private final Class<?> displayListCanvas;
    @NonNull
    private final Method displayListCanvas_drawCircle;
    @NonNull
    private final Method displayListCanvas_drawRoundRect;

    @NonNull
    private final Method canvasProperty_createFloat;
    @NonNull
    private final Method canvasProperty_createPaint;

    @NonNull
    private final Constructor<Animator> renderNodeAnimator_float;
    @NonNull
    private final Constructor<Animator> renderNodeAnimator_paint;
    @NonNull
    private final Method renderNodeAnimator_setTarget;
    private final int renderNodeAnimator_paintField_strokeWidth;
    private final int renderNodeAnimator_paintField_alpha;

    private RenderThreadMethods(
            @NonNull Class<?> displayListCanvas,
            @NonNull Method displayListCanvas_drawCircle,
            @NonNull Method displayListCanvas_drawRoundRect,
            @NonNull Method canvasProperty_createFloat,
            @NonNull Method canvasProperty_createPaint,
            @NonNull Constructor<Animator> renderNodeAnimator_float,
            @NonNull Constructor<Animator> renderNodeAnimator_paint,
            @NonNull Method renderNodeAnimator_setTarget,
            int renderNodeAnimator_paintField_strokeWidth,
            int renderNodeAnimator_paintField_alpha) {

        this.displayListCanvas = displayListCanvas;
        this.displayListCanvas_drawCircle = displayListCanvas_drawCircle;
        this.displayListCanvas_drawRoundRect = displayListCanvas_drawRoundRect;
        this.canvasProperty_createFloat = canvasProperty_createFloat;
        this.canvasProperty_createPaint = canvasProperty_createPaint;
        this.renderNodeAnimator_float = renderNodeAnimator_float;
        this.renderNodeAnimator_paint = renderNodeAnimator_paint;
        this.renderNodeAnimator_setTarget = renderNodeAnimator_setTarget;
        this.renderNodeAnimator_paintField_strokeWidth = renderNodeAnimator_paintField_strokeWidth;
        this.renderNodeAnimator_paintField_alpha = renderNodeAnimator_paintField_alpha;
    }

    @Nullable
    static RenderThreadMethods create() {
        int sdk = Build.VERSION.SDK_INT;
        if (!isSupportedAndroidVersion(sdk)) {
            return null;
        }

        try {
            ClassLoader classLoader = RenderThreadMethods.class.getClassLoader();
            Class<?> displayListCanvas = loadDisplayListCanvasClassOrEquivalent(sdk, classLoader);
            Class<?> canvasProperty = loadCanvasPropertyClass(classLoader);
            Class<Animator> renderNodeAnimatorClass = loadRenderNodeAnimatorClass(classLoader);
            Method displayListCanvas_drawCircle = getDisplayListCanvasDrawCircleMethod(displayListCanvas, canvasProperty);
            Method displayListCanvas_drawRoundRect = getDisplayListCanvasDrawRoundRectMethod(displayListCanvas, canvasProperty);
            Method canvasProperty_createFloat = getCanvasPropertyCreateFloatMethod(canvasProperty);
            Method canvasProperty_createPaint = getCanvasPropertyCreatePaintMethod(canvasProperty);
            Constructor<Animator> renderNodeAnimator_float = getRenderNodeAnimatorFloatConstructor(canvasProperty, renderNodeAnimatorClass);
            Constructor<Animator> renderNodeAnimator_paint = getRenderNodeAnimatorPaintConstructor(canvasProperty, renderNodeAnimatorClass);
            Method renderNodeAnimator_setTarget = getRenderNodeAnimatorSetTargetMethod(renderNodeAnimatorClass);
            int renderNodeAnimator_paintField_strokeWidth = getRenderNodeAnimatorPaintStrokeWidthConstant(renderNodeAnimatorClass);
            int renderNodeAnimator_paintField_alpha = getRenderNodeAnimatorPaintAlphaConstant(renderNodeAnimatorClass);

            return new RenderThreadMethods(
                    displayListCanvas,
                    displayListCanvas_drawCircle,
                    displayListCanvas_drawRoundRect,
                    canvasProperty_createFloat,
                    canvasProperty_createPaint,
                    renderNodeAnimator_float,
                    renderNodeAnimator_paint,
                    renderNodeAnimator_setTarget,
                    renderNodeAnimator_paintField_strokeWidth,
                    renderNodeAnimator_paintField_alpha);
        } catch (Exception e) {
            Log.w("RenderThread", "Error while getting render thread methods.", e);
            return null;
        }
    }

    private static boolean isSupportedAndroidVersion(int sdk) {
        return sdk >= MIN_SUPPORTED_ANDROID_VERSION && sdk <= MAX_SUPPORTED_ANDROID_VERSION;
    }

    @NonNull
    private static Class<?> loadDisplayListCanvasClassOrEquivalent(int sdk, @NonNull ClassLoader classLoader) throws ClassNotFoundException {
        ClassNotFoundException error;
        if (sdk >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                return loadDisplayListCanvasClass(classLoader);
            } catch (ClassNotFoundException e) {
                error = e;
            }
            try {
                return loadGLES20CanvasClass(classLoader);
            } catch (ClassNotFoundException ignore) {
            }
        } else {
            try {
                return loadGLES20CanvasClass(classLoader);
            } catch (ClassNotFoundException e) {
                error = e;
            }
            try {
                return loadDisplayListCanvasClass(classLoader);
            } catch (ClassNotFoundException ignore) {
            }
        }
        throw error;
    }

    @NonNull
    @SuppressLint("PrivateApi") // This is a hack, but we guard as much as possible against issues
    private static Class<?> loadDisplayListCanvasClass(@NonNull ClassLoader classLoader) throws ClassNotFoundException {
        return classLoader.loadClass("android.view.DisplayListCanvas");
    }

    @NonNull
    @SuppressLint("PrivateApi") // This is a hack, but we guard as much as possible against issues
    private static Class<?> loadGLES20CanvasClass(@NonNull ClassLoader classLoader) throws ClassNotFoundException {
        return classLoader.loadClass("android.view.GLES20Canvas");
    }

    @NonNull
    @SuppressLint("PrivateApi") // This is a hack, but we guard as much as possible against issues
    private static Class<?> loadCanvasPropertyClass(@NonNull ClassLoader classLoader) throws ClassNotFoundException {
        return classLoader.loadClass("android.graphics.CanvasProperty");
    }

    @NonNull
    @SuppressLint("PrivateApi") // This is a hack, but we guard as much as possible against issues
    private static Class<Animator> loadRenderNodeAnimatorClass(@NonNull ClassLoader classLoader) throws ClassNotFoundException {
        return castClass(classLoader.loadClass("android.view.RenderNodeAnimator"), Animator.class);
    }

    @NonNull
    @SuppressWarnings("unchecked") // We have to do an unsafe cast here because we have no type informations
    private static <T> Class<T> castClass(@NonNull Class<?> originalClass, @NonNull Class<T> targetClass) {
        if (!targetClass.isAssignableFrom(originalClass)) {
            throw new ClassCastException(String.format("Cannot cast class %s to %s.", originalClass, targetClass));
        }
        return (Class<T>) originalClass;
    }

    @NonNull
    private static Method getDisplayListCanvasDrawCircleMethod(@NonNull Class<?> displayListCanvas, @NonNull Class<?> canvasProperty)
            throws NoSuchMethodException {

        Method displayListCanvas_drawCircle = displayListCanvas.getDeclaredMethod(
                "drawCircle", canvasProperty, canvasProperty, canvasProperty, canvasProperty);
        displayListCanvas_drawCircle.setAccessible(true);
        return displayListCanvas_drawCircle;
    }

    @NonNull
    private static Method getDisplayListCanvasDrawRoundRectMethod(@NonNull Class<?> displayListCanvas, @NonNull Class<?> canvasProperty)
            throws NoSuchMethodException {

        Method displayListCanvas_drawRoundRect = displayListCanvas.getDeclaredMethod(
                "drawRoundRect", canvasProperty, canvasProperty, canvasProperty, canvasProperty, canvasProperty, canvasProperty, canvasProperty);
        displayListCanvas_drawRoundRect.setAccessible(true);
        return displayListCanvas_drawRoundRect;
    }

    @NonNull
    private static Method getCanvasPropertyCreateFloatMethod(@NonNull Class<?> canvasProperty) throws NoSuchMethodException {
        Method canvasProperty_createFloat = canvasProperty.getDeclaredMethod("createFloat", float.class);
        canvasProperty_createFloat.setAccessible(true);
        return canvasProperty_createFloat;
    }

    @NonNull
    private static Method getCanvasPropertyCreatePaintMethod(@NonNull Class<?> canvasProperty) throws NoSuchMethodException {
        Method canvasProperty_createPaint = canvasProperty.getDeclaredMethod("createPaint", Paint.class);
        canvasProperty_createPaint.setAccessible(true);
        return canvasProperty_createPaint;
    }

    @NonNull
    private static Constructor<Animator> getRenderNodeAnimatorFloatConstructor(
            @NonNull Class<?> canvasProperty, @NonNull Class<Animator> renderNodeAnimatorClass) throws NoSuchMethodException {

        Constructor<Animator> renderNodeAnimator_float = renderNodeAnimatorClass.getConstructor(canvasProperty, float.class);
        renderNodeAnimator_float.setAccessible(true);
        return renderNodeAnimator_float;
    }

    @NonNull
    private static Constructor<Animator> getRenderNodeAnimatorPaintConstructor(
            @NonNull Class<?> canvasProperty, @NonNull Class<Animator> renderNodeAnimatorClass) throws NoSuchMethodException {

        Constructor<Animator> renderNodeAnimator_paint = renderNodeAnimatorClass.getConstructor(canvasProperty, int.class, float.class);
        renderNodeAnimator_paint.setAccessible(true);
        return renderNodeAnimator_paint;
    }

    @NonNull
    private static Method getRenderNodeAnimatorSetTargetMethod(@NonNull Class<Animator> renderNodeAnimatorClass) throws NoSuchMethodException {
        Method renderNodeAnimator_setTarget = renderNodeAnimatorClass.getDeclaredMethod("setTarget", Canvas.class);
        renderNodeAnimator_setTarget.setAccessible(true);
        return renderNodeAnimator_setTarget;
    }

    private static int getRenderNodeAnimatorPaintStrokeWidthConstant(
            @NonNull Class<Animator> renderNodeAnimatorClass) throws NoSuchFieldException, IllegalAccessException {

        return getStaticIntConstant(renderNodeAnimatorClass, "PAINT_STROKE_WIDTH");
    }

    private static int getRenderNodeAnimatorPaintAlphaConstant(
            @NonNull Class<Animator> renderNodeAnimatorClass) throws NoSuchFieldException, IllegalAccessException {

        return getStaticIntConstant(renderNodeAnimatorClass, "PAINT_ALPHA");
    }

    private static int getStaticIntConstant(@NonNull Class<Animator> klass, @NonNull String fieldName)
            throws NoSuchFieldException, IllegalAccessException {

        Field constant = klass.getDeclaredField(fieldName);
        constant.setAccessible(true);
        return constant.getInt(null);
    }

    @NonNull
    @Property(Float.class)
    public Object createCanvasProperty(float initialValue) {
        try {
            return canvasProperty_createFloat.invoke(null, initialValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @Property(Paint.class)
    public Object createCanvasProperty(@NonNull Paint initialValue) {
        try {
            return canvasProperty_createPaint.invoke(null, initialValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean instanceOfDisplayListCanvas(@Nullable Canvas canvas) {
        return displayListCanvas.isInstance(canvas);
    }

    public void drawCircle(
            @DisplayListCanvas @NonNull Canvas canvas,
            @Property(Float.class) @NonNull Object cx,
            @Property(Float.class) @NonNull Object cy,
            @Property(Float.class) @NonNull Object radius,
            @Property(Paint.class) @NonNull Object paint
    ) {
        try {
            displayListCanvas_drawCircle.invoke(canvas, cx, cy, radius, paint);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void drawRoundRect(
            @DisplayListCanvas @NonNull Canvas canvas,
            @Property(Float.class) @NonNull Object left,
            @Property(Float.class) @NonNull Object top,
            @Property(Float.class) @NonNull Object right,
            @Property(Float.class) @NonNull Object bottom,
            @Property(Float.class) @NonNull Object rx,
            @Property(Float.class) @NonNull Object ry,
            @Property(Paint.class) @NonNull Object paint
    ) {
        try {
            displayListCanvas_drawRoundRect.invoke(canvas, left, top, right, bottom, rx, ry, paint);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @RenderNodeAnimator
    public Animator createFloatRenderNodeAnimator(@Property(Float.class) @NonNull Object canvasProperty, float targetValue) {
        try {
            return renderNodeAnimator_float.newInstance(canvasProperty, targetValue);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @RenderNodeAnimator
    public Animator createPaintAlphaRenderNodeAnimator(
            @Property(Paint.class) @NonNull Object canvasProperty,
            @FloatRange(from = 0f, to = 255f) float targetValue
    ) {
        try {
            return renderNodeAnimator_paint.newInstance(canvasProperty, renderNodeAnimator_paintField_alpha, targetValue);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @RenderNodeAnimator
    public Animator createPaintStrokeWidthRenderNodeAnimator(@Property(Paint.class) @NonNull Object canvasProperty, float targetValue) {
        try {
            return renderNodeAnimator_paint.newInstance(canvasProperty, renderNodeAnimator_paintField_strokeWidth, targetValue);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTarget(@RenderNodeAnimator @NonNull Animator animator, @DisplayListCanvas @NonNull Canvas target) {
        try {
            renderNodeAnimator_setTarget.invoke(animator, target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
