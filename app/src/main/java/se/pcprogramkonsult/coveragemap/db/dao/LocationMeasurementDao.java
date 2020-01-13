package se.pcprogramkonsult.coveragemap.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import se.pcprogramkonsult.coveragemap.db.entity.LocationMeasurementEntity;

@Dao
public interface LocationMeasurementDao {
    @Insert
    long insert(LocationMeasurementEntity locationMeasurementEntity);

    @Query("SELECT * FROM location_measurements WHERE location_measurements.trace = :trace ORDER BY measuredAt ASC LIMIT 1")
    LocationMeasurementEntity loadFirstLocationMeasurementForTrace(long trace);

    @Query("SELECT * FROM location_measurements WHERE location_measurements.trace = :trace " +
            "AND location_measurements.latitude BETWEEN :swLat AND :neLat AND location_measurements.longitude BETWEEN :swLon AND :neLon")
    LiveData<List<LocationMeasurementEntity>> loadLocationMeasurementsForTraceWithinBounds(long trace, double neLat, double neLon, double swLat, double swLon);

    @Query("SELECT * FROM location_measurements WHERE location_measurements.trace = :trace")
    List<LocationMeasurementEntity> loadLocationMeasurementsForTrace(long trace);

    @Query("SELECT * FROM location_measurements")
    List<LocationMeasurementEntity> loadAllLocationMeasurements();
}
