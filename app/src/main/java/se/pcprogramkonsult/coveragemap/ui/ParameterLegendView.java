package se.pcprogramkonsult.coveragemap.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import se.pcprogramkonsult.coveragemap.lte.parameter.MeasurementParameter;

public class ParameterLegendView extends View {
    private int w;
    private int h;
    @Nullable
    private int[] colors = null;
    @Nullable
    private Bitmap bm = null;
    @Nullable
    private MeasurementParameter parameter = null;

    public ParameterLegendView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (bm == null) {
            bm = getBitmap();
        }
        if (bm != null) {
            canvas.drawBitmap(bm, 0, 0, null);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.w = w;
        this.h = h;
        this.colors = new int[w * h];
        this.bm = getBitmap();
    }

    @Nullable
    private Bitmap getBitmap() {
        if (colors == null) {
            return null;
        }

        int color = Color.TRANSPARENT;
        for (int i = 0; i < w; i++) {
            if (parameter != null) {
                int value = parameter.getMinValue() + i * (parameter.getMaxValue() - parameter.getMinValue() + 1) / w;
                color = ColorScale.getColor(value, parameter);
            }
            for (int j = 0; j < h; j++) {
                int index = j * w + i;
                colors[index] = color;
            }
        }
        return Bitmap.createBitmap(colors, w, h,  Bitmap.Config.ARGB_8888);
    }

    public void setParameter(MeasurementParameter parameter) {
        this.parameter = parameter;
        this.bm = getBitmap();
        invalidate();
    }
}
