package se.pcprogramkonsult.coveragemap.model;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

public interface ENodeB {
    int getId();
    @Nullable
    String getName();
    double getLatitude();
    double getLongitude();
    boolean isLocated();
    @Nullable
    LatLng getLatLng();
}
