package se.pcprogramkonsult.coveragemap.model;

import java.util.Date;

public interface Cluster {
    long getCoord();

    double getLatitude();
    double getLongitude();

    Date getLastMeasurement();
}
