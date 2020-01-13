package se.pcprogramkonsult.coveragemap.map;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;

public class ClusterGrid {
    private static final double ZOOM = 18.5;
    private static final int GRID_SIZE = 100;

    private final long mNumCells;
    @NonNull
    private final SphericalMercatorProjection mProjection;

    public ClusterGrid() {
        mNumCells = (long) Math.ceil(256 * Math.pow(2, ZOOM) / GRID_SIZE);
        mProjection = new SphericalMercatorProjection(mNumCells);
    }

    public long getCoord(@NonNull final LatLng latLng) {
        final Point point = mProjection.toPoint(latLng);
        return getCoord(point.x, point.y);
    }

    public LatLng getLatLng(final long coord) {
        final Point point = getPoint(coord);
        return mProjection.toLatLng(new Point(Math.floor(point.x) + .5, Math.floor(point.y) + .5));
    }

    private long getCoord(final double x, final double y) {
        return (long) (mNumCells * Math.floor(x) + Math.floor(y));
    }

    @NonNull
    private Point getPoint(long coord) {
        return new Point((double)(coord / mNumCells), (double)(coord % mNumCells));
    }
}
