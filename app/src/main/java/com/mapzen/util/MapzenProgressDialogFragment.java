package com.mapzen.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.mapzen.R;

public class MapzenProgressDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getString(R.string.progress_dialog_title));
        progressDialog.setMessage(getString(R.string.progress_dialog_message));
        return progressDialog;
    }
}
