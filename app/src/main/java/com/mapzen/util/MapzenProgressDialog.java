package com.mapzen.util;

import android.app.ProgressDialog;
import android.content.Context;

public class MapzenProgressDialog extends ProgressDialog {

    public MapzenProgressDialog(Context context) {
        super(context);
        setTitle("Loading");
        setMessage("Wait while loading");
    }
}
