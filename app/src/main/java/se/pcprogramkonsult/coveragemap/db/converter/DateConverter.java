package se.pcprogramkonsult.coveragemap.db.converter;

import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    @Nullable
    @TypeConverter
    public static Date toDate(@Nullable final Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @Nullable
    @TypeConverter
    public static Long toTimestamp(@Nullable final Date date) {
        return date == null ? null : date.getTime();
    }
}
