package se.pcprogramkonsult.coveragemap.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import se.pcprogramkonsult.coveragemap.db.entity.ENodeBEntity;

@Dao
public interface ENodeBDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertOrIgnoreIfAlreadyExists(ENodeBEntity eNodeB);

    @Update
    void update(ENodeBEntity eNodeB);

    @Query("SELECT * FROM enodebs WHERE id = :eNodeBId")
    LiveData<ENodeBEntity> loadENodeB(int eNodeBId);

    @Query("SELECT * FROM enodebs WHERE id = :eNodeBId")
    LiveData<List<ENodeBEntity>> loadENodeBs(int eNodeBId);

    @Query("SELECT * FROM enodebs ORDER BY name ASC")
    LiveData<List<ENodeBEntity>> loadAllENodeBs();
}
