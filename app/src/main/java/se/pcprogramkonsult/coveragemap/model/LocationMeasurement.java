package se.pcprogramkonsult.coveragemap.model;

import androidx.annotation.Nullable;

import java.util.Date;

public interface LocationMeasurement {
    long getId();

    @Nullable
    Long getCoord();

    @Nullable
    Long getTrace();
    @Nullable
    Long getPrevId();

    double getLatitude();
    double getLongitude();

    Date getMeasuredAt();

    int getDataActivity();
    int getDataState();
}
