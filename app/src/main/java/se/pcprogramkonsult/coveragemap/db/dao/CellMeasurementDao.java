package se.pcprogramkonsult.coveragemap.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import se.pcprogramkonsult.coveragemap.db.entity.CellMeasurementEntity;

@Dao
public interface CellMeasurementDao {
    @Insert
    void insertAll(List<CellMeasurementEntity> cellMeasurements);

    @Update
    void update(CellMeasurementEntity cellMeasurementEntity);

    @Update
    void updateAll(List<CellMeasurementEntity> cellMeasurementEntities);

    @Query("SELECT * FROM cell_measurements WHERE locationId = :locationId")
    List<CellMeasurementEntity> loadCellMeasurements(long locationId);

    @Query("SELECT * FROM cell_measurements WHERE coord = :coord")
    List<CellMeasurementEntity> loadCellMeasurementsForClusterSync(long coord);

    @Query("SELECT * FROM cell_measurements WHERE coord = :coord")
    LiveData<List<CellMeasurementEntity>> loadCellMeasurementsForCluster(long coord);

    @Query("SELECT * FROM cell_measurements WHERE coord = :coord AND eNodeB = :eNodeB")
    LiveData<List<CellMeasurementEntity>> loadCellMeasurementsForClusterAndENodeB(long coord, int eNodeB);

    @Query("SELECT * FROM cell_measurements WHERE coord = :coord AND eNodeB = :eNodeB AND cid = :cid")
    LiveData<List<CellMeasurementEntity>> loadCellMeasurementsForClusterAndENodeBAndCid(long coord, int eNodeB, int cid);

    @Query("SELECT * FROM cell_measurements WHERE coord = :coord AND earfcn = :earfcn")
    LiveData<List<CellMeasurementEntity>> loadCellMeasurementsForClusterAndEarfcn(long coord, int earfcn);

    @Query("SELECT * FROM cell_measurements WHERE coord = :coord AND earfcn = :earfcn AND eNodeB = :eNodeB")
    LiveData<List<CellMeasurementEntity>> loadCellMeasurementsForClusterAndEarfcnAndENodeB(long coord, int earfcn, int eNodeB);

    @Query("SELECT * FROM cell_measurements WHERE locationId = :currLocationId OR locationId = :prevLocationId")
    LiveData<List<CellMeasurementEntity>> loadCellMeasurementsForLocationMeasurements(Long currLocationId, Long prevLocationId);
}
