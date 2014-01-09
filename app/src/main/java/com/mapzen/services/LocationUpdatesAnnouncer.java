package com.mapzen.services;

import android.app.IntentService;
import android.content.Intent;

public class LocationUpdatesAnnouncer extends IntentService {
    public LocationUpdatesAnnouncer() {
        super("LocationUpdatesAnnouncer");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
