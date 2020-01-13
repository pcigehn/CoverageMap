package se.pcprogramkonsult.coveragemap.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import se.pcprogramkonsult.coveragemap.db.entity.IdentifiedCellEntity;
import se.pcprogramkonsult.coveragemap.db.entity.IdentifiedCellWithENodeB;

@Dao
public interface IdentifiedCellDao {
    @Query("SELECT * FROM identified_cells WHERE eNodeB = :eNodeB AND cid = :cid")
    IdentifiedCellEntity load(int eNodeB, int cid);

    @Query("SELECT * FROM identified_cells WHERE earfcn = :earfcn AND pci = :pci")
    List<IdentifiedCellEntity> loadMatchingCells(int earfcn, int pci);

    @Query("SELECT * FROM identified_cells")
    List<IdentifiedCellEntity> loadAll();

    @Insert
    void insert(IdentifiedCellEntity identifiedCellEntity);

    @Update
    void update(IdentifiedCellEntity identifiedCellEntity);

    @Query("SELECT * FROM identified_cells WHERE eNodeB == :eNodeB")
    LiveData<List<IdentifiedCellEntity>> loadIdentifiedCellsForENodeB(final int eNodeB);

    @Query("SELECT * FROM identified_cells INNER JOIN enodebs ON identified_cells.eNodeB = enodebs.id")
    LiveData<List<IdentifiedCellWithENodeB>> loadAllIdentifiedCellsWithENodeB();

    @Query("SELECT * FROM identified_cells INNER JOIN enodebs ON identified_cells.eNodeB = enodebs.id WHERE eNodeB == :eNodeB")
    LiveData<List<IdentifiedCellWithENodeB>> loadIdentifiedCellsWithENodeBForENodeB(final int eNodeB);

    @Query("SELECT * FROM identified_cells INNER JOIN enodebs ON identified_cells.eNodeB = enodebs.id WHERE eNodeB == :eNodeB AND cid == :cid")
    LiveData<List<IdentifiedCellWithENodeB>> loadIdentifiedCellWithENodeBForENodeBAndCid(int eNodeB, int cid);

    @Query("SELECT * FROM identified_cells INNER JOIN enodebs ON identified_cells.eNodeB = enodebs.id WHERE earfcn == :earfcn")
    LiveData<List<IdentifiedCellWithENodeB>> loadIdentifiedCellsWithENodeBForEarfcn(final int earfcn);

    @Query("SELECT * FROM identified_cells INNER JOIN enodebs ON identified_cells.eNodeB = enodebs.id WHERE earfcn == :earfcn AND eNodeB == :eNodeB")
    LiveData<List<IdentifiedCellWithENodeB>> loadIdentifiedCellsWithENodeBForEarfcnAndENodeB(
            final int earfcn, final int eNodeB);
}
