package se.pcprogramkonsult.coveragemap.lte.parameter;

import androidx.annotation.NonNull;

import se.pcprogramkonsult.coveragemap.model.CellMeasurement;

public interface MeasurementParameter {
    int getValue(CellMeasurement cellMeasurement);
    int getMinValue();
    int getMaxValue();
    @NonNull
    String getName();
    @NonNull
    String getUnit();
    boolean isLogarithmicScale();
    float logarithmicOffset();
    boolean isInverseScale();
}
