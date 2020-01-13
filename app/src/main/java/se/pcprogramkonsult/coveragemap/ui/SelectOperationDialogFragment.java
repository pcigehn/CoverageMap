package se.pcprogramkonsult.coveragemap.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import se.pcprogramkonsult.coveragemap.lte.operation.AverageOperation;
import se.pcprogramkonsult.coveragemap.lte.operation.MaxOperation;
import se.pcprogramkonsult.coveragemap.lte.operation.MeasurementOperation;
import se.pcprogramkonsult.coveragemap.lte.operation.MedianOperation;
import se.pcprogramkonsult.coveragemap.lte.operation.MinOperation;
import se.pcprogramkonsult.coveragemap.lte.operation.UniqueOperation;
import se.pcprogramkonsult.coveragemap.viewmodel.CoverageMapViewModel;

@SuppressWarnings("WeakerAccess")
public class SelectOperationDialogFragment extends DialogFragment {

    private final List<SelectOperationItem> mCurrentSelectItems = new ArrayList<>();

    private class SelectOperationItem {

        @NonNull
        final String label;
        @NonNull
        final MeasurementOperation operation;

        SelectOperationItem(@NonNull MeasurementOperation operation) {
            this.label = operation.getName();
            this.operation = operation;
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
        final ArrayAdapter<SelectOperationItem> adapter =
                new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_dropdown_item_1line);

        final CoverageMapViewModel viewModel = new ViewModelProvider(getActivity()).get(CoverageMapViewModel.class);
        mCurrentSelectItems.add(new SelectOperationItem(new MaxOperation()));
        mCurrentSelectItems.add(new SelectOperationItem(new MinOperation()));
        mCurrentSelectItems.add(new SelectOperationItem(new AverageOperation()));
        mCurrentSelectItems.add(new SelectOperationItem(new MedianOperation()));
        mCurrentSelectItems.add(new SelectOperationItem(new UniqueOperation()));
        adapter.addAll(mCurrentSelectItems);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select operation")
                .setAdapter(adapter, (dialog, which) -> {
                    SelectOperationItem item = adapter.getItem(which);
                    if (item != null) {
                        viewModel.selectMeasurementOperation(item.operation);
                    }
                });
        return builder.create();
    }
}
