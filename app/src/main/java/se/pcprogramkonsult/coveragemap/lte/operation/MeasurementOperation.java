package se.pcprogramkonsult.coveragemap.lte.operation;

import androidx.annotation.NonNull;

import java.util.List;

import se.pcprogramkonsult.coveragemap.db.entity.CellMeasurementEntity;
import se.pcprogramkonsult.coveragemap.lte.parameter.MeasurementParameter;

public interface MeasurementOperation {
    int getResult(List<CellMeasurementEntity> cellMeasurementEntities, MeasurementParameter measurementParameter);
    @NonNull
    String getName();
}
