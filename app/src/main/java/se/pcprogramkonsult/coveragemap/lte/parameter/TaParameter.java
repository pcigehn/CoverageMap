package se.pcprogramkonsult.coveragemap.lte.parameter;

import androidx.annotation.NonNull;

import se.pcprogramkonsult.coveragemap.model.CellMeasurement;

public class TaParameter implements MeasurementParameter {

    private static final int MIN_TA = 0;
    private static final int MAX_TA = 1282;

    @Override
    public int getValue(@NonNull CellMeasurement cellMeasurement) {
        return cellMeasurement.getTa();
    }

    @Override
    public int getMinValue() {
        return MIN_TA;
    }

    @Override
    public int getMaxValue() {
        return MAX_TA;
    }

    @NonNull
    @Override
    public String getName() {
        return "TA";
    }

    @NonNull
    @Override
    public String getUnit() {
        return "";
    }

    @Override
    public boolean isLogarithmicScale() {
        return true;
    }

    @Override
    public float logarithmicOffset() {
        return 0.8f;
    }

    @Override
    public boolean isInverseScale() {
        return true;
    }
}
