package se.pcprogramkonsult.coveragemap.core;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class CoverageAppExecutors {

    private static final int THREAD_COUNT = 3;

    private final Executor mDiskIO;
    private final Executor mBackgroundIO;
    private final Executor mNetworkIO;
    private final Executor mMainThread;

    private CoverageAppExecutors(final Executor diskIO,
                                 final Executor backgroundIO,
                                 final Executor networkIO,
                                 final Executor mainThread)
    {
        this.mDiskIO = diskIO;
        this.mBackgroundIO = backgroundIO;
        this.mNetworkIO = networkIO;
        this.mMainThread = mainThread;
    }

    CoverageAppExecutors() {
        this(Executors.newSingleThreadExecutor(),
                Executors.newSingleThreadExecutor(),
                Executors.newFixedThreadPool(THREAD_COUNT),
                new MainThreadExecutor());
    }

    Executor diskIO() {
        return mDiskIO;
    }

    Executor backgroundIO() {
        return mBackgroundIO;
    }

    @SuppressWarnings("unused")
    public Executor networkIO() {
        return mNetworkIO;
    }

    @SuppressWarnings("unused")
    public Executor mainThread() {
        return mMainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull final Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
