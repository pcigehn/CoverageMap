package se.pcprogramkonsult.coveragemap.lte.parameter;

import androidx.annotation.NonNull;

import se.pcprogramkonsult.coveragemap.model.CellMeasurement;

public class RsrpParameter implements MeasurementParameter {

    private static final int MIN_RSRP = -140;
    private static final int MAX_RSRP = -44;

    @Override
    public int getValue(@NonNull CellMeasurement cellMeasurement) {
        return cellMeasurement.getRsrp();
    }

    @Override
    public int getMinValue() {
        return MIN_RSRP;
    }

    @Override
    public int getMaxValue() {
        return MAX_RSRP;
    }

    @NonNull
    @Override
    public String getName() {
        return "RSRP";
    }

    @NonNull
    @Override
    public String getUnit() {
        return " dBm";
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
