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
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import se.pcprogramkonsult.coveragemap.lte.Earfcn;
import se.pcprogramkonsult.coveragemap.viewmodel.CoverageMapViewModel;

@SuppressWarnings("WeakerAccess")
public class EarfcnFilterDialogFragment extends DialogFragment {

    private List<EarfcnFilterItem> mCurrentFilterItems = new ArrayList<>();

    private class EarfcnFilterItem {

        final String label;
        @Nullable
        final Earfcn earfcn;

        EarfcnFilterItem(@SuppressWarnings("SameParameterValue") String label) {
            this.label = label;
            this.earfcn = null;
        }

        EarfcnFilterItem(@NonNull Earfcn earfcn) {
            this.label = String.format(Locale.ENGLISH, "%.1f MHz (EARFCN %d)", earfcn.getFrequency(), earfcn.getValue());
            this.earfcn = earfcn;
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
        final ArrayAdapter<EarfcnFilterItem> adapter =
                new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_dropdown_item_1line);
        adapter.add(new EarfcnFilterItem("All frequencies"));

        final CoverageMapViewModel viewModel = new ViewModelProvider(getActivity()).get(CoverageMapViewModel.class);
        viewModel.getUniqueEarfcns().observe(this, earfcns -> {
            mCurrentFilterItems.forEach(adapter::remove);
            if (earfcns != null) {
                mCurrentFilterItems = earfcns.stream().map(EarfcnFilterItem::new).collect(Collectors.toList());
                adapter.addAll(mCurrentFilterItems);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select frequency")
                .setAdapter(adapter, (dialog, which) -> {
                    if (which == 0) {
                        viewModel.selectAllEarfcns();
                    } else {
                        EarfcnFilterItem item = adapter.getItem(which);
                        if (item != null && item.earfcn != null) {
                            viewModel.selectEarfcn(item.earfcn.getValue());
                        }
                    }
                });
        return builder.create();
    }
}
