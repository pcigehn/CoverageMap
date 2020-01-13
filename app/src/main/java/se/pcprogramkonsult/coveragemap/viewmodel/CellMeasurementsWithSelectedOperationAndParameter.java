package se.pcprogramkonsult.coveragemap.viewmodel;

import java.util.List;

import se.pcprogramkonsult.coveragemap.db.entity.CellMeasurementEntity;
import se.pcprogramkonsult.coveragemap.lte.operation.MeasurementOperation;
import se.pcprogramkonsult.coveragemap.lte.parameter.MeasurementParameter;

public class CellMeasurementsWithSelectedOperationAndParameter {
    public final MeasurementOperation operation;
    public final MeasurementParameter parameter;
    public final List<CellMeasurementEntity> cellMeasurements;

    CellMeasurementsWithSelectedOperationAndParameter(
            MeasurementOperation operation,
            MeasurementParameter parameter,
            List<CellMeasurementEntity> cellMeasurements) {
        this.operation = operation;
        this.parameter = parameter;
        this.cellMeasurements = cellMeasurements;
    }
}
