package se.pcprogramkonsult.coveragemap.db.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import se.pcprogramkonsult.coveragemap.model.Cluster;

@Entity(tableName = "clusters")
public class ClusterEntity implements Cluster {
    @PrimaryKey
    private final long coord;

    private final double latitude;
    private final double longitude;

    private Date lastMeasurement;

    @Nullable
    @Ignore
    private LatLng latLng = null;

    public ClusterEntity(final long coord, final double latitude, final double longitude, final Date lastMeasurement) {
        this.coord = coord;

        this.latitude = latitude;
        this.longitude = longitude;

        this.lastMeasurement = lastMeasurement;
    }

    @Ignore
    public ClusterEntity(final long coord, @NonNull final LatLng latLng) {
        this.coord = coord;
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
        this.lastMeasurement = new Date(0);
    }

    @Override
    public long getCoord() {
        return coord;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @SuppressWarnings("unused")
    @Override
    public Date getLastMeasurement() {
        return lastMeasurement;
    }

    public void updateLastMeasurement() {
        this.lastMeasurement = new Date();
    }

    @NonNull
    public LatLng getLatLng() {
        if (latLng == null) {
            latLng = new LatLng(getLatitude(), getLongitude());
        }
        return latLng;
    }
}
