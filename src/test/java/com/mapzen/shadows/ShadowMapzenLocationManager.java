package com.mapzen.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.shadows.ShadowPendingIntent;

import android.app.PendingIntent;
import android.location.LocationManager;

import java.util.ArrayList;
import java.util.List;

import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings("unused")
@Implements(LocationManager.class)
public class ShadowMapzenLocationManager extends ShadowLocationManager {
    List<ShadowPendingIntent> proximityAlerts = new ArrayList<ShadowPendingIntent>();

    @Implementation
    public void addProximityAlert(double latitude, double longitude, float radius, long expiration,
            PendingIntent intent) {
        proximityAlerts.add(shadowOf(intent));
    }

    @Implementation
    public void removeProximityAlert(PendingIntent intent) {
        proximityAlerts.remove(shadowOf(intent));
    }

    public List<ShadowPendingIntent> getProximityAlerts() {
        return proximityAlerts;
    }

}
