package se.pcprogramkonsult.coveragemap.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import se.pcprogramkonsult.coveragemap.core.CoverageApp;
import se.pcprogramkonsult.coveragemap.core.CoverageRepository;
import se.pcprogramkonsult.coveragemap.db.entity.CellMeasurementEntity;
import se.pcprogramkonsult.coveragemap.db.entity.ClusterEntity;
import se.pcprogramkonsult.coveragemap.db.entity.ENodeBEntity;
import se.pcprogramkonsult.coveragemap.db.entity.HandoverEntity;
import se.pcprogramkonsult.coveragemap.db.entity.IdentifiedCellEntity;
import se.pcprogramkonsult.coveragemap.db.entity.IdentifiedCellWithENodeB;
import se.pcprogramkonsult.coveragemap.db.entity.LocationMeasurementEntity;
import se.pcprogramkonsult.coveragemap.db.entity.TraceEntity;
import se.pcprogramkonsult.coveragemap.lte.Earfcn;
import se.pcprogramkonsult.coveragemap.lte.IdUtil;
import se.pcprogramkonsult.coveragemap.lte.operation.MaxOperation;
import se.pcprogramkonsult.coveragemap.lte.operation.MeasurementOperation;
import se.pcprogramkonsult.coveragemap.lte.parameter.MeasurementParameter;
import se.pcprogramkonsult.coveragemap.lte.parameter.RsrpParameter;
import se.pcprogramkonsult.coveragemap.ui.HandoverType;

public class CoverageMapViewModel extends AndroidViewModel {

    public class CombinedENodeBFilter {
        public final Integer selectedENodeBId;
        public final Integer selectedCi;

        CombinedENodeBFilter(final Integer selectedENodeBId, final Integer selectedCi) {
            this.selectedENodeBId = selectedENodeBId;
            this.selectedCi = selectedCi;
        }
    }

    public class CombinedCellFilter extends CombinedENodeBFilter {
        public final Integer selectedEarfcn;

        CombinedCellFilter(final Integer selectedENodeBId, final Integer selectedCi, final Integer selectedEarfcn) {
            super(selectedENodeBId, selectedCi);
            this.selectedEarfcn = selectedEarfcn;
        }
    }

    class CombinedClusterFilter extends CombinedCellFilter {
        final LatLngBounds visibleBounds;

        CombinedClusterFilter(final Integer selectedENodeBId, final Integer selectedCi, final Integer selectedEarfcn, final LatLngBounds visibleBounds) {
            super(selectedENodeBId, selectedCi, selectedEarfcn);
            this.visibleBounds = visibleBounds;
        }
    }

    public class CombinedHandoverFilter extends CombinedCellFilter {
        public final HandoverType handoverType;

        CombinedHandoverFilter(final Integer selectedENodeBId, final Integer selectedCi, final Integer selectedEarfcn, final HandoverType handoverType) {
            super(selectedENodeBId, selectedCi, selectedEarfcn);
            this.handoverType = handoverType;
        }
    }

    class CombinedLocationMeasurementFilter {
        final Long selectedTraceId;
        final Boolean isReplayActive;
        final LatLngBounds visibleBounds;

        CombinedLocationMeasurementFilter(final Long selectedTraceId, final Boolean isReplayActive, final LatLngBounds visibleBounds) {
            this.selectedTraceId = selectedTraceId;
            this.isReplayActive = isReplayActive;
            this.visibleBounds = visibleBounds;
        }
    }

    private final CoverageRepository mRepository;

    private final LiveData<List<ENodeBEntity>> mAllENodeBs;
    private final LiveData<List<IdentifiedCellWithENodeB>> mAllIdentifiedCells;
    private final LiveData<List<TraceEntity>> mAllTraces;
    @NonNull
    private final LiveData<List<Earfcn>> mUniqueEarfcns;
    @NonNull
    private final LiveData<Long> mCurrentTraceId;
    @NonNull
    private final LiveData<Boolean> mIsTraceActive;
    @NonNull
    private final LiveData<Boolean> mIsReplayActive;

    @NonNull
    private final LiveData<List<ENodeBEntity>> mSelectedENodeBs;
    @NonNull
    private final LiveData<List<ClusterEntity>> mFilteredClusters;
    @NonNull
    private final LiveData<List<ClusterEntity>> mZoomedClusters;
    @NonNull
    private final LiveData<List<ClusterEntity>> mSelectedClusters;
    @NonNull
    private final LiveData<List<IdentifiedCellWithENodeB>> mFilteredIdentifiedCells;
    @NonNull
    private final LiveData<List<IdentifiedCellWithENodeB>> mSelectedIdentifiedCells;
    @NonNull
    private final LiveData<List<LocationMeasurementEntity>> mSelectedLocationMeasurements;
    @NonNull
    private final LiveData<List<LocationMeasurementEntity>> mLocationMeasurementsForReplayTrace;

    private final MutableLiveData<Float> mZoomLevel = new MutableLiveData<>();
    private final MutableLiveData<LatLngBounds> mVisibleBounds = new MutableLiveData<>();
    private final MutableLiveData<Integer> mSelectedENodeBId = new MutableLiveData<>();
    private final MutableLiveData<Integer> mSelectedCi = new MutableLiveData<>();
    private final MutableLiveData<Integer> mSelectedEarfcn = new MutableLiveData<>();
    private final MutableLiveData<CameraPosition> mCameraPosition = new MutableLiveData<>();
    private final MutableLiveData<MeasurementOperation> mSelectedOperation = new MutableLiveData<>();
    private final MutableLiveData<MeasurementParameter> mSelectedParameter = new MutableLiveData<>();
    private final MutableLiveData<HandoverType> mSelectedHandoverType = new MutableLiveData<>();

    private final MutableLiveData<List<ClusterEntity>> mNoClusters = new MutableLiveData<>();
    private final MutableLiveData<List<IdentifiedCellWithENodeB>> mNoIdentifiedCells = new MutableLiveData<>();
    private final MutableLiveData<List<LocationMeasurementEntity>> mNoLocationMeasurements = new MutableLiveData<>();
    private final MutableLiveData<List<HandoverEntity>> mNoHandovers = new MutableLiveData<>();
    private final MutableLiveData<ENodeBEntity> mNoENodeB = new MutableLiveData<>();

    private final MediatorLiveData<CombinedENodeBFilter> mCombinedENodeBFilter = new MediatorLiveData<>();
    private final MediatorLiveData<CombinedCellFilter> mCombinedCellFilter = new MediatorLiveData<>();
    private final MediatorLiveData<CombinedClusterFilter> mCombinedClusterFilter = new MediatorLiveData<>();
    private final MediatorLiveData<CombinedHandoverFilter> mCombinedHandoverFilter = new MediatorLiveData<>();
    private final MediatorLiveData<CombinedLocationMeasurementFilter> mCombinedLocationMeasurementFilter = new MediatorLiveData<>();
    private final MediatorLiveData<CameraUpdate> mCameraUpdate = new MediatorLiveData<>();
    private final MediatorLiveData<String> mCurrentLegend = new MediatorLiveData<>();

    public CoverageMapViewModel(@NonNull final Application application) {
        super(application);

        mRepository = ((CoverageApp) application).getRepository();

        mNoClusters.setValue(new ArrayList<>());
        mNoIdentifiedCells.setValue(new ArrayList<>());
        mNoLocationMeasurements.setValue(new ArrayList<>());
        mNoHandovers.setValue(new ArrayList<>());
        mNoENodeB.setValue(null);

        mSelectedParameter.setValue(new RsrpParameter());
        mSelectedOperation.setValue(new MaxOperation());
        mSelectedHandoverType.setValue(HandoverType.BOTH);

        mAllENodeBs = mRepository.getAllENodeBs();
        mAllIdentifiedCells = mRepository.getAllIdentifiedCellsWithENodeB();
        mAllTraces = mRepository.getAllTraces();
        mUniqueEarfcns = mRepository.getUniqueEarfcns();
        mCurrentTraceId = mRepository.getCurrentTraceId();
        mLocationMeasurementsForReplayTrace = mRepository.getLocationMeasurementsForReplayTrace();

        mIsTraceActive = Transformations.map(mCurrentTraceId, Objects::nonNull);
        mIsReplayActive = mRepository.isReplayActive();

        LiveData<LatLng> initialLocation = mRepository.getInitialLocation();
        LiveData<LatLng> currentLocation = mRepository.getCurrentLocation();

        mCameraUpdate.addSource(mCameraPosition, cameraPosition ->
                mCameraUpdate.setValue(CameraUpdateFactory.newCameraPosition(cameraPosition)));
        mCameraUpdate.addSource(initialLocation, latLng ->
                mCameraUpdate.setValue(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f)));
        mCameraUpdate.addSource(currentLocation, latLng ->
                mCameraUpdate.setValue(CameraUpdateFactory.newLatLng(latLng)));

        mCombinedENodeBFilter.addSource(mSelectedENodeBId, value -> updateCombinedENodeBFilter());
        mCombinedENodeBFilter.addSource(mSelectedCi, value -> updateCombinedENodeBFilter());

        mSelectedENodeBs = Transformations.switchMap(mCombinedENodeBFilter, combinedFilter -> {
            if (combinedFilter.selectedCi != null) {
                return mRepository.getENodeBs(IdUtil.getENodeB(combinedFilter.selectedCi));
            } else if (combinedFilter.selectedENodeBId != null) {
                return mRepository.getENodeBs(combinedFilter.selectedENodeBId);
            } else {
                return mAllENodeBs;
            }
        });

        mCombinedCellFilter.addSource(mSelectedENodeBId, value -> updateCombinedCellFilter());
        mCombinedCellFilter.addSource(mSelectedCi, value -> updateCombinedCellFilter());
        mCombinedCellFilter.addSource(mSelectedEarfcn, value -> updateCombinedCellFilter());

        mFilteredIdentifiedCells = Transformations.switchMap(mCombinedCellFilter, combinedFilter -> {
            if (combinedFilter.selectedCi != null) {
                return mRepository.getIdentifiedCellWithENodeB(combinedFilter.selectedCi);
            } else if (combinedFilter.selectedENodeBId != null) {
                if (combinedFilter.selectedEarfcn != null) {
                    return mRepository.getIdentifiedCellsWithENodeBForEarfcnAndENodeB(combinedFilter.selectedEarfcn, combinedFilter.selectedENodeBId);
                } else {
                    return mRepository.getIdentifiedCellsWithENodeBForENodeB(combinedFilter.selectedENodeBId);
                }
            } else if (combinedFilter.selectedEarfcn != null) {
                return mRepository.getIdentifiedCellsWithENodeBForEarfcn(combinedFilter.selectedEarfcn);
            } else {
                return mAllIdentifiedCells;
            }
        });

        mSelectedIdentifiedCells = Transformations.switchMap(mZoomLevel, zoomLevel -> {
            if (zoomLevel < 13.0) {
                return mNoIdentifiedCells;
            } else {
                return mFilteredIdentifiedCells;
            }
        });

        mCombinedClusterFilter.addSource(mSelectedENodeBId, value -> updateCombinedClusterFilter());
        mCombinedClusterFilter.addSource(mSelectedCi, value -> updateCombinedClusterFilter());
        mCombinedClusterFilter.addSource(mSelectedEarfcn, value -> updateCombinedClusterFilter());
        mCombinedClusterFilter.addSource(mVisibleBounds, value -> updateCombinedClusterFilter());

        mFilteredClusters = Transformations.switchMap(mCombinedClusterFilter, combinedFilter -> {
            if (combinedFilter.visibleBounds != null) {
                if (combinedFilter.selectedCi != null) {
                    return mRepository.getClustersForCiWithinBounds(
                            combinedFilter.selectedCi, combinedFilter.visibleBounds);
                } else if (combinedFilter.selectedENodeBId != null) {
                    if (combinedFilter.selectedEarfcn != null) {
                        return mRepository.getClustersForEarfcnAndENodeBWithinBounds(
                                combinedFilter.selectedEarfcn, combinedFilter.selectedENodeBId, combinedFilter.visibleBounds);
                    } else {
                        return mRepository.getClustersForENodeBWithinBounds(
                                combinedFilter.selectedENodeBId, combinedFilter.visibleBounds);
                    }
                } else if (combinedFilter.selectedEarfcn != null) {
                    return mRepository.getClustersForEarfcnWithinBounds(
                            combinedFilter.selectedEarfcn, combinedFilter.visibleBounds);
                } else {
                    return mRepository.getClustersWithinBounds(combinedFilter.visibleBounds);
                }
            } else {
                return mNoClusters;
            }
        });

        mCombinedHandoverFilter.addSource(mSelectedENodeBId, value -> updateCombinedHandoverFilter());
        mCombinedHandoverFilter.addSource(mSelectedCi, value -> updateCombinedHandoverFilter());
        mCombinedHandoverFilter.addSource(mSelectedEarfcn, value -> updateCombinedHandoverFilter());
        mCombinedHandoverFilter.addSource(mSelectedHandoverType, value -> updateCombinedHandoverFilter());

        mZoomedClusters = Transformations.switchMap(mZoomLevel, zoomLevel -> {
            if (zoomLevel < 15.0) {
                return mNoClusters;
            } else {
                return mFilteredClusters;
            }
        });

        mSelectedClusters = Transformations.switchMap(mCurrentTraceId, currentTraceId -> {
            if (currentTraceId != null) {
                return mNoClusters;
            } else {
                return mZoomedClusters;
            }
        });

        mCombinedLocationMeasurementFilter.addSource(mVisibleBounds, value -> updateCombinedLocationMeasurementFilter());
        mCombinedLocationMeasurementFilter.addSource(mCurrentTraceId, value -> updateCombinedLocationMeasurementFilter());
        mCombinedLocationMeasurementFilter.addSource(mIsReplayActive, value -> updateCombinedLocationMeasurementFilter());

        mSelectedLocationMeasurements = Transformations.switchMap(mCombinedLocationMeasurementFilter, combinedFilter -> {
            if (combinedFilter.isReplayActive != null && combinedFilter.isReplayActive) {
                return mLocationMeasurementsForReplayTrace;
            } else if (combinedFilter.visibleBounds != null) {
                if (combinedFilter.selectedTraceId != null) {
                    return mRepository.getLocationMeasurementsForTraceWithinBounds(
                            combinedFilter.selectedTraceId, combinedFilter.visibleBounds);
                } else {
                    return mNoLocationMeasurements;
                }
            } else {
                return mNoLocationMeasurements;
            }
        });

        mCurrentLegend.addSource(mSelectedOperation, operation -> {
            if (operation != null && mSelectedParameter.getValue() != null) {
                mCurrentLegend.setValue(operation.getName() + " " + mSelectedParameter.getValue().getName());
            }
        });
        mCurrentLegend.addSource(mSelectedParameter, parameter -> {
            if (parameter != null && mSelectedOperation.getValue() != null) {
                mCurrentLegend.setValue(mSelectedOperation.getValue().getName() + " " + parameter.getName());
            }
        });

        updateCombinedENodeBFilter();
        updateCombinedCellFilter();
        updateCombinedClusterFilter();
    }

    private void updateCombinedENodeBFilter() {
        mCombinedENodeBFilter.setValue(new CombinedENodeBFilter(
                mSelectedENodeBId.getValue(), mSelectedCi.getValue()));
    }

    private void updateCombinedCellFilter() {
        mCombinedCellFilter.setValue(new CombinedCellFilter(
                mSelectedENodeBId.getValue(), mSelectedCi.getValue(), mSelectedEarfcn.getValue()));
    }

    private void updateCombinedClusterFilter() {
        mCombinedClusterFilter.setValue(new CombinedClusterFilter(
                mSelectedENodeBId.getValue(), mSelectedCi.getValue(), mSelectedEarfcn.getValue(), mVisibleBounds.getValue()));
    }

    private void updateCombinedHandoverFilter() {
        mCombinedHandoverFilter.setValue(new CombinedHandoverFilter(
                mSelectedENodeBId.getValue(), mSelectedCi.getValue(), mSelectedEarfcn.getValue(), mSelectedHandoverType.getValue()));
    }

    private void updateCombinedLocationMeasurementFilter() {
        mCombinedLocationMeasurementFilter.setValue(new CombinedLocationMeasurementFilter(
                mCurrentTraceId.getValue(), mIsReplayActive.getValue(), mVisibleBounds.getValue()));
    }

    public LiveData<List<ENodeBEntity>> getAllENodeBs() {
        return mAllENodeBs;
    }

    public LiveData<ENodeBEntity> getENodeB(int eNodeBId) {
        return mRepository.getENodeB(eNodeBId);
    }

    public void updateENodeB(ENodeBEntity eNodeB) {
        mRepository.updateENodeB(eNodeB);
    }

    @NonNull
    public LiveData<List<ENodeBEntity>> getSelectedENodeBs() {
        return mSelectedENodeBs;
    }

    @NonNull
    public LiveData<List<ClusterEntity>> getSelectedClusters() {
        return mSelectedClusters;
    }

    @NonNull
    public LiveData<List<LocationMeasurementEntity>> getSelectedLocationMeasurements() {
        return mSelectedLocationMeasurements;
    }

    @NonNull
    public LiveData<HandoverEntitiesWithSelectedCi> getHandoversWithSelectedCiForCluster(long coord) {
        final LiveData<List<HandoverEntity>> handoversForCluster = Transformations.switchMap(mCombinedHandoverFilter, combinedFilter -> {
            if (combinedFilter.handoverType == HandoverType.NONE) {
                return mNoHandovers;
            } else if (combinedFilter.selectedCi != null) {
                switch (combinedFilter.handoverType) {
                    case INTRA:
                        return mRepository.getIntraHandoversForClusterAndCi(coord, combinedFilter.selectedCi);
                    case INTER:
                        return mRepository.getInterHandoversForClusterAndCi(coord, combinedFilter.selectedCi);
                    default:
                        return mRepository.getHandoversForClusterAndCi(coord, combinedFilter.selectedCi);
                }
            } else if (combinedFilter.selectedENodeBId != null) {
                if (combinedFilter.selectedEarfcn != null) {
                    switch (combinedFilter.handoverType) {
                        case INTRA:
                            return mRepository.getIntraHandoversForClusterAndENodeBAndEarfcn(coord,
                                    combinedFilter.selectedENodeBId, combinedFilter.selectedEarfcn);
                        case INTER:
                            return mRepository.getInterHandoversForClusterAndENodeBAndEarfcn(coord,
                                    combinedFilter.selectedENodeBId, combinedFilter.selectedEarfcn);
                        default:
                            return mRepository.getHandoversForClusterAndENodeBAndEarfcn(coord,
                                    combinedFilter.selectedENodeBId, combinedFilter.selectedEarfcn);
                    }
                } else {
                    switch (combinedFilter.handoverType) {
                        case INTRA:
                            return mRepository.getIntraHandoversForClusterAndENodeB(coord, combinedFilter.selectedENodeBId);
                        case INTER:
                            return mRepository.getInterHandoversForClusterAndENodeB(coord, combinedFilter.selectedENodeBId);
                        default:
                            return mRepository.getHandoversForClusterAndENodeB(coord, combinedFilter.selectedENodeBId);
                    }
                }
            } else if (combinedFilter.selectedEarfcn != null) {
                switch (combinedFilter.handoverType) {
                    case INTRA:
                        return mRepository.getIntraHandoversForClusterAndEarfcn(coord, combinedFilter.selectedEarfcn);
                    case INTER:
                        return mRepository.getInterHandoversForClusterAndEarfcn(coord, combinedFilter.selectedEarfcn);
                    default:
                        return mRepository.getHandoversForClusterAndEarfcn(coord, combinedFilter.selectedEarfcn);
                }
            } else {
                switch (combinedFilter.handoverType) {
                    case INTRA:
                        return mRepository.getIntraHandoversForCluster(coord);
                    case INTER:
                        return mRepository.getInterHandoversForCluster(coord);
                    default:
                        return mRepository.getHandoversForCluster(coord);
                }
            }
        });
        final MediatorLiveData<HandoverEntitiesWithSelectedCi> result = new MediatorLiveData<>();
        result.addSource(mSelectedCi, selectedCi ->
                result.setValue(new HandoverEntitiesWithSelectedCi(selectedCi, handoversForCluster.getValue(), mZoomLevel.getValue())));
        result.addSource(handoversForCluster, handoverEntities ->
                result.setValue(new HandoverEntitiesWithSelectedCi(mSelectedCi.getValue(), handoverEntities, mZoomLevel.getValue())));
        result.addSource(mZoomLevel, zoomLevel ->
                result.setValue(new HandoverEntitiesWithSelectedCi(mSelectedCi.getValue(), handoversForCluster.getValue(), zoomLevel)));
        return result;
    }

    @NonNull
    public LiveData<CellMeasurementsWithSelectedOperationAndParameter> getCellMeasurementsWithSelectedOperationForCluster(long coord) {
        final LiveData<List<CellMeasurementEntity>> cellMeasurementsForCluster = Transformations.switchMap(mCombinedCellFilter, combinedFilter -> {
            if (combinedFilter.selectedCi != null) {
                return mRepository.getCellMeasurementsForClusterAndCi(coord, combinedFilter.selectedCi);
            } else if (combinedFilter.selectedENodeBId != null) {
                if (combinedFilter.selectedEarfcn != null) {
                    return mRepository.getCellMeasurementsForClusterAndEarfcnAndENodeB(coord,
                            combinedFilter.selectedEarfcn, combinedFilter.selectedENodeBId);
                } else {
                    return mRepository.getCellMeasurementsForClusterAndENodeB(coord, combinedFilter.selectedENodeBId);
                }
            } else if (combinedFilter.selectedEarfcn != null) {
                return mRepository.getCellMeasurementsForClusterAndEarfcn(coord, combinedFilter.selectedEarfcn);
            } else {
                return mRepository.getCellMeasurementsForCluster(coord);
            }
        });
        final MediatorLiveData<CellMeasurementsWithSelectedOperationAndParameter> result = new MediatorLiveData<>();
        result.addSource(mSelectedOperation, selectedOperation ->
                result.setValue(new CellMeasurementsWithSelectedOperationAndParameter(
                        selectedOperation, mSelectedParameter.getValue(), cellMeasurementsForCluster.getValue())));
        result.addSource(mSelectedParameter, selectedParameter ->
                result.setValue(new CellMeasurementsWithSelectedOperationAndParameter(
                        mSelectedOperation.getValue(), selectedParameter, cellMeasurementsForCluster.getValue())));
        result.addSource(cellMeasurementsForCluster, cellMeasurementEntities ->
                result.setValue(new CellMeasurementsWithSelectedOperationAndParameter(
                        mSelectedOperation.getValue(), mSelectedParameter.getValue(), cellMeasurementEntities)));
        return result;
    }

    @NonNull
    public LiveData<CellMeasurementsWithSelectedParameter> getCellMeasurementsForLocationMeasurements(Long currLocationId, Long prevLocationId) {
        final LiveData<List<CellMeasurementEntity>> cellMeasurementsForLocationMeasurements =
                mRepository.getCellMeasurementsForLocationMeasurements(currLocationId, prevLocationId);
        final MediatorLiveData<CellMeasurementsWithSelectedParameter> result = new MediatorLiveData<>();
        result.addSource(mSelectedParameter, selectedParameter ->
                result.setValue(new CellMeasurementsWithSelectedParameter(
                        selectedParameter, cellMeasurementsForLocationMeasurements.getValue())));
        result.addSource(cellMeasurementsForLocationMeasurements, cellMeasurementEntities ->
                result.setValue(new CellMeasurementsWithSelectedParameter(
                        mSelectedParameter.getValue(), cellMeasurementEntities)));
        return result;
    }

    @NonNull
    public LiveData<List<Earfcn>> getUniqueEarfcns() {
        return mUniqueEarfcns;
    }

    @NonNull
    public LiveData<List<IdentifiedCellWithENodeB>> getSelectedIdentifiedCellsWithENodeB() {
        return mSelectedIdentifiedCells;
    }

    public LiveData<List<IdentifiedCellEntity>> getIdentifiedCellsForENodeB(int eNodeBId) {
        return mRepository.getIdentifiedCellsForENodeB(eNodeBId);
    }

    @NonNull
    public LiveData<CellMeasurementEntity> getCurrentServingCellMeasurement() {
        return Transformations.switchMap(mIsReplayActive, isReplayActive -> {
            if (isReplayActive != null && isReplayActive) {
                return mRepository.getCurrentReplayServingCellMeasurement();
            } else {
                return mRepository.getCurrentServingCellMeasurement();
            }
        });
    }

    @NonNull
    public LiveData<ENodeBEntity> getCurrentENodeB() {
        return Transformations.switchMap(getCurrentServingCellMeasurement(), currentServingCellMeasurement -> {
            if (currentServingCellMeasurement != null) {
                return mRepository.getENodeB(currentServingCellMeasurement.getENodeB());
            } else {
                return mNoENodeB;
            }
        });
    }

    @NonNull
    public LiveData<IdentifiedCellEntity> getCurrentIdentifiedCell() {
        return mRepository.getCurrentIdentifiedCell();
    }

    @NonNull
    public LiveData<String> getCurrentLegend() {
        return mCurrentLegend;
    }

    public void selectAllENodeBs() {
        mSelectedENodeBId.setValue(null);
    }

    public void selectENodeB(final int eNodeBId) {
        mSelectedENodeBId.setValue(eNodeBId);
    }

    public void selectAllCi() {
        mSelectedCi.setValue(null);
    }

    public void selectCi(final int ci) {
        mSelectedCi.setValue(ci);
    }

    public void selectAllEarfcns() {
        mSelectedEarfcn.setValue(null);
    }

    public void selectEarfcn(final int earfcn) {
        mSelectedEarfcn.setValue(earfcn);
    }

    public void selectMeasurementOperation(final MeasurementOperation operation) {
        mSelectedOperation.setValue(operation);
    }

    public void selectMeasurementParameter(final MeasurementParameter parameter) {
        mSelectedParameter.setValue(parameter);
    }

    @NonNull
    public LiveData<MeasurementParameter> getSelectedMeasurementParameter() {
        return mSelectedParameter;
    }

    public void selectHandoverType(final HandoverType handoverType) {
        mSelectedHandoverType.setValue(handoverType);
    }

    @NonNull
    public LiveData<Boolean> getRequestingLocationUpdates() {
        return mRepository.getRequestingLocationUpdates();
    }

    public void setCameraPosition(CameraPosition cameraPosition) {
        mCameraPosition.setValue(cameraPosition);
    }

    @NonNull
    public LiveData<CameraUpdate> getCameraUpdate() {
        return mCameraUpdate;
    }

    public void setZoomLevel(final float zoom) {
        mZoomLevel.setValue(zoom);
    }

    public void setVisibleBounds(LatLngBounds latLngBounds) {
        mVisibleBounds.setValue(latLngBounds);
    }

    public void refreshIdentifiedENodeBsAndCells() {
        mRepository.refreshIdentifiedENodeBsAndCells();
    }

    public void refreshHandovers() {
        mRepository.refreshHandovers();
    }

    public void refreshIdentifiedCells() {
        mRepository.refreshIdentifiedCells();
    }

    @NonNull
    public LiveData<Boolean> isTraceActive() {
        return mIsTraceActive;
    }

    public void activateLatestTrace() {
        mRepository.activateLatestTrace();
    }

    public void activateTrace(long traceId) {
        mRepository.activateTrace(traceId);
    }

    public void deactivateTrace() {
        mRepository.deactivateTrace();
    }

    public void startNewTrace() {
        mRepository.startNewTrace();
    }

    public void deleteCurrentTrace() {
        mRepository.deleteCurrentTrace();
    }

    public void startReplayTrace(long traceId) {
        mRepository.startReplayTrace(traceId);
    }

    public void stopReplayTrace() {
        mRepository.stopReplayTrace();
    }

    @NonNull
    public LiveData<Boolean> isReplayActive() {
        return mIsReplayActive;
    }

    public LiveData<List<TraceEntity>> getAllTraces() {
        return mAllTraces;
    }

    @NonNull
    public LiveData<CombinedHandoverFilter> getCurrentFilter() {
        return mCombinedHandoverFilter;
    }
}
