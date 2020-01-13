package se.pcprogramkonsult.coveragemap.lte.operation;

import androidx.annotation.NonNull;

import java.util.List;

import se.pcprogramkonsult.coveragemap.db.entity.CellMeasurementEntity;
import se.pcprogramkonsult.coveragemap.lte.parameter.MeasurementParameter;

public class MaxOperation implements MeasurementOperation {
    @Override
    public int getResult(@NonNull List<CellMeasurementEntity> cellMeasurements, @NonNull MeasurementParameter measurementParameter) {
        final int minValue = measurementParameter.getMinValue();
        final int maxValue = measurementParameter.getMaxValue();
        int result = Integer.MIN_VALUE;
        for (CellMeasurementEntity cellMeasurement : cellMeasurements) {
            int value = measurementParameter.getValue(cellMeasurement);
            if (value >= minValue && value <= maxValue && value != Integer.MAX_VALUE) {
                result = Math.max(value, result);
            }
        }
        if (result == Integer.MIN_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return result;
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "Max";
    }
}
