package se.pcprogramkonsult.coveragemap.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import se.pcprogramkonsult.coveragemap.db.entity.ClusterEntity;

@Dao
public interface ClusterDao {
    @Insert
    void insert(ClusterEntity cluster);

    @Update
    void update(ClusterEntity cluster);

    @Query("SELECT * FROM clusters WHERE coord = :coord")
    ClusterEntity loadCluster(long coord);

    @Query("SELECT DISTINCT(clusters.coord), clusters.latitude, clusters.longitude, clusters.lastMeasurement FROM clusters " +
            "INNER JOIN cell_measurements ON cell_measurements.coord = clusters.coord AND cell_measurements.eNodeB = :eNodeB  " +
            "WHERE clusters.latitude BETWEEN :swLat AND :neLat AND clusters.longitude BETWEEN :swLon AND :neLon")
    LiveData<List<ClusterEntity>> loadClustersForENodeBWithinBounds(int eNodeB, double neLat, double neLon, double swLat, double swLon);

    @Query("SELECT DISTINCT(clusters.coord), clusters.latitude, clusters.longitude, clusters.lastMeasurement FROM clusters " +
            "INNER JOIN cell_measurements ON cell_measurements.coord = clusters.coord AND cell_measurements.eNodeB = :eNodeB AND cell_measurements.cid = :cid " +
            "WHERE clusters.latitude BETWEEN :swLat AND :neLat AND clusters.longitude BETWEEN :swLon AND :neLon")
    LiveData<List<ClusterEntity>> loadClustersForENodeBAndCidWithinBounds(int eNodeB, int cid, double neLat, double neLon, double swLat, double swLon);

    @Query("SELECT DISTINCT(clusters.coord), clusters.latitude, clusters.longitude, clusters.lastMeasurement FROM clusters " +
            "INNER JOIN cell_measurements ON cell_measurements.coord = clusters.coord AND cell_measurements.earfcn = :earfcn " +
            "WHERE clusters.latitude BETWEEN :swLat AND :neLat AND clusters.longitude BETWEEN :swLon AND :neLon")
    LiveData<List<ClusterEntity>> loadClustersForEarfcnWithinBounds(int earfcn, double neLat, double neLon, double swLat, double swLon);

    @Query("SELECT DISTINCT(clusters.coord), clusters.latitude, clusters.longitude, clusters.lastMeasurement FROM clusters " +
            "INNER JOIN cell_measurements ON cell_measurements.coord = clusters.coord AND cell_measurements.earfcn = :earfcn AND cell_measurements.eNodeB = :eNodeB " +
            "WHERE clusters.latitude BETWEEN :swLat AND :neLat AND clusters.longitude BETWEEN :swLon AND :neLon")
    LiveData<List<ClusterEntity>> loadClustersForEarfcnAndENodeBWithinBounds(int earfcn, int eNodeB, double neLat, double neLon, double swLat, double swLon);

    @Query("SELECT * FROM clusters WHERE latitude BETWEEN :swLat AND :neLat AND longitude BETWEEN :swLon AND :neLon")
    LiveData<List<ClusterEntity>> loadClustersWithinBounds(double neLat, double neLon, double swLat, double swLon);
}
