package me.eugeniomarletti.renderthread;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;

final class CanvasCompat {

    private CanvasCompat() {
        // Not instantiable
    }

    private static final RectF TMP_RECT = new RectF();

    static void drawRoundRect(
            @NonNull Canvas canvas,
            float left,
            float top,
            float right,
            float bottom,
            float rx,
            float ry,
            @NonNull Paint paint
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            TMP_RECT.set(left, top, right, bottom);
            canvas.drawRoundRect(TMP_RECT, rx, ry, paint);
        } else {
            canvas.drawRoundRect(left, top, right, bottom, rx, ry, paint);
        }
    }
}
