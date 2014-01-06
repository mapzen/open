package com.mapzen.util;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.mapzen.R;

import java.util.Locale;

public class VolleyHelper {
    static public class Error {
        public static String getMessage(Object error, Context context) {
            if (error instanceof TimeoutError) {
                return context.getResources().getString(R.string.request_timed_out);
            } else if (isServerProblem(error)) {
                return handleServerError(error, context);
            } else if (isNetworkProblem(error)) {
                return context.getResources().getString(R.string.no_internet);
            }
            return context.getResources().getString(R.string.generic_error);
        }

        private static boolean isNetworkProblem(Object error) {
            return (error instanceof NetworkError) || (error instanceof NoConnectionError);
        }

        private static boolean isServerProblem(Object error) {
            return (error instanceof ServerError) || (error instanceof AuthFailureError);
        }

        private static String handleServerError(Object err, Context context) {
            VolleyError error = (VolleyError) err;
            NetworkResponse response = error.networkResponse;
            if (response != null) {
                switch (response.statusCode) {
                    case 404:
                    case 422:
                    case 401:
                        return String.format(Locale.ENGLISH, "%d: %s", response.statusCode, error.getMessage());
                    default:
                        return String.format(Locale.ENGLISH, "%d: %s", response.statusCode, context.getResources().getString(R.string.generic_server_down));
                }
            }
            return context.getResources().getString(R.string.generic_error);
        }
    }
}
