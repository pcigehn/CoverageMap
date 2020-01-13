package se.pcprogramkonsult.coveragemap.lte.operation;

import androidx.annotation.NonNull;

import java.util.List;

import se.pcprogramkonsult.coveragemap.db.entity.CellMeasurementEntity;
import se.pcprogramkonsult.coveragemap.lte.parameter.MeasurementParameter;

public class MinOperation implements MeasurementOperation {
    @Override
    public int getResult(@NonNull List<CellMeasurementEntity> cellMeasurements, @NonNull MeasurementParameter measurementParameter) {
        final int minValue = measurementParameter.getMinValue();
        final int maxValue = measurementParameter.getMaxValue();
        int result = Integer.MAX_VALUE;
        for (CellMeasurementEntity cellMeasurement : cellMeasurements) {
            int value = measurementParameter.getValue(cellMeasurement);
            if (value >= minValue && value <= maxValue && value != Integer.MAX_VALUE) {
                result = Math.min(value, result);
            }
        }
        return result;
    }

    @NonNull
    @Override
    public String getName() {
        return "Min";
    }
}
