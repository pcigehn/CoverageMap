package se.pcprogramkonsult.coveragemap.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Objects;

import se.pcprogramkonsult.coveragemap.viewmodel.CoverageMapViewModel;

@SuppressWarnings("WeakerAccess")
public class ConfirmDeleteTraceDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final CoverageMapViewModel viewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(CoverageMapViewModel.class);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Delete trace?")
                .setPositiveButton("Yes", (dialog, which) -> viewModel.deleteCurrentTrace())
                .setNegativeButton("No", (dialog, which) -> {});
        return builder.create();
    }
}
