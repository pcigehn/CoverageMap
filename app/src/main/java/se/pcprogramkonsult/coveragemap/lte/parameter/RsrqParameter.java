package se.pcprogramkonsult.coveragemap.lte.parameter;

import androidx.annotation.NonNull;

import se.pcprogramkonsult.coveragemap.model.CellMeasurement;

public class RsrqParameter implements MeasurementParameter {

    private static final int MIN_RSRQ = -20;
    private static final int MAX_RSRQ = -3;

    @Override
    public int getValue(@NonNull CellMeasurement cellMeasurement) {
        return cellMeasurement.getRsrq();
    }

    @Override
    public int getMinValue() {
        return MIN_RSRQ;
    }

    @Override
    public int getMaxValue() {
        return MAX_RSRQ;
    }

    @NonNull
    @Override
    public String getName() {
        return "RSRQ";
    }

    @NonNull
    @Override
    public String getUnit() {
        return " dB";
    }

    @Override
    public boolean isLogarithmicScale() {
        return false;
    }

    @Override
    public float logarithmicOffset() {
        return 0.0f;
    }

    @Override
    public boolean isInverseScale() {
        return false;
    }
}
