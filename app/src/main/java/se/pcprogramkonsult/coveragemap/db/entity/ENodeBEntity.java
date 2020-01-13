package se.pcprogramkonsult.coveragemap.db.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

import se.pcprogramkonsult.coveragemap.model.ENodeB;

@Entity(tableName = "enodebs")
public class ENodeBEntity implements ENodeB {
    @PrimaryKey
    private final int id;

    @Nullable
    private String name;

    private double latitude;
    private double longitude;

    private boolean located;

    @Nullable
    @Ignore
    private LatLng latLng = null;

    public ENodeBEntity(final int id, @Nullable final String name, final double latitude, final double longitude, final boolean located) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.located = located;
    }

    @Ignore
    public ENodeBEntity(final int id) {
        this.id = id;
        this.name = null;
        this.located = false;
    }

    @Ignore
    public ENodeBEntity(final int id, @Nullable final String name, @NonNull final LatLng latLng) {
        this.id = id;
        this.name = name;
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
        this.located = true;
    }

    @Override
    public int getId() {
        return id;
    }

    @Nullable
    @Override
    public String getName() {
        return name;
    }

    public void setName(@Nullable final String name) {
        this.name = name;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean isLocated() {
        return located;
    }

    public void setLocated(boolean located) {
        this.located = located;
    }

    @NonNull
    @Ignore
    public LatLng getLatLng() {
        if (latLng == null) {
            latLng = new LatLng(getLatitude(), getLongitude());
        }
        return latLng;
    }

    @Ignore
    public void setLocatedLatLng(@NonNull final LatLng latLng) {
        setLatLng(latLng);
        setLocated(true);
    }

    @Ignore
    public void setLatLng(@NonNull final LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "ENodeBEntity{id=%d latLng=%s located=%b}", id, getLatLng().toString(), located);
    }
}
