package se.pcprogramkonsult.coveragemap.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

import se.pcprogramkonsult.coveragemap.R;
import se.pcprogramkonsult.coveragemap.db.entity.ENodeBEntity;
import se.pcprogramkonsult.coveragemap.viewmodel.CoverageMapViewModel;

@SuppressWarnings("WeakerAccess")
public class EditENodeBDialogFragment extends DialogFragment {

    @Nullable
    private CoverageMapViewModel mViewModel = null;
    @Nullable
    private ENodeBEntity mENodeBEntity = null;
    @Nullable
    private EditText mENodeBName = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(CoverageMapViewModel.class);
        final Integer eNodeBId;
        final LatLng latLng;
        Bundle bundle = getArguments();
        if (bundle != null) {
            eNodeBId = bundle.getInt("eNodeB");
            latLng = bundle.getParcelable("latLng");
        } else {
            eNodeBId = null;
            latLng = null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View editENodeB = inflater.inflate(R.layout.dialog_edit_enodeb, null);
        mENodeBName = editENodeB.findViewById(R.id.enodeb_name);
        builder.setMessage("Has this eNodeB been located?")
                .setView(editENodeB)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (mENodeBEntity != null && latLng != null && mENodeBName != null) {
                        mENodeBEntity.setName(mENodeBName.getText().toString());
                        mENodeBEntity.setLocatedLatLng(latLng);
                        mViewModel.updateENodeB(mENodeBEntity);
                    }
                })
                .setNeutralButton("Cancel", (dialog, which) -> {
                    if (mENodeBEntity != null) {
                        mViewModel.updateENodeB(mENodeBEntity);
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    if (mENodeBEntity != null) {
                        if (mENodeBEntity.isLocated()) {
                            ConfirmUnlocateENodeBDialogFragment confirmUnlocateENodeBDialogFragment = new ConfirmUnlocateENodeBDialogFragment();
                            confirmUnlocateENodeBDialogFragment.setENodeBEntity(mENodeBEntity);
                            confirmUnlocateENodeBDialogFragment.show(getActivity().getSupportFragmentManager(), "confirm_unlocate_enodeb");
                        } else {
                            mENodeBEntity.setName(null);
                            mENodeBEntity.setLocated(false);
                            mViewModel.updateENodeB(mENodeBEntity);
                        }
                    }
                });
        final AlertDialog result = builder.create();
        result.setOnShowListener(dialog -> {
            final Button positiveButton = result.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setEnabled(false);
            final Button negativeButton = result.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setEnabled(false);
            if (eNodeBId != null) {
                final LiveData<ENodeBEntity> eNodeB = mViewModel.getENodeB(eNodeBId);
                eNodeB.observe(this, eNodeBEntity -> {
                    mENodeBEntity = eNodeBEntity;
                    if (mENodeBName != null && mENodeBEntity.getName() != null && ! mENodeBEntity.getName().isEmpty()) {
                        mENodeBName.setText(mENodeBEntity.getName());
                    }
                    positiveButton.setEnabled(true);
                    negativeButton.setEnabled(true);
                });
            }
        });
        return result;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        if (mViewModel != null && mENodeBEntity != null) {
            mViewModel.updateENodeB(mENodeBEntity);
        }
        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (mViewModel != null && mENodeBEntity != null) {
            mViewModel.updateENodeB(mENodeBEntity);
        }
        super.onDismiss(dialog);
    }
}
