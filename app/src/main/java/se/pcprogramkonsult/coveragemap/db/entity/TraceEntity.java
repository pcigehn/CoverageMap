package se.pcprogramkonsult.coveragemap.db.entity;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

import se.pcprogramkonsult.coveragemap.model.Trace;

@Entity(tableName = "traces")
public class TraceEntity implements Trace {
    @PrimaryKey(autoGenerate = true)
    private long id = 0;

    @Nullable
    private final Date start;

    @Nullable
    private String name;

    public TraceEntity(final long id, @Nullable final Date start, @Nullable final String name) {
        this.id = id;
        this.start = start;
        this.name = name;
    }

    @Ignore
    public TraceEntity() {
        this.start = new Date();
        this.name = null;
    }

    @Override
    public long getId() {
        return id;
    }

    @Nullable
    @Override
    public Date getStart() {
        return start;
    }

    @SuppressWarnings("unused")
    @Nullable
    @Override
    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    public void setName(@Nullable final String name) {
        this.name = name;
    }
}
