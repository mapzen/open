package com.mapzen.util;

import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.mapzen.R;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;


public class MapzenGPSPromptDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.gps_dialog_title));
        builder.setMessage(getString(R.string.gps_dialog_message));
        builder.setPositiveButton(getString(R.string.gps_positive_button),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton(getString(R.string.gps_negative_button),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        return dialog;
    }
}

