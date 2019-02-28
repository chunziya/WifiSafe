package com.administrator.wifisafe.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author lesences  2018/5/26
 */
public class ProgressDrawable extends Drawable {
    private final Paint paint;
    private final int ringWidth;

    private float progress;
    private int ringColor;
    private int centerColor;

    public ProgressDrawable(int ringWidth, @ColorInt int ringColor, @ColorInt int centerColor) {
        this.progress = 0.f;
        this.ringColor = ringColor;
        this.centerColor = centerColor;

        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.paint.setAntiAlias(true);
        this.ringWidth = ringWidth;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        final Rect bounds = getBounds();

        int size = Math.min(bounds.height(), bounds.width());
        int maxRadius = size / 2;
        int ringRadius = maxRadius - (ringWidth / 2);

        float centerRadius = progress * maxRadius;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(centerColor);
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), centerRadius, paint);

        paint.setColor(ringColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(ringWidth);
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), ringRadius, paint);

    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return 1 - paint.getAlpha();
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidateSelf();
    }
}
