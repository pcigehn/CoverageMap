package se.pcprogramkonsult.coveragemap.viewmodel;

import java.util.List;

import se.pcprogramkonsult.coveragemap.db.entity.HandoverEntity;

public class HandoverEntitiesWithSelectedCi {
    public final Integer selectedCi;
    public final List<HandoverEntity> handoverEntities;
    public final Float zoomLevel;

    HandoverEntitiesWithSelectedCi(Integer selectedCi, List<HandoverEntity> handoverEntities, Float zoomLevel) {
        this.selectedCi = selectedCi;
        this.handoverEntities = handoverEntities;
        this.zoomLevel = zoomLevel;
    }
}
