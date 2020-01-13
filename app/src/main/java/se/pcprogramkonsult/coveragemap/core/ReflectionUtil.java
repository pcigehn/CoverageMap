package se.pcprogramkonsult.coveragemap.core;

import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;

public class ReflectionUtil {
    private static final String TAG = ReflectionUtil.class.getSimpleName();

    public static int getField(@NonNull String name, @NonNull Object source) {
        try {
            Field field = source.getClass().getDeclaredField(name);
            field.setAccessible(true);
            //noinspection ConstantConditions
            return (Integer) field.get(source);
        } catch (Exception e) {
            Log.e(TAG, "Unable to get field " + e);
        }
        return Integer.MAX_VALUE;
    }
}
