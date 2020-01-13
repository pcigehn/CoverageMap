package se.pcprogramkonsult.coveragemap.lte.operation;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.pcprogramkonsult.coveragemap.db.entity.CellMeasurementEntity;
import se.pcprogramkonsult.coveragemap.lte.parameter.MeasurementParameter;

public class MedianOperation implements MeasurementOperation {
    @Override
    public int getResult(@NonNull List<CellMeasurementEntity> cellMeasurements, @NonNull MeasurementParameter measurementParameter) {
        final int minValue = measurementParameter.getMinValue();
        final int maxValue = measurementParameter.getMaxValue();
        List<Integer> values = new ArrayList<>();
        for (CellMeasurementEntity cellMeasurement : cellMeasurements) {
            int value = measurementParameter.getValue(cellMeasurement);
            if (value >= minValue && value <= maxValue && value != Integer.MAX_VALUE) {
                values.add(value);
            }
        }
        Collections.sort(values);
        int count = values.size();
        if (count == 0) {
            return Integer.MAX_VALUE;
        } else if (count % 2 == 0){
            return (values.get(count / 2) + values.get(count / 2 - 1)) / 2;
        } else {
            return values.get(count / 2);
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "Median";
    }
}
