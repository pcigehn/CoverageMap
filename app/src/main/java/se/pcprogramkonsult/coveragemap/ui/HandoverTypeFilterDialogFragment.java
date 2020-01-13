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

import se.pcprogramkonsult.coveragemap.viewmodel.CoverageMapViewModel;

@SuppressWarnings("WeakerAccess")
public class HandoverTypeFilterDialogFragment extends DialogFragment {

    private final List<HandoverTypeFilterItem> mCurrentSelectItems = new ArrayList<>();

    private class HandoverTypeFilterItem {

        @NonNull
        final String label;
        @NonNull
        final HandoverType handoverType;

        HandoverTypeFilterItem(@NonNull String label, @NonNull HandoverType handoverType) {
            this.label = label;
            this.handoverType = handoverType;
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
        final ArrayAdapter<HandoverTypeFilterItem> adapter =
                new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_dropdown_item_1line);

        final CoverageMapViewModel viewModel = new ViewModelProvider(getActivity()).get(CoverageMapViewModel.class);
        mCurrentSelectItems.add(new HandoverTypeFilterItem("No Handover", HandoverType.NONE));
        mCurrentSelectItems.add(new HandoverTypeFilterItem("Intra-Frequency Handover", HandoverType.INTRA));
        mCurrentSelectItems.add(new HandoverTypeFilterItem("Inter-Frequency Handover", HandoverType.INTER));
        mCurrentSelectItems.add(new HandoverTypeFilterItem("Both Intra- and Inter-Frequency Handover", HandoverType.BOTH));
        adapter.addAll(mCurrentSelectItems);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select handover type")
                .setAdapter(adapter, (dialog, which) -> {
                    HandoverTypeFilterItem item = adapter.getItem(which);
                    if (item != null) {
                        viewModel.selectHandoverType(item.handoverType);
                    }
                });
        return builder.create();
    }
}
