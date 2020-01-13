package se.pcprogramkonsult.coveragemap.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import se.pcprogramkonsult.coveragemap.db.entity.TraceEntity;

@Dao
public interface TraceDao {
    @Insert
    long insert(TraceEntity traceEntity);

    @Delete
    void delete(TraceEntity traceEntity);

    @Query("SELECT * FROM traces WHERE traces.id = :id")
    TraceEntity loadTrace(long id);

    @Query("SELECT * FROM traces ORDER BY start DESC")
    LiveData<List<TraceEntity>> loadAllTraces();

    @Query("SELECT * FROM traces ORDER BY start DESC LIMIT 1")
    TraceEntity loadLatestTrace();
}
