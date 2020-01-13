package se.pcprogramkonsult.coveragemap.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Objects;

import se.pcprogramkonsult.coveragemap.db.entity.ENodeBEntity;
import se.pcprogramkonsult.coveragemap.viewmodel.CoverageMapViewModel;

@SuppressWarnings("WeakerAccess")
public class ConfirmUnlocateENodeBDialogFragment extends DialogFragment {

    @Nullable
    private ENodeBEntity mENodeBEntity = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final CoverageMapViewModel viewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(CoverageMapViewModel.class);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Unlocate ENodeB?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (mENodeBEntity != null) {
                        mENodeBEntity.setName(null);
                        mENodeBEntity.setLocated(false);
                        viewModel.updateENodeB(mENodeBEntity);
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {});
        return builder.create();
    }

    public void setENodeBEntity(ENodeBEntity eNodeBEntity) {
        mENodeBEntity = eNodeBEntity;
    }
}
