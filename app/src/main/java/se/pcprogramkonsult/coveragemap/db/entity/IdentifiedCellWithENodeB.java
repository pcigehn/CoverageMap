package se.pcprogramkonsult.coveragemap.db.entity;

import androidx.room.Embedded;

public class IdentifiedCellWithENodeB {
    @Embedded
    public IdentifiedCellEntity identifiedCell;

    @Embedded
    public ENodeBEntity eNodeB;
}
