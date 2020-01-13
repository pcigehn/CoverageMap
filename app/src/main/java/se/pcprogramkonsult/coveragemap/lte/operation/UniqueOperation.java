package se.pcprogramkonsult.coveragemap.lte.operation;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.pcprogramkonsult.coveragemap.db.entity.CellMeasurementEntity;
import se.pcprogramkonsult.coveragemap.lte.parameter.MeasurementParameter;

public class UniqueOperation implements MeasurementOperation {
    @Override
    public int getResult(@NonNull List<CellMeasurementEntity> cellMeasurements, @NonNull MeasurementParameter measurementParameter) {
        final Set<Integer> uniqueValues = new HashSet<>();
        for (CellMeasurementEntity cellMeasurement : cellMeasurements) {
            int value = measurementParameter.getValue(cellMeasurement);
            if (value != Integer.MAX_VALUE) {
                uniqueValues.add(value);
            }
        }
        final int minValue = measurementParameter.getMinValue();
        final int maxValue = measurementParameter.getMaxValue();
        int result = uniqueValues.size();
        if (result < minValue) {
            result = Integer.MAX_VALUE;
        } else if (result > maxValue) {
            result = maxValue;
        }
        return result;
    }

    @NonNull
    @Override
    public String getName() {
        return "Unique";
    }
}
