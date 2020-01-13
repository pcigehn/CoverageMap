package se.pcprogramkonsult.coveragemap.core;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.net.InetAddress;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import se.pcprogramkonsult.coveragemap.R;
import se.pcprogramkonsult.coveragemap.db.entity.ENodeBEntity;
import se.pcprogramkonsult.coveragemap.lte.Earfcn;
import se.pcprogramkonsult.coveragemap.lte.IdUtil;
import se.pcprogramkonsult.coveragemap.ui.MainActivity;

public class TrackingService extends Service implements Observer<List<ENodeBEntity>> {
    private static final String TAG = TrackingService.class.getSimpleName();

    private static final float MAX_ACCURACY = 30.0f;

    private static final String PACKAGE_NAME = "se.pcprogramkonsult.coveragemap";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION =
            PACKAGE_NAME + ".started_from_notification";

    private static final int NOTIFICATION_ID_TRACKING = 0xFF0001;
    private static final int NOTIFICATION_ID_HANDOVER = 0xFF0020;
    private static final int NOTIFICATION_ID_INTRA_FREQ = 0xFF0030;
    private static final int NOTIFICATION_ID_INTER_ENODEB = 0xFF0040;
    private static final String CHANNEL_ID_TRACKING = "channel_main";
    private static final String CHANNEL_ID_HANDOVER = "channel_handover";
    private static final String CHANNEL_ID_INTRA_FREQ = "channel_intra_freq";
    private static final String CHANNEL_ID_INTER_ENODEB = "channel_inter_enodeb";
    private static final int MAX_NOTIFICATIONS = 5;
    private int mHandoverCount = 0;
    private int mIntraFreqCount = 0;
    private int mInterENodeBCount = 0;

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    private TelephonyManager mTelephonyManager;
    @Nullable
    private PhoneStateListener mPhoneStateListener;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    @Nullable
    private LocationCallback mLocationCallback;

    @Nullable
    private NotificationManager mNotificationManager;

    private CoverageRepository mRepository;
    private Handler mServiceHandler;
    private Runnable mPinger;
    @Nullable
    private Runnable mServingCellTracker;

    @SuppressWarnings("unused")
    private boolean mServiceIsInForeground = false;

    private int mServingCellSnr = Integer.MAX_VALUE;
    @Nullable
    private CellInfoLte mPreviousServingCell = null;

    private final ConcurrentHashMap<Integer, String> eNodeBNames = new ConcurrentHashMap<>();

    @Override
    public void onCreate() {
        Log.d(TAG, "in onCreate()");

        mTelephonyManager = getSystemService(TelephonyManager.class);
        mNotificationManager = getSystemService(NotificationManager.class);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mRepository = ((CoverageApp) getApplication()).getRepository();
        mRepository.getAllENodeBs().observeForever(this);

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());

        startServingCellTracker();
        startPhoneStateListener();

        createPinger();
        createLocationCallback();
        createLocationRequest();

        if (mRepository.isRequestingLocationUpdates()) {
            startLocationUpdates();
        }

        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID_TRACKING, "Tracking", NotificationManager.IMPORTANCE_LOW));
            mNotificationManager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID_HANDOVER, "Inter-freq Handover", NotificationManager.IMPORTANCE_HIGH));
            mNotificationManager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID_INTRA_FREQ, "Intra-freq Handover", NotificationManager.IMPORTANCE_DEFAULT));
            mNotificationManager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID_INTER_ENODEB, "Inter-eNodeB Handover", NotificationManager.IMPORTANCE_DEFAULT));
            mNotificationManager.deleteNotificationChannel("channel_01");
        }
    }

    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        boolean startedFromNotification = intent.getBooleanExtra(
                EXTRA_STARTED_FROM_NOTIFICATION, false);
        if (startedFromNotification) {
            stopLocationUpdates();
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "in onBind()");
        mServiceIsInForeground = false;
        stopForeground(true);
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "in onRebind()");
        mServiceIsInForeground = false;
        stopForeground(true);
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");
        if (mRepository.isRequestingLocationUpdates()) {
            Log.i(TAG, "Starting foreground service");
            startForeground(NOTIFICATION_ID_TRACKING, getTrackingNotification());
            mServiceIsInForeground = true;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "in onDestroy()");
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        mServiceHandler.removeCallbacksAndMessages(null);
        mPreviousServingCell = null;
        mRepository.getAllENodeBs().removeObserver(this);
    }

    @SuppressLint("MissingPermission")
    public void getInitialLocation() {
        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d(TAG, "Setting initial location " + latLng);
                mRepository.setInitialLocation(latLng);
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        Log.i(TAG, "Request location updates");
        mRepository.setRequestingLocationUpdates(true);
        startService(new Intent(getApplicationContext(), TrackingService.class));
        if (mLocationCallback != null) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, mServiceHandler.getLooper());
        }
        mServiceHandler.post(mPinger);
    }

    public void stopLocationUpdates() {
        Log.i(TAG, "Remove location updates");
        if (mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        mRepository.setRequestingLocationUpdates(false);
        mServiceIsInForeground = false;
        stopSelf();
        mServiceHandler.removeCallbacks(mPinger);
    }

    @Nullable
    @SuppressLint("MissingPermission")
    private CellInfoLte getCurrentServingCell() {
        if (mTelephonyManager != null) {
            final List<CellInfo> currentCellInfos = mTelephonyManager.getAllCellInfo();
            for (CellInfo cellInfo : currentCellInfos) {
                if (cellInfo instanceof CellInfoLte) {
                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                    if (cellInfoLte.isRegistered()) {
                        return cellInfoLte;
                    }
                }
            }
        }
        return null;
    }

    private boolean isHandover(@Nullable CellInfoLte currentServingCell, @Nullable CellInfoLte previousServingCell) {
        if (currentServingCell != null && previousServingCell != null) {
            int currentCi = currentServingCell.getCellIdentity().getCi();
            int previousCi = previousServingCell.getCellIdentity().getCi();
            return currentCi != previousCi;
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    private void startServingCellTracker() {
        mPreviousServingCell = null;
        mServingCellTracker = () -> {
            try {
                final CellInfoLte currentServingCell = getCurrentServingCell();
                if (currentServingCell != null) {
                    final int currentCi = currentServingCell.getCellIdentity().getCi();
                    if (currentCi > 0 && currentCi != Integer.MAX_VALUE) {
                        mRepository.updateServingCellMeasurement(currentServingCell, mServingCellSnr);
                        final CellInfoLte previousServingCell = mPreviousServingCell;
                        if (isHandover(currentServingCell, previousServingCell)) {
                            if (mNotificationManager != null) {
                                Notification handoverNotification = getHandoverNotification(currentServingCell, previousServingCell);
                                if (handoverNotification != null) {
                                    mHandoverCount = (mHandoverCount + 1) % MAX_NOTIFICATIONS;
                                    mNotificationManager.notify(NOTIFICATION_ID_HANDOVER + mHandoverCount, handoverNotification);
                                } else {
                                    Notification interENodeBNotification = getInterENodeBNotification(currentServingCell, previousServingCell);
                                    if (interENodeBNotification != null) {
                                        mInterENodeBCount = (mInterENodeBCount + 1) % MAX_NOTIFICATIONS;
                                        mNotificationManager.notify(NOTIFICATION_ID_INTER_ENODEB + mInterENodeBCount, interENodeBNotification);
                                    } else {
                                        Notification intraFreqNotification = getIntraFreqNotification(currentServingCell, previousServingCell);
                                        if (intraFreqNotification != null) {
                                            mIntraFreqCount = (mIntraFreqCount + 1) % MAX_NOTIFICATIONS;
                                            mNotificationManager.notify(NOTIFICATION_ID_INTRA_FREQ + mIntraFreqCount, intraFreqNotification);
                                        }
                                    }
                                }
                            }
                            if (mRepository.isRequestingLocationUpdates()) {
                                mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                                    if (location != null && location.getAccuracy() < MAX_ACCURACY) {
                                        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        mRepository.updateHandover(latLng, currentServingCell, previousServingCell);
                                    }
                                });
                            }
                        }
                        mPreviousServingCell = currentServingCell;
                    }
                }
            } finally {
                mServiceHandler.postDelayed(Objects.requireNonNull(mServingCellTracker), 500);
            }
        };
        mServiceHandler.post(mServingCellTracker);
    }

    private void startPhoneStateListener() {
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(@Nullable SignalStrength signalStrength) {
                if (signalStrength != null) {
                    mServingCellSnr = ReflectionUtil.getField("mLteRssnr", signalStrength);
                }
            }
        };
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
    }

    private void createPinger() {
        mPinger = () -> {
            final String host = "8.8.8.8";
            try {
                final InetAddress address = InetAddress.getByName(host);
                final boolean reachable = address.isReachable(2000);
            } catch (Exception e) {
                Log.d(TAG, "Unable to ping " + host + ": " + e);
            } finally {
                mServiceHandler.postDelayed(mPinger, 5000);
            }
        };
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest().
                setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).
                setSmallestDisplacement(15.0f).
                setInterval(2000).
                setFastestInterval(1000);
    }

    @SuppressLint("MissingPermission")
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@Nullable LocationResult locationResult) {
                if (mTelephonyManager != null && locationResult != null) {
                    final Location location = locationResult.getLastLocation();
                    if (location != null && location.getAccuracy() < MAX_ACCURACY) {
                        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        final List<CellInfo> cellInfos = mTelephonyManager.getAllCellInfo();
                        final int dataActivity = mTelephonyManager.getDataActivity();
                        final int dataState = mTelephonyManager.getDataState();
                        mRepository.updateLocation(latLng, cellInfos, dataActivity, dataState, mServingCellSnr);
                    }
                }
            }
        };
    }

    private Notification getTrackingNotification() {
        Intent trackingServiceIntent = new Intent(this, TrackingService.class);
        trackingServiceIntent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);
        PendingIntent servicePendingIntent = PendingIntent.getService(
                this, 0, trackingServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.setAction(Intent.ACTION_MAIN);
        mainActivityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(
                this, 0, mainActivityIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_TRACKING).
                setSmallIcon(R.drawable.ic_radio_tower).
                setContentTitle("Tracking position and measuring cell coverage").
                setOngoing(true).
                setPriority(NotificationCompat.PRIORITY_DEFAULT).
                setContentIntent(activityPendingIntent).
                addAction(R.drawable.ic_radio_tower, "Stop tracking", servicePendingIntent);
        return builder.build();
    }

    @Nullable
    private Notification getHandoverNotification(@Nullable CellInfoLte currentServingCell, @Nullable CellInfoLte previousServingCell) {
        if (currentServingCell == null || previousServingCell == null) {
            return null;
        }
        int currentEarfcn = currentServingCell.getCellIdentity().getEarfcn();
        int previousEarfcn = previousServingCell.getCellIdentity().getEarfcn();
        if (currentEarfcn == previousEarfcn) {
            return null;
        }
        Earfcn fromEarfcn = Earfcn.get(previousEarfcn);
        Earfcn toEarfcn = Earfcn.get(currentEarfcn);
        float fromFrequency = fromEarfcn.getFrequency();
        float toFrequency = toEarfcn.getFrequency();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_HANDOVER).
                setSmallIcon(R.drawable.ic_radio_tower).
                setContentTitle("Inter-frequency handover").
                setContentText(String.format(
                        Locale.ENGLISH, "%.1f Mhz ⇒ %.1f Mhz", fromFrequency, toFrequency)).
                setPriority(NotificationCompat.PRIORITY_HIGH);
        return builder.build();
    }

    @Nullable
    private Notification getInterENodeBNotification(@Nullable CellInfoLte currentServingCell, @Nullable CellInfoLte previousServingCell) {
        if (currentServingCell == null || previousServingCell == null) {
            return null;
        }
        int currentCi = currentServingCell.getCellIdentity().getCi();
        int previousCi = previousServingCell.getCellIdentity().getCi();
        int currentENodeB = IdUtil.getENodeB(currentCi);
        int previousENodeB = IdUtil.getENodeB(previousCi);
        if (currentENodeB == previousENodeB) {
            return null;
        }
        String fromENodeBName = eNodeBNames.get(previousENodeB);
        String toENodeBName = eNodeBNames.get(currentENodeB);
        if (fromENodeBName == null) {
            fromENodeBName = Integer.toString(previousENodeB);
        }
        if (toENodeBName == null) {
            toENodeBName = Integer.toString(currentENodeB);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_INTER_ENODEB).
                setSmallIcon(R.drawable.ic_radio_tower).
                setContentTitle("Inter-eNodeB handover").
                setContentText(String.format(
                        Locale.ENGLISH, "%s ⇒ %s", fromENodeBName, toENodeBName)).
                setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return builder.build();
    }

    @Nullable
    private Notification getIntraFreqNotification(@Nullable CellInfoLte currentServingCell, @Nullable CellInfoLte previousServingCell) {
        if (currentServingCell == null || previousServingCell == null) {
            return null;
        }
        int currentCi = currentServingCell.getCellIdentity().getCi();
        int previousCi = previousServingCell.getCellIdentity().getCi();
        int currentCid = IdUtil.getCid(currentCi);
        int previousCid = IdUtil.getCid(previousCi);
        if (currentCid == previousCid) {
            return null;
        }
        int currentENodeB = IdUtil.getENodeB(currentCi);
        String currentENodeBName = eNodeBNames.get(currentENodeB);
        if (currentENodeBName == null) {
            currentENodeBName = Integer.toString(currentENodeB);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_INTRA_FREQ).
                setSmallIcon(R.drawable.ic_radio_tower).
                setContentTitle("Intra-frequency handover").
                setContentText(String.format(
                        Locale.ENGLISH, "%s %d ⇒ %d", currentENodeBName, previousCid, currentCid)).
                setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return builder.build();
    }

    @Override
    public void onChanged(@NonNull List<ENodeBEntity> eNodeBEntities) {
        eNodeBEntities.forEach(eNodeBEntity -> {
            String eNodeBName = eNodeBEntity.getName();
            if (eNodeBName != null && !eNodeBName.isEmpty()) {
                eNodeBNames.put(eNodeBEntity.getId(), eNodeBName);
            }
        });
    }

    public class LocalBinder extends Binder {
        @NonNull
        public TrackingService getService() {
            return TrackingService.this;
        }
    }
}
