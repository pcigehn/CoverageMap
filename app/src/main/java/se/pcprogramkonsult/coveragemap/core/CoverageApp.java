package se.pcprogramkonsult.coveragemap.core;

import android.app.Application;

import se.pcprogramkonsult.coveragemap.db.CoverageDatabase;

public class CoverageApp extends Application {

    private CoverageAppExecutors mExecutors;

    @Override
    public void onCreate() {
        super.onCreate();

        mExecutors = new CoverageAppExecutors();
    }

    private CoverageDatabase getDatabase() {
        return CoverageDatabase.getInstance(this);
    }

    public CoverageRepository getRepository() {
        return CoverageRepository.getInstance(this, getDatabase(), mExecutors);
    }
}
