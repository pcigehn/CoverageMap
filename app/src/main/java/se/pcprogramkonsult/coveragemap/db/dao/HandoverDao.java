package se.pcprogramkonsult.coveragemap.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import se.pcprogramkonsult.coveragemap.db.entity.HandoverEntity;

@Dao
public interface HandoverDao {
    @Insert
    void insert(HandoverEntity handover);

    @Delete
    void delete(HandoverEntity handoverEntity);

    @Query("SELECT * FROM handovers WHERE coord = :coord")
    List<HandoverEntity> loadHandoversForClusterSync(long coord);

    @Query("SELECT * FROM handovers WHERE coord = :coord")
    LiveData<List<HandoverEntity>> loadHandoversForCluster(long coord);

    @Query("SELECT * FROM handovers WHERE coord = :coord AND sourceEarfcn = targetEarfcn")
    LiveData<List<HandoverEntity>> loadIntraHandoversForCluster(long coord);

    @Query("SELECT * FROM handovers WHERE coord = :coord AND sourceEarfcn != targetEarfcn")
    LiveData<List<HandoverEntity>> loadInterHandoversForCluster(long coord);

    @Query("SELECT * FROM handovers WHERE coord = :coord AND " +
            "((sourceENodeB = :eNodeB AND sourceCid = :cid) OR (targetENodeB = :eNodeB AND targetCid = :cid))")
    LiveData<List<HandoverEntity>> loadHandoversForClusterAndENodeBAndCid(long coord, int eNodeB, int cid);

    @Query("SELECT * FROM handovers WHERE coord = :coord AND " +
            "((sourceENodeB = :eNodeB AND sourceCid = :cid) OR (targetENodeB = :eNodeB AND targetCid = :cid)) AND " +
            "(sourceEarfcn = targetEarfcn)")
    LiveData<List<HandoverEntity>> loadIntraHandoversForClusterAndENodeBAndCid(long coord, int eNodeB, int cid);

    @Query("SELECT * FROM handovers WHERE coord = :coord AND " +
            "((sourceENodeB = :eNodeB AND sourceCid = :cid) OR (targetENodeB = :eNodeB AND targetCid = :cid)) AND " +
            "(sourceEarfcn != targetEarfcn)")
    LiveData<List<HandoverEntity>> loadInterHandoversForClusterAndENodeBAndCid(long coord, int eNodeB, int cid);

    @Query("SELECT * FROM handovers WHERE coord = :coord AND " +
            "((sourceENodeB = :eNodeB AND sourceEarfcn = :earfcn) OR (targetENodeB = :eNodeB AND targetEarfcn = :earfcn))")
    LiveData<List<HandoverEntity>> loadHandoversForClusterAndENodeBAndEarfcn(long coord, int eNodeB, int earfcn);

    @Query("SELECT * FROM handovers WHERE coord = :coord AND " +
            "((sourceENodeB = :eNodeB AND sourceEarfcn = :earfcn) OR (targetENodeB = :eNodeB AND targetEarfcn = :earfcn)) AND " +
            "(sourceEarfcn = targetEarfcn)")
    LiveData<List<HandoverEntity>> loadIntraHandoversForClusterAndENodeBAndEarfcn(long coord, int eNodeB, int earfcn);

    @Query("SELECT * FROM handovers WHERE coord = :coord AND " +
            "((sourceENodeB = :eNodeB AND sourceEarfcn = :earfcn) OR (targetENodeB = :eNodeB AND targetEarfcn = :earfcn)) AND " +
            "(sourceEarfcn != targetEarfcn)")
    LiveData<List<HandoverEntity>> loadInterHandoversForClusterAndENodeBAndEarfcn(long coord, int eNodeB, int earfcn);

    @Query("SELECT * FROM handovers WHERE coord = :coord AND " +
            "(sourceENodeB = :eNodeB OR targetENodeB = :eNodeB)")
    LiveData<List<HandoverEntity>> loadHandoversForClusterAndENodeB(long coord, int eNodeB);

    @Query("SELECT * FROM handovers WHERE coord = :coord AND " +
            "(sourceENodeB = :eNodeB OR targetENodeB = :eNodeB) AND " +
            "(sourceEarfcn = targetEarfcn)")
    LiveData<List<HandoverEntity>> loadIntraHandoversForClusterAndENodeB(long coord, int eNodeB);

    @Query("SELECT * FROM handovers WHERE coord = :coord AND " +
            "(sourceENodeB = :eNodeB OR targetENodeB = :eNodeB) AND " +
            "(sourceEarfcn != targetEarfcn)")
    LiveData<List<HandoverEntity>> loadInterHandoversForClusterAndENodeB(long coord, int eNodeB);

    @Query("SELECT * FROM handovers WHERE coord = :coord AND " +
            "(sourceEarfcn = :earfcn OR targetEarfcn = :earfcn)")
    LiveData<List<HandoverEntity>> loadHandoversForClusterAndEarfcn(long coord, int earfcn);

    @Query("SELECT * FROM handovers WHERE coord = :coord AND " +
            "(sourceEarfcn = :earfcn OR targetEarfcn = :earfcn) AND " +
            "(sourceEarfcn = targetEarfcn)")
    LiveData<List<HandoverEntity>> loadIntraHandoversForClusterAndEarfcn(long coord, int earfcn);

    @Query("SELECT * FROM handovers WHERE coord = :coord AND " +
            "(sourceEarfcn = :earfcn OR targetEarfcn = :earfcn) AND " +
            "(sourceEarfcn != targetEarfcn)")
    LiveData<List<HandoverEntity>> loadInterHandoversForClusterAndEarfcn(long coord, int earfcn);

    @Query("SELECT * FROM handovers")
    List<HandoverEntity> loadAllHandovers();
}
