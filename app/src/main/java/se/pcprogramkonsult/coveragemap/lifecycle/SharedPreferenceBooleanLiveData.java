package se.pcprogramkonsult.coveragemap.lifecycle;

import android.content.SharedPreferences;

public class SharedPreferenceBooleanLiveData extends SharedPreferenceLiveData<Boolean> {
    public SharedPreferenceBooleanLiveData(SharedPreferences sharedPrefs, String key, Boolean defValue) {
        super(sharedPrefs, key, defValue);
    }

    @Override
    protected Boolean getValueFromPreferences() {
        return mSharedPrefs.getBoolean(mKey, mDefValue);
    }
}