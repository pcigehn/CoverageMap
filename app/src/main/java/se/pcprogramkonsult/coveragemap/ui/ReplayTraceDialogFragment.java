package se.pcprogramkonsult.coveragemap.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import se.pcprogramkonsult.coveragemap.db.entity.TraceEntity;
import se.pcprogramkonsult.coveragemap.viewmodel.CoverageMapViewModel;

@SuppressWarnings("WeakerAccess")
public class ReplayTraceDialogFragment extends DialogFragment {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    private List<ReplayTraceItem> mCurrentSelectItems = new ArrayList<>();

    private class ReplayTraceItem {

        @NonNull
        final String label;
        @NonNull
        final TraceEntity traceEntity;

        ReplayTraceItem(@NonNull TraceEntity traceEntity) {
            Date start = traceEntity.getStart();
            String startStr = start == null ? "-" : DATE_FORMAT.format(start);
            this.label = String.format(Locale.ENGLISH, "[%d] %s",
                    traceEntity.getId(), startStr);
            this.traceEntity = traceEntity;
        }

        @NonNull
        @Override
        public String toString() {
            return label;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final ArrayAdapter<ReplayTraceItem> adapter =
                new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_dropdown_item_1line);

        final CoverageMapViewModel viewModel = new ViewModelProvider(getActivity()).get(CoverageMapViewModel.class);
        viewModel.getAllTraces().observe(this, traces -> {
            mCurrentSelectItems.forEach(adapter::remove);
            if (traces != null) {
                mCurrentSelectItems = traces.stream().map(ReplayTraceItem::new).collect(Collectors.toList());
                adapter.addAll(mCurrentSelectItems);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Replay trace")
                .setAdapter(adapter, (dialog, which) -> {
                    ReplayTraceItem item = adapter.getItem(which);
                    if (item != null) {
                        viewModel.startReplayTrace(item.traceEntity.getId());
                    }
                });
        return builder.create();
    }
}
