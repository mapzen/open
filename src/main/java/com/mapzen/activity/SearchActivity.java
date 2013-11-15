package com.mapzen.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;

public class SearchActivity extends Activity {
    private String baseUrl = "http://open.mapquestapi.com/nominatim/v1/search.php?format=json&q=";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("SEARCH: stuff", "aerting");
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = baseUrl + query;

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray jsonArray) {
                    Log.v("stuff", jsonArray.toString());
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.v("stuff", volleyError.toString());
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });
            queue.add(jsonArrayRequest);
        }
    }
}