package se.pcprogramkonsult.coveragemap.lte.parameter;

import androidx.annotation.NonNull;

import se.pcprogramkonsult.coveragemap.lte.IdUtil;
import se.pcprogramkonsult.coveragemap.model.CellMeasurement;

public class CiParameter implements MeasurementParameter {

    @Override
    public int getValue(@NonNull CellMeasurement cellMeasurement) {
        return IdUtil.getCi(cellMeasurement.getENodeB(), cellMeasurement.getCid());
    }

    @Override
    public int getMinValue() {
        return 1;
    }

    @Override
    public int getMaxValue() {
        return 40;
    }

    @NonNull
    @Override
    public String getName() {
        return "CI";
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
