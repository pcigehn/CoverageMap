package se.pcprogramkonsult.coveragemap.lifecycle;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

abstract class SharedPreferenceLiveData<T> extends LiveData<T> {
    final SharedPreferences mSharedPrefs;
    final String mKey;
    final T mDefValue;

    private final SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @NonNull String key) {
                    if (key.equals(mKey)) {
                        setValue(getValueFromPreferences());
                    }
                }
            };

    SharedPreferenceLiveData(SharedPreferences sharedPrefs, String key, T defValue) {
        mSharedPrefs = sharedPrefs;
        mKey = key;
        mDefValue = defValue;
    }

    protected abstract T getValueFromPreferences();

    @Override
    protected void onActive() {
        super.onActive();
        setValue(getValueFromPreferences());
        mSharedPrefs.registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }

    @Override
    protected void onInactive() {
        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
        super.onInactive();
    }
}
