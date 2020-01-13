package se.pcprogramkonsult.coveragemap.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import se.pcprogramkonsult.coveragemap.R;
import se.pcprogramkonsult.coveragemap.core.TrackingService;
import se.pcprogramkonsult.coveragemap.db.entity.CellMeasurementEntity;
import se.pcprogramkonsult.coveragemap.db.entity.ENodeBEntity;
import se.pcprogramkonsult.coveragemap.db.entity.HandoverEntity;
import se.pcprogramkonsult.coveragemap.db.entity.IdentifiedCellEntity;
import se.pcprogramkonsult.coveragemap.lte.Earfcn;
import se.pcprogramkonsult.coveragemap.lte.FrequencyBand;
import se.pcprogramkonsult.coveragemap.lte.IdUtil;
import se.pcprogramkonsult.coveragemap.lte.operation.MeasurementOperation;
import se.pcprogramkonsult.coveragemap.lte.parameter.MeasurementParameter;
import se.pcprogramkonsult.coveragemap.viewmodel.CellMeasurementsWithSelectedOperationAndParameter;
import se.pcprogramkonsult.coveragemap.viewmodel.CellMeasurementsWithSelectedParameter;
import se.pcprogramkonsult.coveragemap.viewmodel.CoverageMapViewModel;
import se.pcprogramkonsult.coveragemap.viewmodel.HandoverEntitiesWithSelectedCi;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST = 0x1000;

    private final static String KEY_CAMERA_POSITION = "camera-position";

    private CoverageMapViewModel mViewModel;

    @Nullable
    private GoogleMap mMap = null;

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, Marker> mENodeBMarkers = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private final Map<Integer, Marker> mIdentifiedCellMarkers = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private final Map<Long, Circle> mClusterCircles = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private final Map<Long, Circle> mLocationMeasurementCircles = new HashMap<>();

    @Nullable
    private Circle mCurrentIdentifiedCellCircle = null;

    @Nullable
    private BitmapDescriptor mRadioTowerBitmap = null;
    @Nullable
    private BitmapDescriptor mRadioTowerShadowBitmap = null;
    @Nullable
    private BitmapDescriptor mCellBitmap = null;
    @Nullable
    private BitmapDescriptor mCellShadowBitmap = null;
    @Nullable
    private BitmapDescriptor mArrowUpBitmap = null;
    @Nullable
    private BitmapDescriptor mArrowUpSlopedBitmap = null;
    @Nullable
    private BitmapDescriptor mArrowDownBitmap = null;
    @Nullable
    private BitmapDescriptor mArrowDownSlopedBitmap = null;
    @Nullable
    private BitmapDescriptor mArrowRightBitmap = null;

    private NavigationView mNavigationView;
    private FloatingActionButton mFab;

    private TextView mENodeBNameTextView;
    private TextView mENodeBTextView;
    private TextView mCidTextView;
    private TextView mEarfcnTextView;
    private TextView mPciTextView;
    private TextView mTacTextView;

    private TextView mRsrpTextView;
    private TextView mRsrqTextView;
    private TextView mTaTextView;
    private TextView mRssiTextView;
    private TextView mSnrTextView;

    private RelativeLayout mFilterGroup;
    private TextView mFilterTextView;

    private ParameterLegendView mParameterLegendView;
    private TextView mMinLegendLabelView;
    private TextView mMaxLegendLabelView;
    private TextView mLegendTextView;

    @Nullable
    private MenuItem mSelectTraceMenuItem = null;
    @Nullable
    private MenuItem mReplayTraceMenuItem = null;
    @Nullable
    private MenuItem mDeleteTraceMenuItem = null;

    @Nullable
    private Boolean mIsRequestingLocationUpdates = null;
    @Nullable
    private Boolean mIsReplayActive = null;
    private boolean mInitialLocationNeedsUpdate = false;
    private int mCurrentNavigationId = -1;

    private class ENodeBMarkerTag {
        final int mENodeBId;
        @Nullable
        LiveData<List<IdentifiedCellEntity>> mIdentifiedCellsForENodeB;

        ENodeBMarkerTag(int eNodeBId) {
            mENodeBId = eNodeBId;
            mIdentifiedCellsForENodeB = null;
        }
    }

    private class IdentifiedCellMarkerTag {
        final int mCi;
        final Marker mExtCellMarker;
        final Circle mCellCircle;
        final Circle mExtCellCircle;

        IdentifiedCellMarkerTag(int ci, Marker extCellMarker, Circle cellCircle, Circle extCellCircle) {
            mCi = ci;
            mExtCellMarker = extCellMarker;
            mCellCircle = cellCircle;
            mExtCellCircle = extCellCircle;
        }
    }

    private class ClusterCircleTag {
        boolean isServingCluster;
        boolean isHandoverCluster;
        @Nullable
        Marker mHandoverMarker;
        @Nullable
        LiveData<HandoverEntitiesWithSelectedCi> mHandoverEntitiesWithSelectedCi;
        @Nullable
        LiveData<CellMeasurementsWithSelectedOperationAndParameter> mCellMeasurementsWithOperationAndParameter;

        ClusterCircleTag() {
            isServingCluster = false;
            isHandoverCluster = false;
            mHandoverMarker = null;
            mHandoverEntitiesWithSelectedCi = null;
            mCellMeasurementsWithOperationAndParameter = null;
        }
    }

    private class LocationMeasurementCircleTag {
        @Nullable
        Polyline mENodeBLine;
        @Nullable
        Marker mHandoverMarker;
        @Nullable
        LiveData<CellMeasurementsWithSelectedParameter> mCellMeasurementsWithParameter;
        @Nullable
        LiveData<ENodeBEntity> mCurrentENodeB;

        LocationMeasurementCircleTag() {
            mENodeBLine = null;
            mHandoverMarker = null;
            mCellMeasurementsWithParameter = null;
            mCurrentENodeB = null;
        }
    }

    @Nullable
    private TrackingService mService = null;
    @Nullable
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TrackingService.LocalBinder binder = (TrackingService.LocalBinder) service;
            mService = binder.getService();
            updateInitialLocationIfNeeded();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "in onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mENodeBNameTextView = findViewById(R.id.eNodeBNameTextView);
        mENodeBTextView = findViewById(R.id.eNodeBTextView);
        mCidTextView = findViewById(R.id.cidTextView);
        mEarfcnTextView = findViewById(R.id.earfcnTextView);
        mPciTextView = findViewById(R.id.pciTextView);
        mTacTextView = findViewById(R.id.tacTextView);

        mRsrpTextView = findViewById(R.id.rsrpTextView);
        mRsrqTextView = findViewById(R.id.rsrqTextView);
        mTaTextView = findViewById(R.id.taTextView);
        mRssiTextView = findViewById(R.id.rssiTextView);
        mSnrTextView = findViewById(R.id.snrTextView);

        mFilterGroup = findViewById(R.id.filterGroup);
        mFilterTextView = findViewById(R.id.filterTextView);

        mParameterLegendView = findViewById(R.id.parameterLegendView);
        mMinLegendLabelView = findViewById(R.id.minLegendLabelView);
        mMaxLegendLabelView = findViewById(R.id.maxLabelLegendView);
        mLegendTextView = findViewById(R.id.legendTextView);

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(menuItem -> {
            int selectedMenuItemId = menuItem.getItemId();
            if (mIsRequestingLocationUpdates != null && mService != null) {
                if (mIsRequestingLocationUpdates && selectedMenuItemId != mCurrentNavigationId) {
                    mService.stopLocationUpdates();
                }
            }
            selectCurrentNavigationItem(selectedMenuItemId);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        mViewModel = new ViewModelProvider(this).get(CoverageMapViewModel.class);

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(view -> {
            if (mIsReplayActive != null && mIsReplayActive) {
                mViewModel.stopReplayTrace();
            } else if (mIsRequestingLocationUpdates != null && mService != null) {
                if (mIsRequestingLocationUpdates) {
                    mService.stopLocationUpdates();
                } else {
                    if (mCurrentNavigationId == R.id.nav_trace_map) {
                        mViewModel.startNewTrace();
                    }
                    mService.startLocationUpdates();
                }
            }
        });

        if (savedInstanceState == null) {
            Log.d(TAG, "Initial location needs update");
            mInitialLocationNeedsUpdate = true;
        } else {
            CameraPosition cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
            Log.d(TAG, "Setting initial camera position " + cameraPosition);
            mViewModel.setCameraPosition(cameraPosition);
        }

        if (checkPermission()) {
            onCreateWithPermissionGranted();
        } else {
            requestPermission();
        }
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "in onStart()");
        super.onStart();
        if (checkPermission()) {
            onStartWithPermissionGranted();
        }
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "in onStop()");
        if (mService != null) {
            unbindService(mServiceConnection);
            mService = null;
        }
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.i(TAG, "in onSaveInstanceState()");
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
        }
        super.onSaveInstanceState(outState);
    }

    private void selectCurrentNavigationItem(int navigationItem) {
        switch (navigationItem) {
            case R.id.nav_grid_map:
                mViewModel.deactivateTrace();
                break;
            case R.id.nav_trace_map:
                mViewModel.activateLatestTrace();
                break;
        }
    }

    @SuppressWarnings("SameReturnValue")
    private void onCreateWithPermissionGranted() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                Log.i(TAG, "in onMapReady()");
                mMap = googleMap;
                if (mMap != null) {
                    final LifecycleOwner owner = this;
                    mMap.setInfoWindowAdapter(new MultiLineInfoWindowAdapter(this));

                    mMap.setOnMarkerClickListener(marker -> {
                        Object tag = marker.getTag();
                        if (tag instanceof IdentifiedCellMarkerTag) {
                            IdentifiedCellMarkerTag cellMarkerTag = (IdentifiedCellMarkerTag) tag;
                            cellMarkerTag.mCellCircle.setVisible(true);
                            cellMarkerTag.mExtCellCircle.setVisible(true);
                            mViewModel.selectCi(cellMarkerTag.mCi);
                        } else if (tag instanceof ENodeBMarkerTag) {
                            ENodeBMarkerTag eNodeBMarkerTag = (ENodeBMarkerTag) tag;
                            mViewModel.selectENodeB(eNodeBMarkerTag.mENodeBId);
                        }
                        return false;
                    });

                    mMap.setOnInfoWindowCloseListener(marker -> {
                        Object tag = marker.getTag();
                        if (tag instanceof IdentifiedCellMarkerTag) {
                            IdentifiedCellMarkerTag cellMarkerTag = (IdentifiedCellMarkerTag) tag;
                            cellMarkerTag.mCellCircle.setVisible(false);
                            cellMarkerTag.mExtCellCircle.setVisible(false);
                            mViewModel.selectAllCi();
                        } else if (tag instanceof ENodeBMarkerTag) {
                            mViewModel.selectAllENodeBs();
                        }
                    });

                    mMap.setOnCameraIdleListener(() -> {
                        final LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                        mViewModel.setVisibleBounds(latLngBounds);
                        final float zoomLevel = mMap.getCameraPosition().zoom;
                        mViewModel.setZoomLevel(zoomLevel);
                    });

                    mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                        @Override
                        public void onMarkerDragStart(Marker marker) {

                        }

                        @Override
                        public void onMarkerDrag(Marker marker) {

                        }

                        @Override
                        public void onMarkerDragEnd(@NonNull Marker marker) {
                            Object tag = marker.getTag();
                            if (tag instanceof ENodeBMarkerTag) {
                                ENodeBMarkerTag eNodeBMarkerTag = (ENodeBMarkerTag) tag;
                                Bundle bundle = new Bundle();
                                bundle.putInt("eNodeB", eNodeBMarkerTag.mENodeBId);
                                bundle.putParcelable("latLng", marker.getPosition());
                                EditENodeBDialogFragment editENodeBDialogFragment = new EditENodeBDialogFragment();
                                editENodeBDialogFragment.setArguments(bundle);
                                editENodeBDialogFragment.show(getSupportFragmentManager(), "edit_enodeb");
                            }
                        }
                    });

                    updateInitialLocationIfNeeded();
                    updateIconBitmaps();
                    updateMapUiSettings();
                    subscribeToViewModel();
                }
            });
        }
    }

    private void onStartWithPermissionGranted() {
        bindService(new Intent(this, TrackingService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void updateInitialLocationIfNeeded() {
        if (mInitialLocationNeedsUpdate && mService != null && checkPermission()) {
            mService.getInitialLocation();
            mInitialLocationNeedsUpdate = false;
        }
    }

    private void updateIconBitmaps() {
        mRadioTowerBitmap = ResourceUtil.getBitmapDescriptor(this, R.drawable.ic_radio_tower);
        mRadioTowerShadowBitmap = ResourceUtil.getBitmapDescriptor(this, R.drawable.ic_radio_tower_shadow);
        mCellBitmap = ResourceUtil.getBitmapDescriptor(this, R.drawable.ic_cell);
        mCellShadowBitmap = ResourceUtil.getBitmapDescriptor(this, R.drawable.ic_cell_shadow);
        mArrowUpBitmap = ResourceUtil.getBitmapDescriptor(this, R.drawable.ic_arrow_up);
        mArrowUpSlopedBitmap = ResourceUtil.getBitmapDescriptor(this, R.drawable.ic_arrow_up_sloped);
        mArrowDownBitmap = ResourceUtil.getBitmapDescriptor(this, R.drawable.ic_arrow_down);
        mArrowDownSlopedBitmap = ResourceUtil.getBitmapDescriptor(this, R.drawable.ic_arrow_down_sloped);
        mArrowRightBitmap = ResourceUtil.getBitmapDescriptor(this, R.drawable.ic_arrow_right);
        HandoverBitmap.updateBitmaps(this);
    }

    @SuppressLint("MissingPermission")
    private void updateMapUiSettings() {
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            UiSettings uiSettings = mMap.getUiSettings();
            uiSettings.setMyLocationButtonEnabled(true);
            uiSettings.setCompassEnabled(true);
            uiSettings.setMapToolbarEnabled(false);
        }
    }

    private void subscribeToViewModel() {
        mViewModel.isTraceActive().observe(this, isTraceActive -> {
            if (isTraceActive != null) {
                if (isTraceActive) {
                    mCurrentNavigationId = R.id.nav_trace_map;
                } else {
                    mCurrentNavigationId = R.id.nav_grid_map;
                }
                mNavigationView.setCheckedItem(mCurrentNavigationId);
                enableDisableMenuChoices();
            }
        });

        mViewModel.getCameraUpdate().observe(this, cameraUpdate -> {
            if (mMap != null && cameraUpdate != null) {
                mMap.moveCamera(cameraUpdate);
            }
        });

        mViewModel.getRequestingLocationUpdates().observe(this, isRequestingLocationUpdates -> {
            mIsRequestingLocationUpdates = isRequestingLocationUpdates;
            if (mIsRequestingLocationUpdates != null) {
                if (mIsRequestingLocationUpdates) {
                    mFab.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    mFab.setImageResource(android.R.drawable.ic_media_play);
                }
                enableDisableMenuChoices();
            }
        });

        mViewModel.isReplayActive().observe(this, isReplayActive -> {
            mIsReplayActive = isReplayActive;
            if (isReplayActive != null) {
                if (isReplayActive) {
                    mFab.setImageResource(android.R.drawable.ic_media_pause);
                } else if (mIsRequestingLocationUpdates != null) {
                    if (mIsRequestingLocationUpdates) {
                        mFab.setImageResource(android.R.drawable.ic_media_pause);
                    } else {
                        mFab.setImageResource(android.R.drawable.ic_media_play);
                    }
                } else {
                    mFab.setImageResource(android.R.drawable.ic_media_play);
                }
            }
        });

        mViewModel.getSelectedENodeBs().observe(this, eNodeBEntities -> {
            final Set<Integer> invisibleMarkers = new HashSet<>(mENodeBMarkers.keySet());
            if (mMap != null && eNodeBEntities != null) {
                eNodeBEntities.forEach(eNodeBEntity -> {
                    final int eNodeBId = eNodeBEntity.getId();
                    final ENodeBMarkerTag tag;
                    Marker marker = mENodeBMarkers.get(eNodeBId);
                    if (marker == null) {
                        final BitmapDescriptor eNodeBIcon;
                        if (eNodeBEntity.isLocated()) {
                            eNodeBIcon = mRadioTowerBitmap;
                        } else {
                            eNodeBIcon = mRadioTowerShadowBitmap;
                        }
                        marker = mMap.addMarker(new MarkerOptions().
                                position(eNodeBEntity.getLatLng()).
                                draggable(true).
                                title(eNodeBEntity.getName()).
                                snippet("eNodeB " + eNodeBEntity.getId()).
                                icon(eNodeBIcon));
                        tag = new ENodeBMarkerTag(eNodeBId);
                        marker.setTag(tag);
                        mENodeBMarkers.put(eNodeBId, marker);
                    } else {
                        tag = (ENodeBMarkerTag) marker.getTag();
                        if (eNodeBEntity.isLocated()) {
                            marker.setIcon(mRadioTowerBitmap);
                        } else {
                            marker.setIcon(mRadioTowerShadowBitmap);
                        }
                        marker.setTitle(eNodeBEntity.getName());
                        if (!marker.getPosition().equals(eNodeBEntity.getLatLng())) {
                            marker.setPosition(eNodeBEntity.getLatLng());
                            if (tag != null && tag.mIdentifiedCellsForENodeB != null) {
                                tag.mIdentifiedCellsForENodeB.removeObservers(this);
                                tag.mIdentifiedCellsForENodeB = null;
                            }
                        }
                        marker.setVisible(true);
                        invisibleMarkers.remove(eNodeBId);
                    }
                    final Marker eNodeBMarker = marker;
                    if (tag != null && tag.mIdentifiedCellsForENodeB == null) {
                        tag.mIdentifiedCellsForENodeB = mViewModel.getIdentifiedCellsForENodeB(eNodeBId);
                        tag.mIdentifiedCellsForENodeB.observe(this, identifiedCellEntities -> {
                            if (identifiedCellEntities != null) {
                                SortedMap<Earfcn, List<IdentifiedCellEntity>> allCells = new TreeMap<>();
                                identifiedCellEntities.forEach(identifiedCellEntity -> {
                                    Earfcn earfcn = Earfcn.get(identifiedCellEntity.getEarfcn());
                                    List<IdentifiedCellEntity> earfcnCells = allCells.get(earfcn);
                                    if (earfcnCells == null) {
                                        earfcnCells = new ArrayList<>();
                                        allCells.put(earfcn, earfcnCells);
                                    }
                                    earfcnCells.add(identifiedCellEntity);
                                });
                                StringBuilder snippet = new StringBuilder(String.format(Locale.ENGLISH,
                                        "eNodeB %d, %d cells in total",
                                        eNodeBEntity.getId(), identifiedCellEntities.size()));
                                allCells.keySet().forEach(earfcn -> {
                                    List<IdentifiedCellEntity> earfcnCells = allCells.get(earfcn);
                                    if (earfcnCells != null) {
                                        snippet.append(String.format(Locale.ENGLISH, "\nEARFCN %d (%.1f MHz), %d cells",
                                                earfcn.getValue(), earfcn.getFrequency(), earfcnCells.size()));
                                    }
                                });
                                eNodeBMarker.setSnippet(snippet.toString());
                                if (!eNodeBEntity.isLocated()) {
                                    final LatLngBounds.Builder eNodeBBoundsBuilder = LatLngBounds.builder();
                                    identifiedCellEntities.forEach(identifiedCellEntity -> {
                                        final LatLngBounds cellBounds = identifiedCellEntity.getBounds();
                                        final LatLngBounds extCellBounds = identifiedCellEntity.getExtBounds();
                                        eNodeBBoundsBuilder.include(cellBounds.northeast).include(cellBounds.southwest).
                                                include(extCellBounds.northeast).include(extCellBounds.southwest);
                                    });
                                    final LatLngBounds eNodeBBounds = eNodeBBoundsBuilder.build();
                                    final LatLng eNodeBLocation = eNodeBBounds.getCenter();
                                    eNodeBMarker.setPosition(eNodeBLocation);
                                    if (!eNodeBEntity.getLatLng().equals(eNodeBLocation)) {
                                        eNodeBEntity.setLatLng(eNodeBLocation);
                                        mViewModel.updateENodeB(eNodeBEntity);
                                    }
                                    identifiedCellEntities.forEach(identifiedCellEntity -> {
                                        final Integer ci = identifiedCellEntity.getCi();
                                        final LatLngBounds cellBounds = identifiedCellEntity.getBounds();
                                        final LatLngBounds extCellBounds = identifiedCellEntity.getExtBounds();
                                        final LatLng cellCenter = cellBounds.getCenter();
                                        final LatLng extCellCenter = extCellBounds.getCenter();
                                        Marker cellMarker = mIdentifiedCellMarkers.get(ci);
                                        if (cellMarker == null) {
                                            cellMarker = createCellMarker(identifiedCellEntity);
                                        }
                                        if (cellMarker != null) {
                                            updateMarkerRotation(cellMarker, cellCenter, eNodeBLocation);
                                            IdentifiedCellMarkerTag cellMarkerTag = (IdentifiedCellMarkerTag) cellMarker.getTag();
                                            if (cellMarkerTag != null) {
                                                Marker extCellMarker = cellMarkerTag.mExtCellMarker;
                                                updateMarkerRotation(extCellMarker, extCellCenter, eNodeBLocation);
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }
            invisibleMarkers.forEach(eNodeBId -> {
                Marker eNodeBMarker = mENodeBMarkers.get(eNodeBId);
                if (eNodeBMarker != null) {
                    ENodeBMarkerTag eNodeBMarkerTag = (ENodeBMarkerTag) eNodeBMarker.getTag();
                    if (eNodeBMarkerTag != null && eNodeBMarkerTag.mIdentifiedCellsForENodeB != null) {
                        eNodeBMarkerTag.mIdentifiedCellsForENodeB.removeObservers(this);
                        eNodeBMarkerTag.mIdentifiedCellsForENodeB = null;
                    }
                    eNodeBMarker.setVisible(false);
                }
            });
        });

        mViewModel.getSelectedIdentifiedCellsWithENodeB().observe(this, identifiedCellEntitiesWithENodeB -> {
            final Set<Integer> invisibleMarkers = new HashSet<>(mIdentifiedCellMarkers.keySet());
            if (mMap != null && identifiedCellEntitiesWithENodeB != null) {
                identifiedCellEntitiesWithENodeB.forEach(identifiedCellEntityWithENodeB -> {
                    final Integer ci = identifiedCellEntityWithENodeB.identifiedCell.getCi();
                    final LatLngBounds cellBounds = identifiedCellEntityWithENodeB.identifiedCell.getBounds();
                    final LatLngBounds extCellBounds = identifiedCellEntityWithENodeB.identifiedCell.getExtBounds();
                    final LatLng cellCenter = cellBounds.getCenter();
                    final LatLng extCellCenter = extCellBounds.getCenter();
                    Marker cellMarker = mIdentifiedCellMarkers.get(ci);
                    Marker extCellMarker = null;
                    IdentifiedCellMarkerTag cellMarkerTag;
                    if (cellMarker == null) {
                        cellMarker = createCellMarker(identifiedCellEntityWithENodeB.identifiedCell);
                        if (cellMarker != null) {
                            cellMarkerTag = (IdentifiedCellMarkerTag) cellMarker.getTag();
                            if (cellMarkerTag != null) {
                                extCellMarker = cellMarkerTag.mExtCellMarker;
                            }
                        }
                    } else {
                        final double cellRadius = SphericalUtil.computeDistanceBetween(cellBounds.northeast, cellBounds.southwest) / 2.0;
                        final double extCellRadius = SphericalUtil.computeDistanceBetween(extCellBounds.northeast, extCellBounds.southwest) / 2.0;
                        cellMarker.setPosition(cellCenter);
                        cellMarker.setVisible(true);
                        cellMarkerTag = (IdentifiedCellMarkerTag) cellMarker.getTag();
                        if (cellMarkerTag != null) {
                            extCellMarker = cellMarkerTag.mExtCellMarker;
                            extCellMarker.setPosition(extCellCenter);
                            extCellMarker.setVisible(true);
                            Circle cellCircle = cellMarkerTag.mCellCircle;
                            cellCircle.setCenter(cellCenter);
                            cellCircle.setRadius(cellRadius);
                            Circle extCellCircle = cellMarkerTag.mExtCellCircle;
                            extCellCircle.setCenter(extCellCenter);
                            extCellCircle.setRadius(extCellRadius);
                        }
                        invisibleMarkers.remove(ci);
                    }
                    if (identifiedCellEntityWithENodeB.eNodeB.isLocated()) {
                        final LatLng eNodeBLocation = identifiedCellEntityWithENodeB.eNodeB.getLatLng();
                        updateMarkerRotation(cellMarker, cellCenter, eNodeBLocation);
                        updateMarkerRotation(extCellMarker, extCellCenter, eNodeBLocation);
                    }
                });
            }
            invisibleMarkers.forEach(ci -> {
                Marker cellMarker = mIdentifiedCellMarkers.get(ci);
                if (cellMarker != null) {
                    cellMarker.setVisible(false);
                    IdentifiedCellMarkerTag cellMarkerTag = (IdentifiedCellMarkerTag) cellMarker.getTag();
                    if (cellMarkerTag != null) {
                        Marker extCellMarker = cellMarkerTag.mExtCellMarker;
                        extCellMarker.setVisible(false);
                        Circle cellCircle = cellMarkerTag.mCellCircle;
                        cellCircle.setVisible(false);
                        Circle extCellCircle = cellMarkerTag.mExtCellCircle;
                        extCellCircle.setVisible(false);
                    }
                }
            });
        });

        mViewModel.getCurrentENodeB().observe(this, eNodeBEntity -> {
            if (eNodeBEntity != null) {
                if (eNodeBEntity.isLocated()) {
                    if (eNodeBEntity.getName() != null && !eNodeBEntity.getName().isEmpty()) {
                        mENodeBNameTextView.setText(eNodeBEntity.getName());
                    } else {
                        mENodeBNameTextView.setText(R.string.no_name);
                    }
                } else {
                    mENodeBNameTextView.setText(R.string.no_location);
                }
            } else {
                mENodeBNameTextView.setText("-");
            }
        });

        mViewModel.getCurrentServingCellMeasurement().observe(this, cellMeasurementEntity -> {
            if (cellMeasurementEntity != null) {
                mENodeBTextView.setText(String.valueOf(cellMeasurementEntity.getENodeB()));
                mCidTextView.setText(String.valueOf(cellMeasurementEntity.getCid()));
                mEarfcnTextView.setText(String.valueOf(cellMeasurementEntity.getEarfcn()));
                mPciTextView.setText(String.valueOf(cellMeasurementEntity.getPci()));
                mTacTextView.setText(String.valueOf(cellMeasurementEntity.getTac()));
                mRsrpTextView.setText(String.format(Locale.ENGLISH, "%d dBm", cellMeasurementEntity.getRsrp()));
                mRsrqTextView.setText(String.format(Locale.ENGLISH, "%d dB", cellMeasurementEntity.getRsrq()));
                final int ta = cellMeasurementEntity.getTa();
                if (ta != Integer.MAX_VALUE) {
                    mTaTextView.setText(String.valueOf(ta));
                } else {
                    mTaTextView.setText("N/A");
                }
                mRssiTextView.setText(String.format(Locale.ENGLISH, "%d dBm", cellMeasurementEntity.getRssi()));
                final int snr = cellMeasurementEntity.getSnr();
                if (snr != Integer.MAX_VALUE) {
                    mSnrTextView.setText(String.format(Locale.ENGLISH, "%.1f dB", snr / 10.0f));
                } else {
                    mSnrTextView.setText("N/A");
                }
            } else {
                mENodeBTextView.setText("-");
                mCidTextView.setText("-");
                mEarfcnTextView.setText("-");
                mPciTextView.setText("-");
                mTacTextView.setText("-");
                mRsrpTextView.setText("-");
                mRsrqTextView.setText("-");
                mTaTextView.setText("-");
                mRssiTextView.setText("-");
                mSnrTextView.setText("-");
            }
        });

        mViewModel.getCurrentFilter().observe(this, combinedFilter -> {
            StringBuilder builder = new StringBuilder();
            if (combinedFilter != null) {
                if (combinedFilter.selectedCi != null) {
                    final int ci = combinedFilter.selectedCi;
                    builder.append("eNodeB ").append(IdUtil.getENodeB(ci)).append(" CID ").append(IdUtil.getCid(ci)).append(" ");
                } else {
                    if (combinedFilter.selectedENodeBId != null) {
                        builder.append("eNodeB ").append(combinedFilter.selectedENodeBId).append(" ");
                    }
                    if (combinedFilter.selectedEarfcn != null) {
                        builder.append("EARFCN ").append(combinedFilter.selectedEarfcn).append(" ");
                    }
                }
                switch (combinedFilter.handoverType) {
                    case INTER:
                        builder.append("Inter-Freq ");
                        break;
                    case INTRA:
                        builder.append("Intra-Freq ");
                        break;
                }
            }
            if (builder.length() > 0) {
                mFilterTextView.setText(builder.toString());
                mFilterGroup.setVisibility(View.VISIBLE);
            } else {
                mFilterTextView.setText("");
                mFilterGroup.setVisibility(View.GONE);
            }
        });

        mViewModel.getCurrentLegend().observe(this, legend -> mLegendTextView.setText(legend));

        mViewModel.getSelectedMeasurementParameter().observe(this, parameter -> {
            mParameterLegendView.setParameter(parameter);
            String minLegendLabel = parameter.getMinValue() + parameter.getUnit();
            mMinLegendLabelView.setText(minLegendLabel);
            String maxLegendLabel = parameter.getMaxValue() + parameter.getUnit();
            mMaxLegendLabelView.setText(maxLegendLabel);
        });

        mViewModel.getCurrentIdentifiedCell().observe(this, identifiedCellEntity -> {
            if (mMap != null && identifiedCellEntity != null) {
                final LatLngBounds bounds = identifiedCellEntity.getBounds();
                final LatLng cellCenter = bounds.getCenter();
                final double cellRadius = SphericalUtil.computeDistanceBetween(bounds.northeast, bounds.southwest) / 2.0;
                if (mCurrentIdentifiedCellCircle == null) {
                    mCurrentIdentifiedCellCircle = mMap.addCircle(new CircleOptions().
                            center(cellCenter).
                            radius(cellRadius).
                            strokeWidth(5.0f).
                            zIndex(0.0f).
                            fillColor(Color.LTGRAY & 0x44FFFFFF));
                } else {
                    mCurrentIdentifiedCellCircle.setCenter(cellCenter);
                    mCurrentIdentifiedCellCircle.setRadius(cellRadius);
                    mCurrentIdentifiedCellCircle.setVisible(true);
                }
                mViewModel.selectCi(identifiedCellEntity.getCi());
            } else if (mCurrentIdentifiedCellCircle != null) {
                mCurrentIdentifiedCellCircle.setVisible(false);
                mViewModel.selectAllCi();
            }
        });

        mViewModel.getSelectedClusters().observe(this, clusterEntities -> {
            final Set<Long> invisibleClusters = new HashSet<>(mClusterCircles.keySet());
            if (mMap != null && clusterEntities != null) {
                clusterEntities.forEach(clusterEntity -> {
                    final long coord = clusterEntity.getCoord();
                    final LatLng latLng = clusterEntity.getLatLng();
                    Circle circle = mClusterCircles.get(coord);
                    final ClusterCircleTag tag;
                    if (circle == null) {
                        circle = mMap.addCircle(new CircleOptions().
                                center(latLng).
                                radius(10.0).
                                strokeWidth(0).
                                zIndex(30.0f).
                                visible(false));
                        tag = new ClusterCircleTag();
                        circle.setTag(tag);
                        mClusterCircles.put(coord, circle);
                    } else {
                        tag = (ClusterCircleTag) circle.getTag();
                        invisibleClusters.remove(coord);
                    }
                    final Circle clusterCircle = circle;
                    if (tag != null && tag.mCellMeasurementsWithOperationAndParameter == null) {
                        tag.mCellMeasurementsWithOperationAndParameter = mViewModel.getCellMeasurementsWithSelectedOperationForCluster(coord);
                        tag.mCellMeasurementsWithOperationAndParameter.observe(this, cellMeasurementsWithOperationAndParameter -> {
                            if (cellMeasurementsWithOperationAndParameter != null &&
                                    cellMeasurementsWithOperationAndParameter.operation != null &&
                                    cellMeasurementsWithOperationAndParameter.parameter != null &&
                                    cellMeasurementsWithOperationAndParameter.cellMeasurements != null) {
                                List<CellMeasurementEntity> cellMeasurements = cellMeasurementsWithOperationAndParameter.cellMeasurements;
                                if (cellMeasurements.isEmpty()) {
                                    clusterCircle.setVisible(false);
                                } else {
                                    tag.isServingCluster = cellMeasurements.stream().anyMatch(CellMeasurementEntity::isRegistered);
                                    if (tag.isHandoverCluster) {
                                        clusterCircle.setStrokeWidth(4);
                                    } else if (tag.isServingCluster) {
                                        clusterCircle.setStrokeWidth(2);
                                    } else {
                                        clusterCircle.setStrokeWidth(0);
                                    }
                                    MeasurementOperation operation = cellMeasurementsWithOperationAndParameter.operation;
                                    MeasurementParameter parameter = cellMeasurementsWithOperationAndParameter.parameter;
                                    final int fillColor = ColorScale.getColor(
                                            operation.getResult(cellMeasurements, parameter), parameter);
                                    clusterCircle.setFillColor(fillColor);
                                    clusterCircle.setVisible(true);
                                }
                            }
                        });
                    }
                    if (tag != null && tag.mHandoverEntitiesWithSelectedCi == null) {
                        tag.mHandoverEntitiesWithSelectedCi = mViewModel.getHandoversWithSelectedCiForCluster(coord);
                        tag.mHandoverEntitiesWithSelectedCi.observe(this, handoverEntitiesWithSelectedCi -> {
                            if (handoverEntitiesWithSelectedCi != null &&
                                    handoverEntitiesWithSelectedCi.handoverEntities != null &&
                                    handoverEntitiesWithSelectedCi.zoomLevel != null) {
                                tag.isHandoverCluster = !handoverEntitiesWithSelectedCi.handoverEntities.isEmpty();
                                if (tag.isHandoverCluster) {
                                    clusterCircle.setStrokeWidth(4);
                                    if (handoverEntitiesWithSelectedCi.zoomLevel < 16.8) {
                                        if (tag.mHandoverMarker != null) {
                                            tag.mHandoverMarker.setVisible(false);
                                        }
                                    } else {
                                        final BitmapDescriptor handoverIcon;
                                        final String title = "Handovers";
                                        final HandoverBitmap handoverBitmap;
                                        if (handoverEntitiesWithSelectedCi.selectedCi == null) {
                                            handoverBitmap = new HandoverBitmap(false);
                                        } else {
                                            handoverBitmap = new HandoverBitmap(true);
                                        }
                                        for (HandoverEntity handoverEntity : handoverEntitiesWithSelectedCi.handoverEntities) {
                                            Earfcn sourceEarfcn = Earfcn.get(handoverEntity.getSourceEarfcn());
                                            Earfcn targetEarfcn = Earfcn.get(handoverEntity.getTargetEarfcn());
                                            int interHandover = sourceEarfcn.compareTo(targetEarfcn);
                                            if (handoverEntitiesWithSelectedCi.selectedCi == null) {
                                                if (interHandover == 0) {
                                                    handoverBitmap.addIntra();
                                                } else if (interHandover < 0) {
                                                    handoverBitmap.addInterUp();
                                                } else {
                                                    handoverBitmap.addInterDown();
                                                }
                                            } else {
                                                if (handoverEntity.getSourceCi() == handoverEntitiesWithSelectedCi.selectedCi) {
                                                    if (interHandover == 0) {
                                                        handoverBitmap.addOutgoingIntra();
                                                    } else if (interHandover < 0) {
                                                        handoverBitmap.addOutgoingInterUp();
                                                    } else {
                                                        handoverBitmap.addOutgoingInterDown();
                                                    }
                                                }
                                                if (handoverEntity.getTargetCi() == handoverEntitiesWithSelectedCi.selectedCi) {
                                                    if (interHandover == 0) {
                                                        handoverBitmap.addIncomingIntra();
                                                    } else if (interHandover < 0) {
                                                        handoverBitmap.addIncomingInterUp();
                                                    } else {
                                                        handoverBitmap.addIncomingInterDown();
                                                    }
                                                }
                                            }
                                        }
                                        handoverIcon = handoverBitmap.get();
                                        StringBuilder builder = new StringBuilder();
                                        for (HandoverEntity handoverEntity : handoverEntitiesWithSelectedCi.handoverEntities) {
                                            if (builder.length() > 0) {
                                                builder.append("\n");
                                            }
                                            builder.append(String.format(Locale.ENGLISH, "%d (%d) ⇒ %d (%d)",
                                                    handoverEntity.getSourceCid(), handoverEntity.getSourceENodeB(),
                                                    handoverEntity.getTargetCid(), handoverEntity.getTargetENodeB()));
                                        }
                                        final String snippet = builder.toString();
                                        if (tag.mHandoverMarker != null) {
                                            tag.mHandoverMarker.setTitle(title);
                                            tag.mHandoverMarker.setSnippet(snippet);
                                            tag.mHandoverMarker.setIcon(handoverIcon);
                                            tag.mHandoverMarker.setVisible(true);
                                        } else {
                                            tag.mHandoverMarker = mMap.addMarker(new MarkerOptions().
                                                    position(latLng).
                                                    title(title).
                                                    snippet(snippet).
                                                    anchor(0.5f, 0.5f).
                                                    icon(handoverIcon).
                                                    flat(true));
                                        }
                                    }
                                } else {
                                    if (tag.isServingCluster) {
                                        clusterCircle.setStrokeWidth(2);
                                    } else {
                                        clusterCircle.setStrokeWidth(0);
                                    }
                                    if (tag.mHandoverMarker != null) {
                                        tag.mHandoverMarker.setVisible(false);
                                    }
                                }
                            }
                        });
                    }
                });
            }
            invisibleClusters.forEach(coord -> {
                Circle clusterCircle = mClusterCircles.get(coord);
                if (clusterCircle != null) {
                    ClusterCircleTag tag = (ClusterCircleTag) clusterCircle.getTag();
                    if (tag != null) {
                        if (tag.mCellMeasurementsWithOperationAndParameter != null) {
                            tag.mCellMeasurementsWithOperationAndParameter.removeObservers(this);
                            tag.mCellMeasurementsWithOperationAndParameter = null;
                        }
                        if (tag.mHandoverEntitiesWithSelectedCi != null) {
                            tag.mHandoverEntitiesWithSelectedCi.removeObservers(this);
                            tag.mHandoverEntitiesWithSelectedCi = null;
                        }
                        if (tag.mHandoverMarker != null) {
                            tag.mHandoverMarker.setVisible(false);
                        }
                    }
                    clusterCircle.setVisible(false);
                }
            });
        });

        mViewModel.getSelectedLocationMeasurements().observe(this, locationMeasurementEntities -> {
            final Set<Long> invisibleLocationMeasurements = new HashSet<>(mLocationMeasurementCircles.keySet());
            if (mMap != null && locationMeasurementEntities != null) {
                locationMeasurementEntities.forEach(locationMeasurementEntity -> {
                    final long currLocationId = locationMeasurementEntity.getId();
                    final Long prevLocationId = locationMeasurementEntity.getPrevId();
                    final LatLng latLng = locationMeasurementEntity.getLatLng();
                    final LocationMeasurementCircleTag tag;
                    Circle circle = mLocationMeasurementCircles.get(currLocationId);
                    if (circle == null) {
                        circle = mMap.addCircle(new CircleOptions().
                                center(latLng).
                                radius(10.0).
                                strokeWidth(2.0f).
                                zIndex(30.0f).
                                visible(false));
                        tag = new LocationMeasurementCircleTag();
                        circle.setTag(tag);
                        mLocationMeasurementCircles.put(currLocationId, circle);
                    } else {
                        tag = (LocationMeasurementCircleTag) circle.getTag();
                        invisibleLocationMeasurements.remove(currLocationId);
                    }
                    final Circle locationMeasurementCircle = circle;
                    if (tag != null && tag.mCellMeasurementsWithParameter == null) {
                        tag.mCellMeasurementsWithParameter = mViewModel.getCellMeasurementsForLocationMeasurements(currLocationId, prevLocationId);
                        tag.mCellMeasurementsWithParameter.observe(this, cellMeasurementsWithParameter -> {
                            if (cellMeasurementsWithParameter == null ||
                                    cellMeasurementsWithParameter.cellMeasurements == null ||
                                    cellMeasurementsWithParameter.parameter == null) {
                                return;
                            }
                            CellMeasurementEntity currentServingCellMeasurement = null;
                            boolean currentMeasurementsFound = false;
                            CellMeasurementEntity previousServingCellMeasurement = null;
                            boolean previousMeasurementsFound = false;
                            for (CellMeasurementEntity cellMeasurementEntity : cellMeasurementsWithParameter.cellMeasurements) {
                                if (currLocationId == cellMeasurementEntity.getLocationId()) {
                                    currentMeasurementsFound = true;
                                    if (cellMeasurementEntity.isRegistered()) {
                                        currentServingCellMeasurement = cellMeasurementEntity;
                                    }
                                }
                                if (prevLocationId != null && prevLocationId == cellMeasurementEntity.getLocationId()) {
                                    previousMeasurementsFound = true;
                                    if (cellMeasurementEntity.isRegistered()) {
                                        previousServingCellMeasurement = cellMeasurementEntity;
                                    }
                                }
                            }
                            if (!currentMeasurementsFound) {
                                Log.e(TAG, "No cell measurements at all found for current " + currLocationId);
                            }
                            if (prevLocationId != null && !previousMeasurementsFound) {
                                Log.e(TAG, "No cell measurements at all found for previous " + prevLocationId);
                            }
                            if (currentServingCellMeasurement == null) {
                                Log.e(TAG, "No serving cell measurement found for current " + currLocationId);
                                return;
                            }
                            MeasurementParameter parameter = cellMeasurementsWithParameter.parameter;
                            final int currentParameterValue = parameter.getValue(currentServingCellMeasurement);
                            final int currentParameterColor = ColorScale.getColor(currentParameterValue, parameter);
                            if (tag.mENodeBLine != null) {
                                tag.mENodeBLine.setColor(currentParameterColor);
                                tag.mENodeBLine.setVisible(true);
                            }
                            if (tag.mHandoverMarker != null) {
                                tag.mHandoverMarker.setVisible(true);
                            }
                            final int currentTa = currentServingCellMeasurement.getTa();
                            if (currentTa != Integer.MAX_VALUE) {
                                // A valid timing advance indicates an RRC connected UE
                                locationMeasurementCircle.setStrokeWidth(4.0f);
                            }
                            locationMeasurementCircle.setFillColor(currentParameterColor);
                            locationMeasurementCircle.setVisible(true);
                            if (tag.mENodeBLine != null || tag.mHandoverMarker != null) {
                                return;
                            }
                            if (tag.mCurrentENodeB == null) {
                                int currentENodeB = currentServingCellMeasurement.getENodeB();
                                tag.mCurrentENodeB = mViewModel.getENodeB(currentENodeB);
                                tag.mCurrentENodeB.observe(this, eNodeBEntity -> {
                                    if (eNodeBEntity != null && eNodeBEntity.isLocated()) {
                                        LatLng eNodeBLatLng = eNodeBEntity.getLatLng();
                                        tag.mENodeBLine = mMap.addPolyline(new PolylineOptions().
                                                add(latLng, eNodeBLatLng).
                                                color(currentParameterColor).
                                                width(4.0f));
                                    }
                                });
                            }
                            if (previousServingCellMeasurement == null) {
                                if (prevLocationId != null) {
                                    Log.e(TAG, "No previous serving cell measurements for previous " + prevLocationId);
                                }
                                return;
                            }
                            int currentENodeB = currentServingCellMeasurement.getENodeB();
                            int currentCid = currentServingCellMeasurement.getCid();
                            int currentCi = IdUtil.getCi(currentENodeB, currentCid);
                            Earfcn currentEarfcn = Earfcn.get(currentServingCellMeasurement.getEarfcn());
                            int previousENodeB = previousServingCellMeasurement.getENodeB();
                            int previousCid = previousServingCellMeasurement.getCid();
                            int previousCi = IdUtil.getCi(previousENodeB, previousCid);
                            Earfcn previousEarfcn = Earfcn.get(previousServingCellMeasurement.getEarfcn());
                            if (currentCi != previousCi) {
                                final String title;
                                final StringBuilder snippet = new StringBuilder(
                                        String.format(Locale.ENGLISH, "%d (%d) ⇒ %d (%d)",
                                                previousCid, previousENodeB,
                                                currentCid, currentENodeB));
                                final BitmapDescriptor icon;
                                final float previousFrequency = previousEarfcn.getFrequency();
                                final float currentFrequency = currentEarfcn.getFrequency();
                                if (currentEarfcn.equals(previousEarfcn)) {
                                    title = "Intra-frequency handover";
                                    snippet.append(String.format(
                                            Locale.ENGLISH, "\n%.1f Mhz (%d)",
                                            currentFrequency, currentEarfcn.getValue()));
                                    icon = mArrowRightBitmap;
                                } else {
                                    title = "Inter-frequency handover";
                                    FrequencyBand previousBand = previousEarfcn.getBand();
                                    FrequencyBand currentBand = currentEarfcn.getBand();
                                    if (previousBand != null && currentBand != null) {
                                        int bandComparison = currentBand.compareTo(previousBand);
                                        if (bandComparison > 0) {
                                            icon = mArrowUpBitmap;
                                        } else if (bandComparison < 0) {
                                            icon = mArrowDownBitmap;
                                        } else {
                                            if (currentFrequency > previousFrequency) {
                                                icon = mArrowUpSlopedBitmap;
                                            } else {
                                                icon = mArrowDownSlopedBitmap;
                                            }
                                        }
                                        snippet.append(String.format(
                                                Locale.ENGLISH, "\n%.1f Mhz (%d) ⇒ %.1f Mhz (%d)",
                                                previousFrequency, previousEarfcn.getValue(),
                                                currentFrequency, currentEarfcn.getValue()));
                                    } else {
                                        snippet.append(String.format(
                                                Locale.ENGLISH, "\n? MHz (%d) ⇒ ? MHz (%d)",
                                                previousEarfcn.getValue(),
                                                currentEarfcn.getValue()));
                                        icon = mArrowRightBitmap;
                                    }
                                }
                                tag.mHandoverMarker = mMap.addMarker(new MarkerOptions().
                                        position(latLng).
                                        anchor(0.5f, 0.5f).
                                        flat(true).
                                        icon(icon).
                                        title(title).
                                        snippet(snippet.toString()));
                            }
                        });
                    }
                });
            }
            invisibleLocationMeasurements.forEach(id -> {
                Circle circle = mLocationMeasurementCircles.get(id);
                if (circle != null) {
                    LocationMeasurementCircleTag tag = (LocationMeasurementCircleTag) circle.getTag();
                    if (tag != null) {
                        if (tag.mCellMeasurementsWithParameter != null) {
                            tag.mCellMeasurementsWithParameter.removeObservers(this);
                            tag.mCellMeasurementsWithParameter = null;
                        }
                        if (tag.mCurrentENodeB != null) {
                            tag.mCurrentENodeB.removeObservers(this);
                            tag.mCurrentENodeB = null;
                        }
                        if (tag.mHandoverMarker != null) {
                            tag.mHandoverMarker.setVisible(false);
                        }
                        if (tag.mENodeBLine != null) {
                            tag.mENodeBLine.setVisible(false);
                        }
                    }
                    circle.setVisible(false);
                }
            });
        });
    }

    @Nullable
    private Marker createCellMarker(@NonNull IdentifiedCellEntity identifiedCellEntity) {
        if (mMap != null) {
            final int ci = identifiedCellEntity.getCi();
            final LatLngBounds cellBounds = identifiedCellEntity.getBounds();
            final LatLngBounds extCellBounds = identifiedCellEntity.getExtBounds();
            final LatLng cellCenter = cellBounds.getCenter();
            final LatLng extCellCenter = extCellBounds.getCenter();
            final double cellRadius = SphericalUtil.computeDistanceBetween(cellBounds.northeast, cellBounds.southwest) / 2.0;
            final double extCellRadius = SphericalUtil.computeDistanceBetween(extCellBounds.northeast, extCellBounds.southwest) / 2.0;
            final Earfcn earfcn = Earfcn.get(identifiedCellEntity.getEarfcn());
            final String title = String.format(Locale.ENGLISH, "eNodeB %d CID %d",
                    identifiedCellEntity.getENodeB(),
                    identifiedCellEntity.getCid());
            final String snippet = String.format(Locale.ENGLISH, "EARFCN %d (%.1f MHz) PCI %d",
                    earfcn.getValue(), earfcn.getFrequency(), identifiedCellEntity.getPci());
            Marker cellMarker = mMap.addMarker(new MarkerOptions().
                    position(cellCenter).
                    title(title).
                    snippet(snippet).
                    flat(true).
                    anchor(0.5f, 0.5f).
                    icon(mCellBitmap));
            Marker extCellMarker = mMap.addMarker(new MarkerOptions().
                    position(extCellCenter).
                    title(title).
                    snippet(snippet).
                    flat(true).
                    anchor(0.5f, 0.5f).
                    icon(mCellShadowBitmap).
                    alpha(0.5f));
            Circle cellCircle = mMap.addCircle(new CircleOptions().
                    center(cellCenter).
                    radius(cellRadius).
                    strokeWidth(4.0f).
                    zIndex(20.0f).
                    fillColor(Color.LTGRAY & 0x88FFFFFF).
                    visible(false));
            Circle extCellCircle = mMap.addCircle(new CircleOptions().
                    center(extCellCenter).
                    radius(extCellRadius).
                    strokeWidth(4.0f).
                    strokePattern(Arrays.asList(new Dash(10.f), new Gap(10.0f))).
                    zIndex(10.0f).
                    fillColor(Color.LTGRAY & 0x44FFFFFF).
                    visible(false));
            IdentifiedCellMarkerTag cellMarkerTag = new IdentifiedCellMarkerTag(ci, extCellMarker, cellCircle, extCellCircle);
            cellMarker.setTag(cellMarkerTag);
            extCellMarker.setTag(cellMarkerTag);
            mIdentifiedCellMarkers.put(ci, cellMarker);
            return cellMarker;
        } else {
            return null;
        }
    }

    private void updateMarkerRotation(@Nullable final Marker cellMarker, @NonNull final LatLng cellCenter, @NonNull final LatLng eNodeBLocation) {
        if (cellMarker != null) {
            final double heading = SphericalUtil.computeHeading(eNodeBLocation, cellCenter) - 90.0;
            final double infoWindowAnchorX = Math.sin(-heading * Math.PI / 180.0) * 0.5 + 0.5;
            final double infoWindowAnchorY = -(Math.cos(-heading * Math.PI / 180.0) * 0.5 - 0.5);
            cellMarker.setRotation((float) heading);
            cellMarker.setInfoWindowAnchor((float) infoWindowAnchorX, (float) infoWindowAnchorY);
        }
    }

    private boolean checkPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        requestPermissions(
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                onCreateWithPermissionGranted();
                onStartWithPermissionGranted();
                updateInitialLocationIfNeeded();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mSelectTraceMenuItem = menu.findItem(R.id.action_select_trace);
        mReplayTraceMenuItem = menu.findItem(R.id.action_replay_trace);
        mDeleteTraceMenuItem = menu.findItem(R.id.action_delete_trace);
        enableDisableMenuChoices();
        return true;
    }

    private void enableDisableMenuChoices() {
        if (mIsRequestingLocationUpdates != null) {
            if (mSelectTraceMenuItem != null) {
                mSelectTraceMenuItem.setEnabled(!mIsRequestingLocationUpdates);
            }
            if (mReplayTraceMenuItem != null) {
                mReplayTraceMenuItem.setEnabled(!mIsRequestingLocationUpdates);
            }
            if (mDeleteTraceMenuItem != null) {
                switch (mCurrentNavigationId) {
                    case R.id.nav_trace_map:
                        mDeleteTraceMenuItem.setEnabled(!mIsRequestingLocationUpdates);
                        break;
                    case R.id.nav_grid_map:
                        mDeleteTraceMenuItem.setEnabled(false);
                        break;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            mViewModel.refreshIdentifiedENodeBsAndCells();
            mViewModel.refreshIdentifiedCells();
            mViewModel.refreshHandovers();
            return true;
        } else if (id == R.id.action_delete_trace) {
            ConfirmDeleteTraceDialogFragment confirmDeleteTraceDialogFragment = new ConfirmDeleteTraceDialogFragment();
            confirmDeleteTraceDialogFragment.show(getSupportFragmentManager(), "confirm_delete_trace");
            return true;
        } else if (id == R.id.action_enodeb_filter) {
            ENodeBFilterDialogFragment eNodeBFilterDialogFragment = new ENodeBFilterDialogFragment();
            eNodeBFilterDialogFragment.show(getSupportFragmentManager(), "enodeb_filter");
            return true;
        } else if (id == R.id.action_earfcn_filter) {
            EarfcnFilterDialogFragment earfcnFilterDialogFragment = new EarfcnFilterDialogFragment();
            earfcnFilterDialogFragment.show(getSupportFragmentManager(), "earfcn_filter");
            return true;
        } else if (id == R.id.action_handover_type_filter) {
            HandoverTypeFilterDialogFragment handoverTypeFilterDialogFragment = new HandoverTypeFilterDialogFragment();
            handoverTypeFilterDialogFragment.show(getSupportFragmentManager(), "handover_type_filter");
            return true;
        } else if (id == R.id.action_select_parameter) {
            SelectParameterDialogFragment selectParameterDialogFragment = new SelectParameterDialogFragment();
            selectParameterDialogFragment.show(getSupportFragmentManager(), "select_parameter");
            return true;
        } else if (id == R.id.action_select_operation) {
            SelectOperationDialogFragment selectOperationDialogFragment = new SelectOperationDialogFragment();
            selectOperationDialogFragment.show(getSupportFragmentManager(), "select_operation");
            return true;
        } else if (id == R.id.action_select_trace) {
            SelectTraceDialogFragment selectTraceDialogFragment = new SelectTraceDialogFragment();
            selectTraceDialogFragment.show(getSupportFragmentManager(), "select_trace");
            return true;
        } else if (id == R.id.action_replay_trace) {
            ReplayTraceDialogFragment replayTraceDialogFragment = new ReplayTraceDialogFragment();
            replayTraceDialogFragment.show(getSupportFragmentManager(), "replay_trace");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
