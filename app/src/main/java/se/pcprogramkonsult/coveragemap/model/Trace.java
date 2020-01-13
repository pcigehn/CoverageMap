package se.pcprogramkonsult.coveragemap.model;

import androidx.annotation.Nullable;

import java.util.Date;

public interface Trace {
    long getId();

    @Nullable
    Date getStart();

    @Nullable
    String getName();
}
