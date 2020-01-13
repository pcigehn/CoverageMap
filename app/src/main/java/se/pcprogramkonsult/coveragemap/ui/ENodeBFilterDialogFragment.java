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

import se.pcprogramkonsult.coveragemap.db.entity.ENodeBEntity;
import se.pcprogramkonsult.coveragemap.viewmodel.CoverageMapViewModel;

@SuppressWarnings("WeakerAccess")
public class ENodeBFilterDialogFragment extends DialogFragment {

    private List<ENodeBFilterItem> mCurrentFilterItems = new ArrayList<>();

    private class ENodeBFilterItem {

        final String label;
        @Nullable
        final ENodeBEntity eNodeBEntity;

        ENodeBFilterItem(@SuppressWarnings("SameParameterValue") String label) {
            this.label = label;
            this.eNodeBEntity = null;
        }

        ENodeBFilterItem(@NonNull ENodeBEntity eNodeBEntity) {
            String name = eNodeBEntity.getName();
            int id = eNodeBEntity.getId();
            if (name == null) {
                this.label = String.format(Locale.ENGLISH, "%d", id);
            } else {
                this.label = String.format(Locale.ENGLISH, "%s (%d)", name, id);
            }
            this.eNodeBEntity = eNodeBEntity;
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
        final ArrayAdapter<ENodeBFilterItem> adapter =
                new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_dropdown_item_1line);
        adapter.add(new ENodeBFilterItem("All eNodeBs"));

        final CoverageMapViewModel viewModel = new ViewModelProvider(getActivity()).get(CoverageMapViewModel.class);
        viewModel.getAllENodeBs().observe(this, eNodeBEntities -> {
            mCurrentFilterItems.forEach(adapter::remove);
            if (eNodeBEntities != null) {
                mCurrentFilterItems = eNodeBEntities.stream().map(ENodeBFilterItem::new).collect(Collectors.toList());
                adapter.addAll(mCurrentFilterItems);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select eNodeB");
        builder.setAdapter(adapter, (dialog, which) -> {
            if (which == 0) {
                viewModel.selectAllENodeBs();
            } else {
                ENodeBFilterItem item = adapter.getItem(which);
                if (item != null && item.eNodeBEntity != null) {
                    viewModel.selectENodeB(item.eNodeBEntity.getId());
                }
            }
        });
        return builder.create();
    }
}
