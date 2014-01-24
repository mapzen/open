package com.mapzen.shadows;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow implementation for {@link com.android.volley.toolbox.Volley}.
 */
@SuppressWarnings("unused")
@Implements(Volley.class)
public class ShadowVolley {
    @Implementation
    public static RequestQueue newRequestQueue(Context context) {
        return new MockRequestQueue();
    }

    private static class MockRequestQueue extends RequestQueue {
        public MockRequestQueue() {
            super(null, null);
        }
    }
}