package com.mapzen.shadows;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.HashSet;
import java.util.Set;

/**
 * Shadow implementation for {@link com.android.volley.toolbox.Volley}.
 */
@SuppressWarnings("unused")
@Implements(Volley.class)
public class ShadowVolley {
    private static MockRequestQueue requestQueue = new MockRequestQueue();

    @Implementation
    public static RequestQueue newRequestQueue(Context context) {
        return requestQueue;
    }

    public static MockRequestQueue getMockRequestQueue() {
        return requestQueue;
    }

    public static void clearMockRequestQueue() {
        requestQueue.getRequests().clear();
    }

    public static class MockRequestQueue extends RequestQueue {
        private final Set<Request> requests = new HashSet<Request>();

        public MockRequestQueue() {
            super(null, null);
        }

        @Override
        public Request add(Request request) {
            requests.add(request);
            return super.add(request);
        }

        public Set<Request> getRequests() {
            return requests;
        }
    }
}