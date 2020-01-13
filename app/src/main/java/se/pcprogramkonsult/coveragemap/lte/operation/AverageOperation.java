package se.pcprogramkonsult.coveragemap.lte.operation;

import androidx.annotation.NonNull;

import java.util.List;

import se.pcprogramkonsult.coveragemap.db.entity.CellMeasurementEntity;
import se.pcprogramkonsult.coveragemap.lte.parameter.MeasurementParameter;

public class AverageOperation implements MeasurementOperation {
    @Override
    public int getResult(@NonNull List<CellMeasurementEntity> cellMeasurements, @NonNull MeasurementParameter measurementParameter) {
        final int minValue = measurementParameter.getMinValue();
        final int maxValue = measurementParameter.getMaxValue();
        int count = 0;
        double sum = 0.0;
        for (CellMeasurementEntity cellMeasurement : cellMeasurements) {
            int value = measurementParameter.getValue(cellMeasurement);
            if (value >= minValue && value <= maxValue && value != Integer.MAX_VALUE) {
                count += 1;
                sum += value;
            }
        }
        if (count == 0) {
            return Integer.MAX_VALUE;
        } else {
            return (int)(sum / count);
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "Average";
    }
}
