package com.mapzen.shadows;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.List;

import static com.android.volley.Response.Listener;
import static org.fest.reflect.core.Reflection.field;

/**
 * Shadow implementation for {@link com.android.volley.toolbox.Volley}.
 */
@SuppressWarnings("unused")
@Implements(Volley.class)
public final class ShadowVolley {
    private static MockRequestQueue requestQueue = new MockRequestQueue();

    private ShadowVolley() {
    }

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
        private final List<Request> requests = new ArrayList<Request>();

        public MockRequestQueue() {
            super(null, null);
        }

        @Override
        public Request add(Request request) {
            requests.add(request);
            return super.add(request);
        }

        public List<Request> getRequests() {
            return requests;
        }

        @SuppressWarnings("unchecked")
        public void deliverResponse(JsonObjectRequest request, JSONObject response) {
            field("mListener").ofType(Listener.class).in(request).get().onResponse(response);
        }
    }
}
