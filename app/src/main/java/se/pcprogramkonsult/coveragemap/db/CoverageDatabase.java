package se.pcprogramkonsult.coveragemap.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import se.pcprogramkonsult.coveragemap.db.converter.DateConverter;
import se.pcprogramkonsult.coveragemap.db.dao.CellMeasurementDao;
import se.pcprogramkonsult.coveragemap.db.dao.ClusterDao;
import se.pcprogramkonsult.coveragemap.db.dao.ENodeBDao;
import se.pcprogramkonsult.coveragemap.db.dao.HandoverDao;
import se.pcprogramkonsult.coveragemap.db.dao.IdentifiedCellDao;
import se.pcprogramkonsult.coveragemap.db.dao.LocationMeasurementDao;
import se.pcprogramkonsult.coveragemap.db.dao.TraceDao;
import se.pcprogramkonsult.coveragemap.db.entity.CellMeasurementEntity;
import se.pcprogramkonsult.coveragemap.db.entity.ClusterEntity;
import se.pcprogramkonsult.coveragemap.db.entity.ENodeBEntity;
import se.pcprogramkonsult.coveragemap.db.entity.HandoverEntity;
import se.pcprogramkonsult.coveragemap.db.entity.IdentifiedCellEntity;
import se.pcprogramkonsult.coveragemap.db.entity.LocationMeasurementEntity;
import se.pcprogramkonsult.coveragemap.db.entity.TraceEntity;

@Database(entities = {CellMeasurementEntity.class, LocationMeasurementEntity.class,
        ClusterEntity.class, ENodeBEntity.class, IdentifiedCellEntity.class,
        TraceEntity.class, HandoverEntity.class}, version = 3)
@TypeConverters(DateConverter.class)
public abstract class CoverageDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "coverage_db";

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `handovers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `coord` INTEGER NOT NULL, `sourceENodeB` INTEGER NOT NULL, `sourceCid` INTEGER NOT NULL, `sourceEarfcn` INTEGER NOT NULL, `sourcePci` INTEGER NOT NULL, `sourceRsrp` INTEGER NOT NULL, `sourceRsrq` INTEGER NOT NULL, `targetENodeB` INTEGER NOT NULL, `targetCid` INTEGER NOT NULL, `targetEarfcn` INTEGER NOT NULL, `targetPci` INTEGER NOT NULL, `targetRsrp` INTEGER NOT NULL, `targetRsrq` INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX `index_handovers_coord` ON `handovers` (`coord`)");
        }
    };
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE cell_measurements ADD COLUMN coord INTEGER");
            database.execSQL("CREATE INDEX `index_cell_measurements_coord_eNodeB_cid` ON `cell_measurements` (`coord`, `eNodeB`, `cid`)");
            database.execSQL("CREATE INDEX `index_cell_measurements_coord_earfcn_eNodeB` ON `cell_measurements` (`coord`, `earfcn`, `eNodeB`)");
            database.execSQL("DROP INDEX `index_cell_measurements_eNodeB`");
        }
    };

    private static CoverageDatabase sInstance;

    public abstract CellMeasurementDao cellMeasurementDao();
    public abstract LocationMeasurementDao locationMeasurementDao();
    public abstract ClusterDao clusterDao();
    public abstract ENodeBDao eNodeBDao();
    public abstract IdentifiedCellDao identifiedCellDao();
    public abstract TraceDao traceDao();
    public abstract HandoverDao handoverDao();

    public static CoverageDatabase getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (CoverageDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    @NonNull
    private static CoverageDatabase buildDatabase(@NonNull final Context appContext) {
        return Room.databaseBuilder(appContext, CoverageDatabase.class, DATABASE_NAME).
                addMigrations(MIGRATION_1_2, MIGRATION_2_3).build();
    }
}
