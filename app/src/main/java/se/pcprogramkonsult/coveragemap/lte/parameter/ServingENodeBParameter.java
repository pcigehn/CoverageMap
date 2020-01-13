package se.pcprogramkonsult.coveragemap.lte.parameter;

import androidx.annotation.NonNull;

import se.pcprogramkonsult.coveragemap.model.CellMeasurement;

public class ServingENodeBParameter implements MeasurementParameter {

    @Override
    public int getValue(@NonNull CellMeasurement cellMeasurement) {
        if (cellMeasurement.isRegistered()) {
            return cellMeasurement.getENodeB();
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMinValue() {
        return 1;
    }

    @Override
    public int getMaxValue() {
        return 6;
    }

    @NonNull
    @Override
    public String getName() {
        return "S-eNodeB";
    }

    @NonNull
    @Override
    public String getUnit() {
        return "";
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
