package se.pcprogramkonsult.coveragemap.db.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import se.pcprogramkonsult.coveragemap.model.LocationMeasurement;

@Entity(tableName = "location_measurements",
        foreignKeys = {
            @ForeignKey(entity = TraceEntity.class,
                parentColumns = "id",
                childColumns = "trace",
                onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "coord"), @Index(value = "trace")})
public class LocationMeasurementEntity implements LocationMeasurement {
    @PrimaryKey(autoGenerate = true)
    private long id = 0;

    @Nullable
    private final Long coord;

    @Nullable
    private final Long trace;

    @Nullable
    private final Long prevId;

    private final double latitude;
    private final double longitude;

    private final Date measuredAt;

    private final int dataActivity;
    private final int dataState;

    @Nullable
    @Ignore
    private LatLng latLng = null;

    @Ignore
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);

    public LocationMeasurementEntity(long id, @Nullable Long coord, @Nullable Long trace, @Nullable Long prevId, double longitude, double latitude, Date measuredAt, int dataActivity, int dataState) {
        this.id = id;
        this.coord = coord;
        this.trace = trace;
        this.prevId = prevId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.measuredAt = measuredAt;
        this.dataActivity = dataActivity;
        this.dataState = dataState;
    }

    @Ignore
    public LocationMeasurementEntity(@Nullable Long coord, @Nullable Long trace, @Nullable Long prevId, @NonNull LatLng latLng, int dataActivity, int dataState) {
        this.coord = coord;
        this.trace = trace;
        this.prevId = prevId;
        this.longitude = latLng.longitude;
        this.latitude = latLng.latitude;
        this.measuredAt = new Date();
        this.dataActivity = dataActivity;
        this.dataState = dataState;
    }

    @Override
    public long getId() {
        return id;
    }

    @Nullable
    @Override
    public Long getCoord() {
        return coord;
    }

    @SuppressWarnings("unused")
    @Nullable
    @Override
    public Long getTrace() {
        return trace;
    }

    @Nullable
    @Override
    public Long getPrevId() {
        return prevId;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @NonNull
    public LatLng getLatLng() {
        if (latLng == null) {
            latLng = new LatLng(getLatitude(), getLongitude());
        }
        return latLng;
    }

    @SuppressWarnings("unused")
    @Override
    public Date getMeasuredAt() {
        return measuredAt;
    }

    @SuppressWarnings("unused")
    @Override
    public int getDataActivity() {
        return dataActivity;
    }

    @SuppressWarnings("unused")
    @Override
    public int getDataState() {
        return dataState;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "LocationMeasurementEntity{id=%d coord=%d trace=%d prevId=%d latitude=%f longitude=%f measuredAt=%s dataActivity=%d dataState=%d}",
                id, coord, trace, prevId, latitude, longitude, DATE_FORMAT.format(measuredAt), dataActivity, dataState);
    }
}
