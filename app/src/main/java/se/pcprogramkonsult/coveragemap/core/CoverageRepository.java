package se.pcprogramkonsult.coveragemap.core;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import se.pcprogramkonsult.coveragemap.db.CoverageDatabase;
import se.pcprogramkonsult.coveragemap.db.entity.CellMeasurementEntity;
import se.pcprogramkonsult.coveragemap.db.entity.ClusterEntity;
import se.pcprogramkonsult.coveragemap.db.entity.ENodeBEntity;
import se.pcprogramkonsult.coveragemap.db.entity.HandoverEntity;
import se.pcprogramkonsult.coveragemap.db.entity.IdentifiedCellEntity;
import se.pcprogramkonsult.coveragemap.db.entity.IdentifiedCellWithENodeB;
import se.pcprogramkonsult.coveragemap.db.entity.LocationMeasurementEntity;
import se.pcprogramkonsult.coveragemap.db.entity.TraceEntity;
import se.pcprogramkonsult.coveragemap.lifecycle.SharedPreferenceBooleanLiveData;
import se.pcprogramkonsult.coveragemap.lte.CellMeasurementBounds;
import se.pcprogramkonsult.coveragemap.lte.Earfcn;
import se.pcprogramkonsult.coveragemap.lte.EarfcnPciPair;
import se.pcprogramkonsult.coveragemap.lte.IdUtil;
import se.pcprogramkonsult.coveragemap.map.ClusterGrid;

public class CoverageRepository {
    private static final String TAG = CoverageRepository.class.getSimpleName();

    private static final long DELAY_BASE = 200;
    private static final long DELAY_DELTA = 150;

    private static final long INITIAL_TRACE = -1;

    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";
    private static final LatLngBounds UNDEFINED_BOUNDS = new LatLngBounds(new LatLng(0.0, 0.0), new LatLng(0.0, 0.0));

    private static CoverageRepository sInstance;

    private final Application mApplication;
    private final CoverageDatabase mDatabase;
    private final CoverageAppExecutors mExecutors;
    @NonNull
    private final ClusterGrid mClusterGrid;

    private final MutableLiveData<LatLng> mInitialLocation = new MutableLiveData<>();
    private final MutableLiveData<LatLng> mCurrentLocation = new MutableLiveData<>();
    private final MutableLiveData<CellMeasurementEntity> mCurrentServingCellMeasurement = new MutableLiveData<>();
    private final MutableLiveData<CellMeasurementEntity> mCurrentReplayServingCellMeasurement = new MutableLiveData<>();
    private final MutableLiveData<IdentifiedCellEntity> mCurrentIdentifiedCell = new MutableLiveData<>();
    private final MutableLiveData<Long> mCurrentTraceId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsReplayActive = new MutableLiveData<>();
    private final MutableLiveData<List<LocationMeasurementEntity>> mLocationMeasurementsForReplayTrace = new MutableLiveData<>();

    @Nullable
    private Long mPreviousLocationMeasurementId = null;
    private boolean mRefreshScheduled = false;

    @NonNull
    private final Handler mHandler;

    private int mNoOfIdentifiedCellCandidates = 0;
    private int mNoOfNotUpdatedCellCandidates = 0;
    private int mNoOfTooDistantCellCandidates = 0;
    private int mNoOfUnidentifiedCellCandidates = 0;
    private int mNoOfPciConfusion = 0;

    private CoverageRepository(
            final Application application,
            final CoverageDatabase database,
            final CoverageAppExecutors executors) {
        mApplication = application;
        mDatabase = database;
        mExecutors = executors;
        mClusterGrid = new ClusterGrid();
        mHandler = new Handler(Looper.getMainLooper());
        mCurrentServingCellMeasurement.setValue(null);
        mCurrentReplayServingCellMeasurement.setValue(null);
        mCurrentTraceId.setValue(null);
        mIsReplayActive.setValue(false);
    }

    static CoverageRepository getInstance(
            final Application application,
            final CoverageDatabase database,
            final CoverageAppExecutors executors) {
        if (sInstance == null) {
            synchronized (CoverageRepository.class) {
                if (sInstance == null) {
                    sInstance = new CoverageRepository(application, database, executors);
                }
            }
        }
        return sInstance;
    }

    public LiveData<List<ENodeBEntity>> getAllENodeBs() {
        return mDatabase.eNodeBDao().loadAllENodeBs();
    }

    public LiveData<List<ENodeBEntity>> getENodeBs(final int eNodeBId) {
        return mDatabase.eNodeBDao().loadENodeBs(eNodeBId);
    }

    public LiveData<ENodeBEntity> getENodeB(final int eNodeBId) {
        return mDatabase.eNodeBDao().loadENodeB(eNodeBId);
    }

    public void updateENodeB(final ENodeBEntity eNodeB) {
        mExecutors.diskIO().execute(() -> mDatabase.eNodeBDao().update(eNodeB));
    }

    public LiveData<List<ClusterEntity>> getClustersWithinBounds(@NonNull final LatLngBounds bounds) {
        return mDatabase.clusterDao().loadClustersWithinBounds(
                bounds.northeast.latitude, bounds.northeast.longitude,
                bounds.southwest.latitude, bounds.southwest.longitude);
    }

    public LiveData<List<ClusterEntity>> getClustersForENodeBWithinBounds(final int eNodeBId, @NonNull final LatLngBounds bounds) {
        return mDatabase.clusterDao().loadClustersForENodeBWithinBounds(eNodeBId,
                bounds.northeast.latitude, bounds.northeast.longitude,
                bounds.southwest.latitude, bounds.southwest.longitude);
    }

    public LiveData<List<ClusterEntity>> getClustersForCiWithinBounds(final int ci, @NonNull final LatLngBounds bounds) {
        return mDatabase.clusterDao().loadClustersForENodeBAndCidWithinBounds(IdUtil.getENodeB(ci), IdUtil.getCid(ci),
                bounds.northeast.latitude, bounds.northeast.longitude,
                bounds.southwest.latitude, bounds.southwest.longitude);
    }

    public LiveData<List<ClusterEntity>> getClustersForEarfcnWithinBounds(final int earfcn, @NonNull final LatLngBounds bounds) {
        return mDatabase.clusterDao().loadClustersForEarfcnWithinBounds(earfcn,
                bounds.northeast.latitude, bounds.northeast.longitude,
                bounds.southwest.latitude, bounds.southwest.longitude);
    }

    public LiveData<List<ClusterEntity>> getClustersForEarfcnAndENodeBWithinBounds(final int earfcn, final int eNodeB, @NonNull final LatLngBounds bounds) {
        return mDatabase.clusterDao().loadClustersForEarfcnAndENodeBWithinBounds(earfcn, eNodeB,
                bounds.northeast.latitude, bounds.northeast.longitude,
                bounds.southwest.latitude, bounds.southwest.longitude);
    }

    @NonNull
    public LiveData<List<LocationMeasurementEntity>> getLocationMeasurementsForReplayTrace() {
        return mLocationMeasurementsForReplayTrace;
    }

    public LiveData<List<LocationMeasurementEntity>> getLocationMeasurementsForTraceWithinBounds(
            final long traceId, @NonNull final LatLngBounds bounds) {
        return mDatabase.locationMeasurementDao().loadLocationMeasurementsForTraceWithinBounds(traceId,
                bounds.northeast.latitude, bounds.northeast.longitude,
                bounds.southwest.latitude, bounds.southwest.longitude);
    }

    public LiveData<List<HandoverEntity>> getHandoversForCluster(final long coord) {
        return mDatabase.handoverDao().loadHandoversForCluster(coord);
    }

    public LiveData<List<HandoverEntity>> getIntraHandoversForCluster(final long coord) {
        return mDatabase.handoverDao().loadIntraHandoversForCluster(coord);
    }

    public LiveData<List<HandoverEntity>> getInterHandoversForCluster(final long coord) {
        return mDatabase.handoverDao().loadInterHandoversForCluster(coord);
    }

    public LiveData<List<HandoverEntity>> getHandoversForClusterAndCi(final long coord, final int ci) {
        return mDatabase.handoverDao().loadHandoversForClusterAndENodeBAndCid(coord, IdUtil.getENodeB(ci), IdUtil.getCid(ci));
    }

    public LiveData<List<HandoverEntity>> getIntraHandoversForClusterAndCi(final long coord, final int ci) {
        return mDatabase.handoverDao().loadIntraHandoversForClusterAndENodeBAndCid(coord, IdUtil.getENodeB(ci), IdUtil.getCid(ci));
    }

    public LiveData<List<HandoverEntity>> getInterHandoversForClusterAndCi(final long coord, final int ci) {
        return mDatabase.handoverDao().loadInterHandoversForClusterAndENodeBAndCid(coord, IdUtil.getENodeB(ci), IdUtil.getCid(ci));
    }

    public LiveData<List<HandoverEntity>> getHandoversForClusterAndENodeBAndEarfcn(final long coord, final int eNodeB, final int earfcn) {
        return mDatabase.handoverDao().loadHandoversForClusterAndENodeBAndEarfcn(coord, eNodeB, earfcn);
    }

    public LiveData<List<HandoverEntity>> getIntraHandoversForClusterAndENodeBAndEarfcn(final long coord, final int eNodeB, final int earfcn) {
        return mDatabase.handoverDao().loadIntraHandoversForClusterAndENodeBAndEarfcn(coord, eNodeB, earfcn);
    }

    public LiveData<List<HandoverEntity>> getInterHandoversForClusterAndENodeBAndEarfcn(final long coord, final int eNodeB, final int earfcn) {
        return mDatabase.handoverDao().loadInterHandoversForClusterAndENodeBAndEarfcn(coord, eNodeB, earfcn);
    }

    public LiveData<List<HandoverEntity>> getHandoversForClusterAndENodeB(final long coord, final int eNodeB) {
        return mDatabase.handoverDao().loadHandoversForClusterAndENodeB(coord, eNodeB);
    }

    public LiveData<List<HandoverEntity>> getIntraHandoversForClusterAndENodeB(final long coord, final int eNodeB) {
        return mDatabase.handoverDao().loadIntraHandoversForClusterAndENodeB(coord, eNodeB);
    }

    public LiveData<List<HandoverEntity>> getInterHandoversForClusterAndENodeB(final long coord, final int eNodeB) {
        return mDatabase.handoverDao().loadInterHandoversForClusterAndENodeB(coord, eNodeB);
    }

    public LiveData<List<HandoverEntity>> getHandoversForClusterAndEarfcn(final long coord, final int earfcn) {
        return mDatabase.handoverDao().loadHandoversForClusterAndEarfcn(coord, earfcn);
    }

    public LiveData<List<HandoverEntity>> getIntraHandoversForClusterAndEarfcn(final long coord, final int earfcn) {
        return mDatabase.handoverDao().loadIntraHandoversForClusterAndEarfcn(coord, earfcn);
    }

    public LiveData<List<HandoverEntity>> getInterHandoversForClusterAndEarfcn(final long coord, final int earfcn) {
        return mDatabase.handoverDao().loadInterHandoversForClusterAndEarfcn(coord, earfcn);
    }

    public LiveData<List<CellMeasurementEntity>> getCellMeasurementsForCluster(final long coord) {
        return mDatabase.cellMeasurementDao().loadCellMeasurementsForCluster(coord);
    }

    public LiveData<List<CellMeasurementEntity>> getCellMeasurementsForClusterAndENodeB(
            final long coord, final int eNodeBId) {
        return mDatabase.cellMeasurementDao().loadCellMeasurementsForClusterAndENodeB(coord, eNodeBId);
    }

    public LiveData<List<CellMeasurementEntity>> getCellMeasurementsForClusterAndCi(
            final long coord, final int ci) {
        return mDatabase.cellMeasurementDao().loadCellMeasurementsForClusterAndENodeBAndCid(coord, IdUtil.getENodeB(ci), IdUtil.getCid(ci));
    }

    public LiveData<List<CellMeasurementEntity>> getCellMeasurementsForClusterAndEarfcn(
            final long coord, final int earfcn) {
        return mDatabase.cellMeasurementDao().loadCellMeasurementsForClusterAndEarfcn(coord, earfcn);
    }

    public LiveData<List<CellMeasurementEntity>> getCellMeasurementsForClusterAndEarfcnAndENodeB(
            final long coord, final int earfcn, final int eNodeB) {
        return mDatabase.cellMeasurementDao().loadCellMeasurementsForClusterAndEarfcnAndENodeB(
                coord, earfcn, eNodeB);
    }

    public LiveData<List<CellMeasurementEntity>> getCellMeasurementsForLocationMeasurements(Long currLocationId, Long prevLocationId) {
        return mDatabase.cellMeasurementDao().loadCellMeasurementsForLocationMeasurements(currLocationId, prevLocationId);
    }

    public LiveData<List<IdentifiedCellEntity>> getIdentifiedCellsForENodeB(final int eNodeBId) {
        return mDatabase.identifiedCellDao().loadIdentifiedCellsForENodeB(eNodeBId);
    }

    public LiveData<List<IdentifiedCellWithENodeB>> getAllIdentifiedCellsWithENodeB() {
        return mDatabase.identifiedCellDao().loadAllIdentifiedCellsWithENodeB();
    }

    public LiveData<List<IdentifiedCellWithENodeB>> getIdentifiedCellsWithENodeBForENodeB(final int eNodeBId) {
        return mDatabase.identifiedCellDao().loadIdentifiedCellsWithENodeBForENodeB(eNodeBId);
    }

    public LiveData<List<IdentifiedCellWithENodeB>> getIdentifiedCellWithENodeB(int ci) {
        return mDatabase.identifiedCellDao().loadIdentifiedCellWithENodeBForENodeBAndCid(IdUtil.getENodeB(ci), IdUtil.getCid(ci));
    }

    public LiveData<List<IdentifiedCellWithENodeB>> getIdentifiedCellsWithENodeBForEarfcn(final int earfcn) {
        return mDatabase.identifiedCellDao().loadIdentifiedCellsWithENodeBForEarfcn(earfcn);
    }

    public LiveData<List<IdentifiedCellWithENodeB>> getIdentifiedCellsWithENodeBForEarfcnAndENodeB(
            final int earfcn, final int eNodeB) {
        return mDatabase.identifiedCellDao().loadIdentifiedCellsWithENodeBForEarfcnAndENodeB(earfcn, eNodeB);
    }

    @NonNull
    public LiveData<List<Earfcn>> getUniqueEarfcns() {
        return Earfcn.getAllUnique();
    }

    public LiveData<List<TraceEntity>> getAllTraces() {
        return mDatabase.traceDao().loadAllTraces();
    }


    @NonNull
    public LiveData<CellMeasurementEntity> getCurrentServingCellMeasurement() {
        return mCurrentServingCellMeasurement;
    }

    @NonNull
    public LiveData<CellMeasurementEntity> getCurrentReplayServingCellMeasurement() {
        return mCurrentReplayServingCellMeasurement;
    }

    private void setCurrentReplayServingCellMeasurement(CellMeasurementEntity cellMeasurementEntity) {
        mCurrentReplayServingCellMeasurement.postValue(cellMeasurementEntity);
    }

    @NonNull
    public LiveData<IdentifiedCellEntity> getCurrentIdentifiedCell() {
        return mCurrentIdentifiedCell;
    }

    private void setCurrentIdentifiedCell(final IdentifiedCellEntity currentIdentifiedCell) {
        mCurrentIdentifiedCell.postValue(currentIdentifiedCell);
    }

    @NonNull
    public LiveData<LatLng> getInitialLocation() {
        return mInitialLocation;
    }

    void setInitialLocation(LatLng latLng) {
        mInitialLocation.postValue(latLng);
    }

    @NonNull
    public LiveData<LatLng> getCurrentLocation() {
        return mCurrentLocation;
    }

    private void setCurrentLocation(LatLng latLng) {
        mCurrentLocation.postValue(latLng);
    }

    @NonNull
    public LiveData<Long> getCurrentTraceId() {
        return mCurrentTraceId;
    }

    private void setCurrentTraceId(final Long traceId) {
        mCurrentTraceId.postValue(traceId);
        mPreviousLocationMeasurementId = null;
    }

    private void setCurrentLocationToStartOfTrace(final Long traceId) {
        LocationMeasurementEntity firstLocationMeasurement = mDatabase.locationMeasurementDao().loadFirstLocationMeasurementForTrace(traceId);
        if (firstLocationMeasurement != null) {
            setCurrentLocation(firstLocationMeasurement.getLatLng());
        }
    }

    public void activateTrace(final Long traceId) {
        mExecutors.diskIO().execute(() -> {
            setCurrentTraceId(traceId);
            setCurrentLocationToStartOfTrace(traceId);
        });
    }

    public void startNewTrace() {
        mExecutors.diskIO().execute(() -> {
            final TraceEntity traceEntity = new TraceEntity();
            final long newTraceId = mDatabase.traceDao().insert(traceEntity);
            setCurrentTraceId(newTraceId);
        });
    }

    public void deactivateTrace() {
        setCurrentTraceId(null);
    }

    public void activateLatestTrace() {
        mExecutors.diskIO().execute(() -> {
            final TraceEntity latestTrace = mDatabase.traceDao().loadLatestTrace();
            if (latestTrace != null) {
                final long traceId = latestTrace.getId();
                setCurrentTraceId(traceId);
                setCurrentLocationToStartOfTrace(traceId);
            } else {
                setCurrentTraceId(INITIAL_TRACE);
            }
        });
    }

    public void deleteCurrentTrace() {
        final Long currentTraceId = mCurrentTraceId.getValue();
        if (currentTraceId != null && currentTraceId != INITIAL_TRACE) {
            mExecutors.diskIO().execute(() -> mDatabase.runInTransaction(() -> {
                TraceEntity traceEntity = mDatabase.traceDao().loadTrace(currentTraceId);
                mDatabase.traceDao().delete(traceEntity);
                activateLatestTrace();
            }));
        }
    }

    @NonNull
    public LiveData<Boolean> isReplayActive() {
        return mIsReplayActive;
    }

    public void startReplayTrace(long traceId) {
        mIsReplayActive.setValue(true);
        mCurrentTraceId.setValue(traceId);
        mExecutors.diskIO().execute(() -> {
            List<LocationMeasurementEntity> completeTrace = mDatabase.locationMeasurementDao().loadLocationMeasurementsForTrace(traceId);
            List<LocationMeasurementEntity> replayTrace = new ArrayList<>();
            CellMeasurementEntity firstServingCellMeasurement = null;
            CellMeasurementEntity secondServingCellMeasurement = null;
            CellMeasurementEntity thirdServingCellMeasurement = null;
            if (completeTrace.size() > 0) {
                firstServingCellMeasurement =
                        getCurrentServingCellMeasurement(mDatabase.cellMeasurementDao().loadCellMeasurements(completeTrace.get(0).getId()));
            }
            if (completeTrace.size() > 1) {
                secondServingCellMeasurement =
                        getCurrentServingCellMeasurement(mDatabase.cellMeasurementDao().loadCellMeasurements(completeTrace.get(1).getId()));
            }
            if (completeTrace.size() > 2) {
                thirdServingCellMeasurement =
                        getCurrentServingCellMeasurement(mDatabase.cellMeasurementDao().loadCellMeasurements(completeTrace.get(2).getId()));
            }
            nextReplayStep(0, DELAY_BASE, completeTrace, replayTrace,
                    null, firstServingCellMeasurement, secondServingCellMeasurement, thirdServingCellMeasurement);
        });
    }

    private void nextReplayStep(
            final int currentPos,
            final long currentDelay,
            @NonNull final List<LocationMeasurementEntity> completeTrace,
            @NonNull final List<LocationMeasurementEntity> replayTrace,
            final CellMeasurementEntity previousServingCellMeasurement,
            final CellMeasurementEntity currentServingCellMeasurement,
            final CellMeasurementEntity nextServingCellMeasurement,
            final CellMeasurementEntity secondNextServingCellMeasurement) {
        if (currentPos < completeTrace.size()) {
            if (mIsReplayActive.getValue() != null && mIsReplayActive.getValue()) {
                mExecutors.diskIO().execute(() -> {
                    LocationMeasurementEntity currentLocationMeasurementEntity = completeTrace.get(currentPos);
                    setCurrentLocation(currentLocationMeasurementEntity.getLatLng());
                    replayTrace.add(currentLocationMeasurementEntity);
                    setCurrentReplayServingCellMeasurement(currentServingCellMeasurement);
                    boolean isCurrentHandover = isHandover(currentServingCellMeasurement, previousServingCellMeasurement);
                    boolean isNextHandover = isHandover(nextServingCellMeasurement, currentServingCellMeasurement);
                    boolean isSecondNextHandover = isHandover(secondNextServingCellMeasurement, nextServingCellMeasurement);
                    final long nextDelay;
                    if (isCurrentHandover) {
                        nextDelay = DELAY_BASE + 3 * DELAY_DELTA;
                    } else if (isNextHandover) {
                        nextDelay = DELAY_BASE + 2 * DELAY_DELTA;
                    } else if (isSecondNextHandover) {
                        nextDelay = DELAY_BASE + DELAY_DELTA;
                    } else if (currentDelay >= DELAY_BASE + DELAY_DELTA) {
                        nextDelay = currentDelay - DELAY_DELTA;
                    } else {
                        nextDelay = DELAY_BASE;
                    }
                    final CellMeasurementEntity thirdNextServingCellMeasurement;
                    if (currentPos + 3 < completeTrace.size()) {
                        thirdNextServingCellMeasurement =
                                getCurrentServingCellMeasurement(mDatabase.cellMeasurementDao().loadCellMeasurements(completeTrace.get(currentPos + 3).getId()));
                    } else {
                        thirdNextServingCellMeasurement = null;
                    }
                    mLocationMeasurementsForReplayTrace.postValue(replayTrace);
                    mHandler.postDelayed(() -> nextReplayStep(currentPos + 1, nextDelay, completeTrace, replayTrace,
                            currentServingCellMeasurement, nextServingCellMeasurement, secondNextServingCellMeasurement, thirdNextServingCellMeasurement),
                            nextDelay);
                });
            }
        } else {
            mIsReplayActive.postValue(false);
        }
    }

    @Nullable
    private CellMeasurementEntity getCurrentServingCellMeasurement(@Nullable List<CellMeasurementEntity> cellMeasurementEntities) {
        if (cellMeasurementEntities != null) {
            for (CellMeasurementEntity cellMeasurementEntity : cellMeasurementEntities) {
                if (cellMeasurementEntity.isRegistered()) {
                    return cellMeasurementEntity;
                }
            }
        }
        return null;
    }

    private boolean isHandover(@Nullable CellMeasurementEntity currentServingCellMeasurement, @Nullable CellMeasurementEntity previousServingCellMeasurement) {
        if (currentServingCellMeasurement != null && previousServingCellMeasurement != null) {
            int currentENodeB = currentServingCellMeasurement.getENodeB();
            int currentCid = currentServingCellMeasurement.getCid();
            int currentCi = IdUtil.getCi(currentENodeB, currentCid);
            int previousENodeB = previousServingCellMeasurement.getENodeB();
            int previousCid = previousServingCellMeasurement.getCid();
            int previousCi = IdUtil.getCi(previousENodeB, previousCid);
            return currentCi != previousCi;
        }
        return false;
    }

    public void stopReplayTrace() {
        mIsReplayActive.postValue(false);
    }

    void updateServingCellMeasurement(@Nullable CellInfoLte currentServingCell, int mServingCellSnr) {
        if (currentServingCell != null) {
            CellMeasurementEntity servingCellMeasurement = new CellMeasurementEntity(-1, null, currentServingCell, mServingCellSnr);
            mCurrentServingCellMeasurement.postValue(servingCellMeasurement);
        } else {
            mCurrentServingCellMeasurement.postValue(null);
        }
    }

    void updateHandover(@NonNull LatLng latLng, @NonNull CellInfoLte currentServingCell, @NonNull CellInfoLte previousServingCell) {
        mExecutors.diskIO().execute(() -> {
            long coord = mClusterGrid.getCoord(latLng);
            ClusterEntity clusterEntity = getClusterEntity(coord);
            List<HandoverEntity> existingHandovers = mDatabase.handoverDao().loadHandoversForClusterSync(coord);
            HandoverEntity handoverEntity = new HandoverEntity(coord, currentServingCell, previousServingCell);
            boolean includeHandover = true;
            for (HandoverEntity existingHandoverEntity : existingHandovers) {
                final int sourceCi = handoverEntity.getSourceCi();
                final int targetCi = handoverEntity.getTargetCi();

                final int existingSourceCi = existingHandoverEntity.getSourceCi();
                final int existingTargetCi = existingHandoverEntity.getTargetCi();

                if (sourceCi == existingSourceCi && targetCi == existingTargetCi) {
                    includeHandover = false;
                    break;
                }
            }
            if (includeHandover) {
                mDatabase.handoverDao().insert(handoverEntity);
            }
        });
    }

    void updateLocation(
            @NonNull final LatLng latLng,
            final List<CellInfo> cellInfos,
            final int dataActivity,
            final int dataState,
            final int servingCellSnr) {
        setCurrentLocation(latLng);
        mExecutors.diskIO().execute(() -> {
            CellInfoLte servingCell = null;
            List<CellInfoLte> cellInfoLtes = new ArrayList<>();
            if (cellInfos != null) {
                for (CellInfo cellInfo : cellInfos) {
                    if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                        cellInfoLtes.add(cellInfoLte);
                        if (cellInfoLte.isRegistered()) {
                            servingCell = cellInfoLte;
                        }
                    }
                }
            }
            if (servingCell != null) {
                CellIdentityLte cellIdentityLte = servingCell.getCellIdentity();
                int ci = cellIdentityLte.getCi();
                int eNodeBId = IdUtil.getENodeB(ci);
                int cid = IdUtil.getCid(ci);
                IdentifiedCellEntity identifiedCellEntity = mDatabase.identifiedCellDao().load(eNodeBId, cid);
                if (identifiedCellEntity != null) {
                    setCurrentIdentifiedCell(identifiedCellEntity);
                }
            }
            Long currentTraceId = mCurrentTraceId.getValue();
            if (currentTraceId != null && currentTraceId != INITIAL_TRACE) {
                updateTrace(currentTraceId, latLng, cellInfoLtes, dataActivity, dataState, servingCellSnr);
            }
            updateClusterGrid(latLng, cellInfoLtes, dataActivity, dataState, servingCellSnr);
        });
    }

    private void updateTrace(
            final long currentTraceId,
            @NonNull final LatLng latLng,
            @NonNull final List<CellInfoLte> cellInfoLtes,
            final int dataActivity,
            final int dataState,
            final int servingCellSnr) {
        mDatabase.runInTransaction(() -> {
            LocationMeasurementEntity locationMeasurementEntity =
                    new LocationMeasurementEntity(null, currentTraceId, mPreviousLocationMeasurementId, latLng, dataActivity, dataState);
            long locationId = mDatabase.locationMeasurementDao().insert(locationMeasurementEntity);
            List<CellMeasurementEntity> cellMeasurementEntities = getCellMeasurementEntities(cellInfoLtes, servingCellSnr, locationId, null);
            mDatabase.cellMeasurementDao().insertAll(cellMeasurementEntities);
            mPreviousLocationMeasurementId = locationId;
        });
    }

    private void updateClusterGrid(
            @NonNull final LatLng latLng,
            @NonNull final List<CellInfoLte> cellInfoLtes,
            final int dataActivity,
            final int dataState,
            final int servingCellSnr) {
        long coord = mClusterGrid.getCoord(latLng);
        ClusterEntity clusterEntity = getClusterEntity(coord);
        LocationMeasurementEntity locationMeasurementEntity =
                new LocationMeasurementEntity(coord, null, null, latLng, dataActivity, dataState);
        List<CellMeasurementEntity> existingCellMeasurementEntities =
                mDatabase.cellMeasurementDao().loadCellMeasurementsForClusterSync(coord);
        Set<EarfcnPciPair> existingServingEarfcnPciPairs = new HashSet<>();
        Set<EarfcnPciPair> existingNonServingEarfcnPciPairs = new HashSet<>();
        Map<EarfcnPciPair, CellMeasurementBounds> servingCellMeasurementBoundsMap = new HashMap<>();
        Map<EarfcnPciPair, CellMeasurementBounds> nonServingCellMeasurementBoundsMap = new HashMap<>();
        for (CellMeasurementEntity existingCellMeasurementEntity : existingCellMeasurementEntities) {
            EarfcnPciPair existingEarfcnPciPair =
                    new EarfcnPciPair(existingCellMeasurementEntity.getEarfcn(), existingCellMeasurementEntity.getPci());
            if (existingCellMeasurementEntity.isRegistered()) {
                CellMeasurementBounds servingCellMeasurementBounds = servingCellMeasurementBoundsMap.get(existingEarfcnPciPair);
                if (servingCellMeasurementBounds == null) {
                    servingCellMeasurementBounds = new CellMeasurementBounds();
                    servingCellMeasurementBoundsMap.put(existingEarfcnPciPair, servingCellMeasurementBounds);
                }
                servingCellMeasurementBounds.include(existingCellMeasurementEntity);
                existingServingEarfcnPciPairs.add(existingEarfcnPciPair);
            } else {
                CellMeasurementBounds nonServingCellMeasurementBounds = nonServingCellMeasurementBoundsMap.get(existingEarfcnPciPair);
                if (nonServingCellMeasurementBounds == null) {
                    nonServingCellMeasurementBounds = new CellMeasurementBounds();
                    nonServingCellMeasurementBoundsMap.put(existingEarfcnPciPair, nonServingCellMeasurementBounds);
                }
                nonServingCellMeasurementBounds.include(existingCellMeasurementEntity);
                existingNonServingEarfcnPciPairs.add(existingEarfcnPciPair);
            }
        }
        boolean includeLocationMeasurement = false;
        for (CellInfoLte cellInfoLte : cellInfoLtes) {
            CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
            EarfcnPciPair earfcnPciPair = new EarfcnPciPair(cellIdentityLte.getEarfcn(), cellIdentityLte.getPci());
            if (cellInfoLte.isRegistered()) {
                if (!existingServingEarfcnPciPairs.contains(earfcnPciPair)) {
                    includeLocationMeasurement = true;
                    break;
                }
                CellMeasurementBounds servingCellMeasurementBounds = servingCellMeasurementBoundsMap.get(earfcnPciPair);
                if (servingCellMeasurementBounds != null) {
                    CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                    if (servingCellMeasurementBounds.doesNotInclude(cellSignalStrengthLte, servingCellSnr)) {
                        includeLocationMeasurement = true;
                        break;
                    }
                }
            } else {
                if (!existingNonServingEarfcnPciPairs.contains(earfcnPciPair)) {
                    includeLocationMeasurement = true;
                    break;
                }
                CellMeasurementBounds nonServingCellMeasurementBounds = nonServingCellMeasurementBoundsMap.get(earfcnPciPair);
                if (nonServingCellMeasurementBounds != null) {
                    CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                    if (nonServingCellMeasurementBounds.doesNotInclude(cellSignalStrengthLte, Integer.MAX_VALUE)) {
                        includeLocationMeasurement = true;
                        break;
                    }
                }
            }
        }
        if (includeLocationMeasurement) {
            insertMeasurementAtCluster(latLng, clusterEntity, locationMeasurementEntity, cellInfoLtes, servingCellSnr);
        }
    }

    @NonNull
    private ClusterEntity getClusterEntity(long coord) {
        ClusterEntity clusterEntity = mDatabase.clusterDao().loadCluster(coord);
        if (clusterEntity == null) {
            clusterEntity = new ClusterEntity(coord, mClusterGrid.getLatLng(coord));
            mDatabase.clusterDao().insert(clusterEntity);
        }
        return clusterEntity;
    }

    private void insertMeasurementAtCluster(
            @NonNull final LatLng latLng,
            @NonNull final ClusterEntity clusterEntity,
            final LocationMeasurementEntity locationMeasurementEntity,
            @NonNull final List<CellInfoLte> cellInfoLtes,
            final int servingCellSnr) {
        mDatabase.runInTransaction(() -> {
            long coord = clusterEntity.getCoord();
            clusterEntity.updateLastMeasurement();
            mDatabase.clusterDao().update(clusterEntity);
            long locationId = mDatabase.locationMeasurementDao().insert(locationMeasurementEntity);
            List<CellMeasurementEntity> cellMeasurementEntities = getCellMeasurementEntities(cellInfoLtes, servingCellSnr, locationId, coord);
            mDatabase.cellMeasurementDao().insertAll(cellMeasurementEntities);
            insertIdentifiedENodeBAndCell(latLng, cellMeasurementEntities);
        });
    }

    private void insertIdentifiedENodeBAndCell(@NonNull final LatLng latLng, @NonNull final List<CellMeasurementEntity> cellMeasurementEntities) {
        boolean refreshNeeded = false;
        for (CellMeasurementEntity cellMeasurementEntity : cellMeasurementEntities) {
            final int eNodeBId = cellMeasurementEntity.getENodeB();
            final int earfcn = cellMeasurementEntity.getEarfcn();
            final int pci = cellMeasurementEntity.getPci();
            if (cellMeasurementEntity.isRegistered() && eNodeBId >= 0) {
                ENodeBEntity eNodeBEntity = new ENodeBEntity(eNodeBId);
                mDatabase.eNodeBDao().insertOrIgnoreIfAlreadyExists(eNodeBEntity);
                final int cid = cellMeasurementEntity.getCid();
                IdentifiedCellEntity identifiedCellEntity = mDatabase.identifiedCellDao().load(eNodeBId, cid);
                if (identifiedCellEntity == null) {
                    identifiedCellEntity = new IdentifiedCellEntity(eNodeBId, cid, pci, earfcn, latLng);
                    mDatabase.identifiedCellDao().insert(identifiedCellEntity);
                    Log.i(TAG, "New cell identified " + identifiedCellEntity);
                    refreshNeeded = true;
                } else {
                    LatLngBounds oldLatLngBounds = identifiedCellEntity.getBounds();
                    if (oldLatLngBounds.equals(UNDEFINED_BOUNDS)) {
                        oldLatLngBounds = new LatLngBounds(latLng, latLng);
                        identifiedCellEntity.setBounds(oldLatLngBounds);
                        identifiedCellEntity.setExtBounds(oldLatLngBounds);
                        mDatabase.identifiedCellDao().update(identifiedCellEntity);
                    } else {
                        LatLngBounds newLatLngBounds = oldLatLngBounds.including(latLng);
                        if (!newLatLngBounds.equals(oldLatLngBounds)) {
                            refreshNeeded = true;
                            identifiedCellEntity.setBounds(newLatLngBounds);
                            mDatabase.identifiedCellDao().update(identifiedCellEntity);
                        }
                    }
                }
            } else {
                List<IdentifiedCellEntity> identifiedCellEntities = mDatabase.identifiedCellDao().loadMatchingCells(earfcn, pci);
                IdentifiedCellEntity proximityCellCandidate = null;
                for (IdentifiedCellEntity identifiedCellEntity : identifiedCellEntities) {
                    LatLngBounds bounds = identifiedCellEntity.getBounds();
                    final LatLng cellCenter = bounds.getCenter();
                    final double distanceToCellCenter = SphericalUtil.computeDistanceBetween(cellCenter, latLng);
                    final double cellDiameter = SphericalUtil.computeDistanceBetween(bounds.northeast, bounds.southwest);
                    final double maxDistanceToCellCenter = Math.max(cellDiameter * 2.0, 500.0);
                    if (distanceToCellCenter < maxDistanceToCellCenter) {
                        if (proximityCellCandidate != null) {
                            Log.w(TAG, "PCI confusion: " + proximityCellCandidate + " and " + identifiedCellEntity);
                            mNoOfPciConfusion++;
                        } else {
                            proximityCellCandidate = identifiedCellEntity;
                        }
                    } else {
                        mNoOfTooDistantCellCandidates++;
                    }
                }
                if (proximityCellCandidate == null) {
                    mNoOfUnidentifiedCellCandidates++;
                } else {
                    if ((cellMeasurementEntity.getENodeB() == -1 || cellMeasurementEntity.getENodeB() == 0x7FFFFF) &&
                            cellMeasurementEntity.getCid() == 0xFF) {
                        mNoOfIdentifiedCellCandidates++;
                        cellMeasurementEntity.setENodeB(proximityCellCandidate.getENodeB());
                        cellMeasurementEntity.setCid(proximityCellCandidate.getCid());
                        mDatabase.cellMeasurementDao().update(cellMeasurementEntity);
                    } else if (cellMeasurementEntity.getENodeB() != proximityCellCandidate.getENodeB() && cellMeasurementEntity.getCid() != proximityCellCandidate.getCid()) {
                        mNoOfNotUpdatedCellCandidates++;
                        Log.w(TAG, "eNodeB and CID needs to be updated for " + cellMeasurementEntity);
                    }
                    if (!cellMeasurementEntity.isRegistered()) {
                        LatLngBounds oldLatLngBounds = proximityCellCandidate.getExtBounds();
                        if (oldLatLngBounds.equals(UNDEFINED_BOUNDS)) {
                            oldLatLngBounds = new LatLngBounds(latLng, latLng);
                            proximityCellCandidate.setBounds(oldLatLngBounds);
                            proximityCellCandidate.setExtBounds(oldLatLngBounds);
                            mDatabase.identifiedCellDao().update(proximityCellCandidate);
                        } else {
                            LatLngBounds newLatLngBounds = oldLatLngBounds.including(latLng);
                            if (!newLatLngBounds.equals(oldLatLngBounds)) {
                                refreshNeeded = true;
                                proximityCellCandidate.setExtBounds(newLatLngBounds);
                                mDatabase.identifiedCellDao().update(proximityCellCandidate);
                            }
                        }
                    }
                }
            }
        }
        if (refreshNeeded) {
            if (!mRefreshScheduled) {
                synchronized (CoverageRepository.this) {
                    if (!mRefreshScheduled) {
                        mRefreshScheduled = true;
                        mHandler.postDelayed(() -> {
                            mRefreshScheduled = false;
                            Log.i(TAG, "Running the scheduled refresh of identified ENodeBs and Cells");
                            // TODO: Call a lightweight refresh instead
                            // refreshIdentifiedENodeBsAndCells();
                        }, 60L * 1000L);
                        Log.i(TAG, "Refresh of identified ENodeBs and Cells scheduled in 60 seconds");
                    }
                }
            }
        }
    }

    public void refreshIdentifiedENodeBsAndCells() {
        mExecutors.backgroundIO().execute(() -> {
            List<LocationMeasurementEntity> locationMeasurementEntities = mDatabase.locationMeasurementDao().loadAllLocationMeasurements();
            Log.d(TAG, "Refreshing " + locationMeasurementEntities.size() + " location measurements");
            mNoOfIdentifiedCellCandidates = 0;
            mNoOfNotUpdatedCellCandidates = 0;
            mNoOfTooDistantCellCandidates = 0;
            mNoOfUnidentifiedCellCandidates = 0;
            mNoOfPciConfusion = 0;
            int noOfUpdatedCellMeasurementCoords = 0;
            for (LocationMeasurementEntity locationMeasurementEntity : locationMeasurementEntities) {
                final LatLng latLng = locationMeasurementEntity.getLatLng();
                final long locationId = locationMeasurementEntity.getId();
                final List<CellMeasurementEntity> cellMeasurementEntities =
                        mDatabase.cellMeasurementDao().loadCellMeasurements(locationId);
                insertIdentifiedENodeBAndCell(latLng, cellMeasurementEntities);

                final Long coord = locationMeasurementEntity.getCoord();
                if (coord != null) {
                    List<CellMeasurementEntity> updatedCellMeasurementEntities = new ArrayList<>();
                    for (CellMeasurementEntity cellMeasurementEntity : cellMeasurementEntities) {
                        if (!coord.equals(cellMeasurementEntity.getCoord())) {
                            cellMeasurementEntity.setCoord(coord);
                            updatedCellMeasurementEntities.add(cellMeasurementEntity);
                            noOfUpdatedCellMeasurementCoords++;
                        }
                    }
                    if (updatedCellMeasurementEntities.size() > 0) {
                        mDatabase.cellMeasurementDao().updateAll(updatedCellMeasurementEntities);
                    }
                }
            }
            Log.d(TAG, "No of identified cell candidates: " + mNoOfIdentifiedCellCandidates);
            Log.d(TAG, "No of not updated cell candidates: " + mNoOfNotUpdatedCellCandidates);
            Log.d(TAG, "No of too distant cell candidates: " + mNoOfTooDistantCellCandidates);
            Log.d(TAG, "No of unidentified cell candidates: " + mNoOfUnidentifiedCellCandidates);
            Log.d(TAG, "No of PCI confusion: " + mNoOfPciConfusion);
            Log.d(TAG, "No of updated cell measurements coords: " + noOfUpdatedCellMeasurementCoords);
        });
    }

    public void refreshHandovers() {
        mExecutors.backgroundIO().execute(() -> {
            List<HandoverEntity> handoverEntities = mDatabase.handoverDao().loadAllHandovers();
            Log.d(TAG, "Refreshing " + handoverEntities.size() + " handovers");
            handoverEntities.forEach(handoverEntity -> {
                if (((handoverEntity.getSourceENodeB() == -1 || handoverEntity.getSourceENodeB() == 0x7FFFFF) && handoverEntity.getSourceCid() == 0xFF) ||
                        ((handoverEntity.getTargetENodeB() == -1 || handoverEntity.getTargetENodeB() == 0x7FFFFF) && handoverEntity.getTargetCid() == 0xFF)) {
                    Log.d(TAG, "Removing " + handoverEntity);
                    mDatabase.handoverDao().delete(handoverEntity);
                }
            });
        });
    }

    public void refreshIdentifiedCells() {
        mExecutors.backgroundIO().execute(() -> {
            List<IdentifiedCellEntity> identifiedCellEntities = mDatabase.identifiedCellDao().loadAll();
            List<EarfcnPciPair> matchingEarfcnPciPairs = new ArrayList<>();
            for (IdentifiedCellEntity identifiedCellEntity : identifiedCellEntities) {
                final int earfcn = identifiedCellEntity.getEarfcn();
                final int pci = identifiedCellEntity.getPci();
                final EarfcnPciPair earfcnPciPair = new EarfcnPciPair(earfcn, pci);
                if (!matchingEarfcnPciPairs.contains(earfcnPciPair)) {
                    List<IdentifiedCellEntity> matchingIdentifiedCellEntities = mDatabase.identifiedCellDao().loadMatchingCells(earfcn, pci);
                    if (matchingIdentifiedCellEntities.size() > 1) {
                        matchingEarfcnPciPairs.add(earfcnPciPair);
                        Log.d(TAG, "Matching PCI " + pci + " for EARFCN " + earfcn);
                        for (IdentifiedCellEntity matchingIdentifiedCellEntity : matchingIdentifiedCellEntities) {
                            Log.d(TAG, matchingIdentifiedCellEntity.toString());
                        }
                    }
                }
            }
        });
    }

    private List<CellMeasurementEntity> getCellMeasurementEntities(@NonNull final List<CellInfoLte> cellInfoLtes, final int servingCellSnr, final long locationId, final Long coord) {
        return cellInfoLtes.stream().
                map(cellInfoLte -> new CellMeasurementEntity(locationId, coord, cellInfoLte, servingCellSnr)).collect(Collectors.toList());
    }

    @NonNull
    public LiveData<Boolean> getRequestingLocationUpdates() {
        return new SharedPreferenceBooleanLiveData(PreferenceManager.getDefaultSharedPreferences(mApplication),
                KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    boolean isRequestingLocationUpdates() {
        return PreferenceManager.getDefaultSharedPreferences(mApplication).
                getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    void setRequestingLocationUpdates(boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(mApplication).
                edit().putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates).apply();
        if (!requestingLocationUpdates) {
            setCurrentIdentifiedCell(null);
        }
    }
}
