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

import se.pcprogramkonsult.coveragemap.lte.parameter.CiParameter;
import se.pcprogramkonsult.coveragemap.lte.parameter.ENodeBParameter;
import se.pcprogramkonsult.coveragemap.lte.parameter.MeasurementParameter;
import se.pcprogramkonsult.coveragemap.lte.parameter.RsrpParameter;
import se.pcprogramkonsult.coveragemap.lte.parameter.RsrqParameter;
import se.pcprogramkonsult.coveragemap.lte.parameter.ServingCiParameter;
import se.pcprogramkonsult.coveragemap.lte.parameter.ServingENodeBParameter;
import se.pcprogramkonsult.coveragemap.lte.parameter.TaParameter;
import se.pcprogramkonsult.coveragemap.viewmodel.CoverageMapViewModel;

@SuppressWarnings("WeakerAccess")
public class SelectParameterDialogFragment extends DialogFragment {

    private final List<SelectParameterItem> mCurrentSelectItems = new ArrayList<>();

    private class SelectParameterItem {

        @NonNull
        final String label;
        @NonNull
        final MeasurementParameter parameter;

        SelectParameterItem(@NonNull MeasurementParameter parameter) {
            this.label = parameter.getName();
            this.parameter = parameter;
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
        final ArrayAdapter<SelectParameterItem> adapter =
                new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_dropdown_item_1line);

        final CoverageMapViewModel viewModel = new ViewModelProvider(getActivity()).get(CoverageMapViewModel.class);
        mCurrentSelectItems.add(new SelectParameterItem(new RsrpParameter()));
        mCurrentSelectItems.add(new SelectParameterItem(new RsrqParameter()));
        mCurrentSelectItems.add(new SelectParameterItem(new TaParameter()));
        mCurrentSelectItems.add(new SelectParameterItem(new ServingENodeBParameter()));
        mCurrentSelectItems.add(new SelectParameterItem(new ServingCiParameter()));
        mCurrentSelectItems.add(new SelectParameterItem(new ENodeBParameter()));
        mCurrentSelectItems.add(new SelectParameterItem(new CiParameter()));
        adapter.addAll(mCurrentSelectItems);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select parameter")
                .setAdapter(adapter, (dialog, which) -> {
                    SelectParameterItem item = adapter.getItem(which);
                    if (item != null) {
                        viewModel.selectMeasurementParameter(item.parameter);
                    }
                });
        return builder.create();
    }
}
