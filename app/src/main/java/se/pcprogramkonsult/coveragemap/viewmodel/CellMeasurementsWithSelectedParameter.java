package se.pcprogramkonsult.coveragemap.viewmodel;

import java.util.List;

import se.pcprogramkonsult.coveragemap.db.entity.CellMeasurementEntity;
import se.pcprogramkonsult.coveragemap.lte.parameter.MeasurementParameter;

public class CellMeasurementsWithSelectedParameter {
    public final MeasurementParameter parameter;
    public final List<CellMeasurementEntity> cellMeasurements;

    CellMeasurementsWithSelectedParameter(
            MeasurementParameter parameter,
            List<CellMeasurementEntity> cellMeasurements) {
        this.parameter = parameter;
        this.cellMeasurements = cellMeasurements;
    }
}
