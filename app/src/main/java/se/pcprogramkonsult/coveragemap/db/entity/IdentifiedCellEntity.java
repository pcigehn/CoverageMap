package se.pcprogramkonsult.coveragemap.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Locale;

import se.pcprogramkonsult.coveragemap.lte.IdUtil;
import se.pcprogramkonsult.coveragemap.model.IdentifiedCell;

@Entity(tableName = "identified_cells", primaryKeys = { "eNodeB", "cid" })
public class IdentifiedCellEntity implements IdentifiedCell {
    private final int eNodeB;
    private final int cid;

    private final int pci;
    private final int earfcn;

    private double swLat;
    private double swLon;

    private double neLat;
    private double neLon;

    private double extSwLat;
    private double extSwLon;

    private double extNeLat;
    private double extNeLon;

    @Ignore
    private LatLngBounds bounds;

    @Ignore
    private LatLngBounds extBounds;

    public IdentifiedCellEntity(final int eNodeB, final int cid,
                                final int pci, final int earfcn,
                                final double neLat, final double neLon,
                                final double swLat, final double swLon,
                                final double extNeLat, final double extNeLon,
                                final double extSwLat, final double extSwLon)
    {
        this.eNodeB = eNodeB;
        this.cid = cid;
        this.pci = pci;
        this.earfcn = earfcn;
        this.swLat = swLat;
        this.swLon = swLon;
        this.neLat = neLat;
        this.neLon = neLon;
        this.extSwLat = extSwLat;
        this.extSwLon = extSwLon;
        this.extNeLat = extNeLat;
        this.extNeLon = extNeLon;
        this.bounds = new LatLngBounds(new LatLng(getSwLat(), getSwLon()), new LatLng(getNeLat(), getNeLon()));
        this.extBounds = new LatLngBounds(new LatLng(getExtSwLat(), getExtSwLon()), new LatLng(getExtNeLat(), getExtNeLon()));
    }

    @Ignore
    public IdentifiedCellEntity(final int eNodeB, final int cid,
                                final int pci, final int earfcn,
                                @NonNull final LatLng latLng)
    {
        this(eNodeB, cid, pci, earfcn,
                latLng.latitude, latLng.longitude,
                latLng.latitude, latLng.longitude,
                latLng.latitude, latLng.longitude,
                latLng.latitude, latLng.longitude);
    }

    @Override
    public int getENodeB() {
        return eNodeB;
    }

    @Override
    public int getCid() {
        return cid;
    }

    @Override
    public int getPci() {
        return pci;
    }

    @Override
    public int getEarfcn() {
        return earfcn;
    }

    @Override
    public double getSwLat() {
        return swLat;
    }

    @Override
    public double getSwLon() {
        return swLon;
    }

    @Override
    public double getNeLat() {
        return neLat;
    }

    @Override
    public double getNeLon() {
        return neLon;
    }

    @Override
    public double getExtSwLat() {
        return extSwLat;
    }

    @Override
    public double getExtSwLon() {
        return extSwLon;
    }

    @Override
    public double getExtNeLat() {
        return extNeLat;
    }

    @Override
    public double getExtNeLon() {
        return extNeLon;
    }

    public int getCi() {
        return IdUtil.getCi(eNodeB, cid);
    }

    public LatLngBounds getBounds() {
        return bounds;
    }

    public LatLngBounds getExtBounds() {
        return extBounds;
    }

    public void setBounds(@NonNull LatLngBounds bounds) {
        this.swLat = bounds.southwest.latitude;
        this.swLon = bounds.southwest.longitude;
        this.neLat = bounds.northeast.latitude;
        this.neLon = bounds.northeast.longitude;
        this.bounds = bounds;
    }

    public void setExtBounds(@NonNull LatLngBounds extBounds) {
        this.extSwLat = extBounds.southwest.latitude;
        this.extSwLon = extBounds.southwest.longitude;
        this.extNeLat = extBounds.northeast.latitude;
        this.extNeLon = extBounds.northeast.longitude;
        this.extBounds = extBounds;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "IdentifiedCellEntity{eNodeB=%d cid=%d earfcn=%d pci=%d bounds=%s extBounds=%s}",
                eNodeB, cid, earfcn, pci, bounds.toString(), extBounds.toString());
    }

}
