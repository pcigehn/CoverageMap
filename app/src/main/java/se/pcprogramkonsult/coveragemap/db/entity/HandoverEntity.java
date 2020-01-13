package se.pcprogramkonsult.coveragemap.db.entity;

import android.telephony.CellIdentityLte;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Locale;

import se.pcprogramkonsult.coveragemap.lte.Earfcn;
import se.pcprogramkonsult.coveragemap.lte.IdUtil;
import se.pcprogramkonsult.coveragemap.model.Handover;

@Entity(tableName="handovers", indices = {@Index(value = "coord")})
public class HandoverEntity implements Handover {
    @PrimaryKey(autoGenerate = true)
    private long id = 0;

    private final long coord;

    private final int sourceENodeB;
    private final int sourceCid;
    private final int sourceEarfcn;
    private final int sourcePci;
    private final int sourceRsrp;
    private final int sourceRsrq;

    private final int targetENodeB;
    private final int targetCid;
    private final int targetEarfcn;
    private final int targetPci;
    private final int targetRsrp;
    private final int targetRsrq;

    public HandoverEntity(long id, long coord, int sourceENodeB, int sourceCid, int sourceEarfcn, int sourcePci, int sourceRsrp, int sourceRsrq, int targetENodeB, int targetCid, int targetEarfcn, int targetPci, int targetRsrp, int targetRsrq) {
        this.id = id;
        this.coord = coord;
        this.sourceENodeB = sourceENodeB;
        this.sourceCid = sourceCid;
        this.sourceEarfcn = sourceEarfcn;
        this.sourcePci = sourcePci;
        this.sourceRsrp = sourceRsrp;
        this.sourceRsrq = sourceRsrq;
        this.targetENodeB = targetENodeB;
        this.targetCid = targetCid;
        this.targetEarfcn = targetEarfcn;
        this.targetPci = targetPci;
        this.targetRsrp = targetRsrp;
        this.targetRsrq = targetRsrq;

        Earfcn.addUnique(sourceEarfcn);
        Earfcn.addUnique(targetEarfcn);
    }

    @Ignore
    public HandoverEntity(long coord, @NonNull CellInfoLte currentServingCell, @NonNull CellInfoLte previousServingCell) {
        this.coord = coord;

        final CellIdentityLte currentCellIdentity = currentServingCell.getCellIdentity();
        final int currentCi = currentCellIdentity.getCi();
        this.targetENodeB = IdUtil.getENodeB(currentCi);
        this.targetCid = IdUtil.getCid(currentCi);
        this.targetEarfcn = currentCellIdentity.getEarfcn();
        this.targetPci = currentCellIdentity.getPci();
        final CellSignalStrengthLte currentCellSignalStrength = currentServingCell.getCellSignalStrength();
        this.targetRsrp = currentCellSignalStrength.getRsrp();
        this.targetRsrq = currentCellSignalStrength.getRsrq();

        final CellIdentityLte previousCellIdentity = previousServingCell.getCellIdentity();
        final int previousCi = previousCellIdentity.getCi();
        this.sourceENodeB = IdUtil.getENodeB(previousCi);
        this.sourceCid = IdUtil.getCid(previousCi);
        this.sourceEarfcn = previousCellIdentity.getEarfcn();
        this.sourcePci = previousCellIdentity.getPci();
        final CellSignalStrengthLte previousCellSignalStrength = previousServingCell.getCellSignalStrength();
        this.sourceRsrp = previousCellSignalStrength.getRsrp();
        this.sourceRsrq = previousCellSignalStrength.getRsrq();

        Earfcn.addUnique(sourceEarfcn);
        Earfcn.addUnique(targetEarfcn);
    }

    @SuppressWarnings("unused")
    @Override
    public long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    @Override
    public long getCoord() {
        return coord;
    }

    public int getSourceCi() {
        return IdUtil.getCi(sourceENodeB, sourceCid);
    }

    @Override
    public int getSourceENodeB() {
        return sourceENodeB;
    }

    @Override
    public int getSourceCid() {
        return sourceCid;
    }

    @Override
    public int getSourceEarfcn() {
        return sourceEarfcn;
    }

    @SuppressWarnings("unused")
    @Override
    public int getSourcePci() {
        return sourcePci;
    }

    @SuppressWarnings("unused")
    @Override
    public int getSourceRsrp() {
        return sourceRsrp;
    }

    @SuppressWarnings("unused")
    @Override
    public int getSourceRsrq() {
        return sourceRsrq;
    }

    public int getTargetCi() {
        return IdUtil.getCi(targetENodeB, targetCid);
    }

    @Override
    public int getTargetENodeB() {
        return targetENodeB;
    }

    @Override
    public int getTargetCid() {
        return targetCid;
    }

    @Override
    public int getTargetEarfcn() {
        return targetEarfcn;
    }

    @SuppressWarnings("unused")
    @Override
    public int getTargetPci() {
        return targetPci;
    }

    @SuppressWarnings("unused")
    @Override
    public int getTargetRsrp() {
        return targetRsrp;
    }

    @SuppressWarnings("unused")
    @Override
    public int getTargetRsrq() {
        return targetRsrq;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,
                "HandoverEntity{id=%d coord=%d " +
                        "sourceENodeB=%d sourceCid=%d sourceEarfcn=%d sourcePci=%d sourceRsrp=%d sourceRsrq=%d " +
                        "targetENodeB=%d targetCid=%d targetEarfcn=%d targetPci=%d targetRsrp=%d targetRsrq=%d}",
                id, coord,
                sourceENodeB, sourceCid, sourceEarfcn, sourcePci, sourceRsrp, sourceRsrq,
                targetENodeB, targetCid, targetEarfcn, targetPci, targetRsrp, targetRsrq);
    }

}
