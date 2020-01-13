package se.pcprogramkonsult.coveragemap.ui;

import android.graphics.Color;

import androidx.annotation.NonNull;

import se.pcprogramkonsult.coveragemap.lte.parameter.MeasurementParameter;

class ColorScale {

    static int getColor(final int value, @NonNull final MeasurementParameter parameter) {
        if (value == Integer.MAX_VALUE) {
            return Color.TRANSPARENT;
        }
        float adjustedValue = value;
        float min = parameter.getMinValue();
        float max = parameter.getMaxValue();
        adjustedValue = Math.min(Math.max(adjustedValue, min), max);
        if (parameter.isLogarithmicScale()) {
            float offset = parameter.logarithmicOffset();
            adjustedValue = (float) Math.log(adjustedValue + offset);
            min = (float) Math.log(min + offset);
            max = (float) Math.log(max + offset);
        }
        float[] blueHsv = new float[3];
        Color.colorToHSV(Color.BLUE, blueHsv);
        float[] redHsv = new float[3];
        Color.colorToHSV(Color.RED, redHsv);
        final float hue;
        if (parameter.isInverseScale()) {
            hue = redHsv[0] + (blueHsv[0] - redHsv[0]) * (adjustedValue - min) / (max - min);
        } else {
            hue = blueHsv[0] + (redHsv[0] - blueHsv[0]) * (adjustedValue - min) / (max - min);
        }
        return Color.HSVToColor(0x88, new float[]{hue, 1.0f, 1.0f});
    }

}
