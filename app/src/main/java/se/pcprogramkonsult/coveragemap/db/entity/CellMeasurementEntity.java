package se.pcprogramkonsult.coveragemap.db.entity;

import android.telephony.CellIdentityLte;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Locale;

import se.pcprogramkonsult.coveragemap.core.ReflectionUtil;
import se.pcprogramkonsult.coveragemap.lte.Earfcn;
import se.pcprogramkonsult.coveragemap.lte.IdUtil;
import se.pcprogramkonsult.coveragemap.model.CellMeasurement;

@Entity(tableName = "cell_measurements",
        foreignKeys = {
                @ForeignKey(entity = LocationMeasurementEntity.class,
                        parentColumns = "id",
                        childColumns = "locationId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {
                @Index(value = "locationId"),
                @Index(value = {"coord", "eNodeB", "cid"}),
                @Index(value = {"coord", "earfcn", "eNodeB"})})
public class CellMeasurementEntity implements CellMeasurement {
    @PrimaryKey(autoGenerate = true)
    private long id = 0;

    private final long locationId;

    @Nullable
    private Long coord;

    private final boolean registered;

    private int eNodeB;
    private int cid;
    private final int earfcn;
    private final int pci;
    private final int tac;

    private final int rsrp;
    private final int rsrq;
    private final int ta;
    private final int rssi;
    private final int snr;

    public CellMeasurementEntity(final long id, final long locationId, @Nullable final Long coord,
                                 final boolean registered,
                                 final int eNodeB, final int cid, final int earfcn, final int pci,
                                 final int tac, final int rsrp, final int rsrq,
                                 final int ta, final int rssi, final int snr) {
        this.id = id;
        this.locationId = locationId;
        this.coord = coord;
        this.registered = registered;
        this.eNodeB = eNodeB;
        this.cid = cid;
        this.earfcn = earfcn;
        this.pci = pci;
        this.tac = tac;
        this.rsrp = rsrp;
        this.rsrq = rsrq;
        this.ta = ta;
        this.rssi = rssi;
        this.snr = snr;

        Earfcn.addUnique(earfcn);
    }

    @Ignore
    public CellMeasurementEntity(final long locationId, @Nullable final Long coord,
                                 @NonNull final CellInfoLte cellInfoLte, final int servingCellSnr) {
        this.locationId = locationId;
        this.coord = coord;

        this.registered = cellInfoLte.isRegistered();

        final CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
        int ci = cellIdentityLte.getCi();
        this.eNodeB = IdUtil.getENodeB(ci);
        this.cid = IdUtil.getCid(ci);
        this.earfcn = cellIdentityLte.getEarfcn();
        this.pci = cellIdentityLte.getPci();
        this.tac = cellIdentityLte.getTac();

        final CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
        this.rsrp = cellSignalStrengthLte.getRsrp();
        this.rsrq = cellSignalStrengthLte.getRsrq();
        this.ta = cellSignalStrengthLte.getTimingAdvance();

        int asu = ReflectionUtil.getField("mSignalStrength", cellSignalStrengthLte);
        this.rssi = -113 + 2 * asu;

        this.snr = this.registered ? servingCellSnr : Integer.MAX_VALUE;

        Earfcn.addUnique(earfcn);
    }

    @SuppressWarnings("unused")
    public long getId() {
        return id;
    }

    public long getLocationId() {
        return locationId;
    }

    @Nullable
    public Long getCoord() {
        return coord;
    }

    public void setCoord(long coord) {
        this.coord = coord;
    }

    @Override
    public boolean isRegistered() {
        return registered;
    }

    @Override
    public int getENodeB() {
        return eNodeB;
    }

    public void setENodeB(int eNodeB) {
        this.eNodeB = eNodeB;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    @Override
    public int getCid() {
        return cid;
    }

    @Override
    public int getEarfcn() {
        return earfcn;
    }

    @Override
    public int getPci() {
        return pci;
    }

    @Override
    public int getTac() {
        return tac;
    }

    @Override
    public int getRsrp() {
        return rsrp;
    }

    @Override
    public int getRsrq() {
        return rsrq;
    }

    @Override
    public int getTa() {
        return ta;
    }

    @Override
    public int getRssi() {
        return rssi;
    }

    @Override
    public int getSnr() {
        return snr;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,
                "CellMeasurementEntity{id=%d locationId=%d coord=%d registered=%b " +
                        "eNodeB=%d cid=%d earfcn=%d pci=%d tac=%d " +
                        "rsrp=%d rsrq=%d ta=%d rssi=%d snr=%d}",
                id, locationId, coord, registered,
                eNodeB, cid, earfcn, pci, tac,
                rsrp, rsrq, ta, rssi, snr);
    }
}
