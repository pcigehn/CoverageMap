package se.pcprogramkonsult.coveragemap;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import se.pcprogramkonsult.coveragemap.db.entity.ENodeBEntity;
import se.pcprogramkonsult.coveragemap.model.ENodeB;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private static final double ZOOM = 18.5;
    private static final int GRID_SIZE = 100;
    private static final long mNumCells = (long) Math.ceil(256 * Math.pow(2, ZOOM) / GRID_SIZE);
    private static final SphericalMercatorProjection mProjection = new SphericalMercatorProjection(mNumCells);

    static class IdentifiedENodeB {

        private static void add(@NonNull final List<ENodeBEntity> eNodeBs, final int eNodeBId,
                                final String name, final double latitude, final double longitude) {
            eNodeBs.add(new ENodeBEntity(eNodeBId, name, new LatLng(latitude, longitude)));
        }

        @NonNull
        static List<ENodeBEntity> getAll() {
            List<ENodeBEntity> eNodeBs = new ArrayList<>();
            add(eNodeBs, 100328, "Ryd Centrum",                  58.408973, 15.562916);
            add(eNodeBs, 100331, "Collegium, Mjärdevi",          58.393524, 15.560889);
            add(eNodeBs, 100649, "Tekniska verken, Lambohov",    58.391009, 15.574672);
            add(eNodeBs, 100651, "A-Huset, Universitetet",       58.402869, 15.577147);
            add(eNodeBs, 100652, "Ericsson, Mjärdevi",           58.397480, 15.557889);
            add(eNodeBs, 100990, "Bygdegatan, Lambohov",         58.383277, 15.570142);
            add(eNodeBs, 100993, "Ulvåsavägen, Gottfridsberg",   58.408410, 15.593446);
            add(eNodeBs, 105110, "Vidingsjö Motionscentrum",     58.371013, 15.644164);
            add(eNodeBs, 105260, "Räddningsstationen, Lambohov", 58.390463, 15.583042);
            add(eNodeBs, 105993, "Vallastaden",                  58.393484, 15.579996);
            return eNodeBs;
        }
    }

    @Test
    public void coordinateMapping() {
        for (ENodeB eNodeB : IdentifiedENodeB.getAll()) {
            Point point1 = mProjection.toPoint(Objects.requireNonNull(eNodeB.getLatLng()));
            long coord1 = getCoord(point1);
            Point point2 = getPoint(coord1);
            long coord2 = getCoord(point2);
            System.out.println(point1 + " = " + point2);
            assertEquals(coord1, coord2);
        }
    }

    private long getCoord(@NonNull Point p) {
        return (long) (mNumCells * Math.floor(p.x) + Math.floor(p.y));
    }

    @NonNull
    private Point getPoint(long coord) {
        return new Point((double)(coord / mNumCells), (double)(coord % mNumCells));
    }

}