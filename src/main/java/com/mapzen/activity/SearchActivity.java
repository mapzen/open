package com.mapzen.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.android.volley.*;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.mapzen.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchActivity extends Activity {
    private ListView results;
    private final ArrayList<String> list = new ArrayList<String>();
    ArrayAdapter adapter;
    private String baseUrl = "http://open.mapquestapi.com/nominatim/v1/search.php?format=json&q=";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results);
        results = (ListView) findViewById(R.id.results);
        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        results.setAdapter(adapter);
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
            double[] box = intent.getDoubleArrayExtra("box");
            String url = baseUrl + query + "&bounded=1&viewbox=" + Double.toString(box[0]) + "," + Double.toString(box[1]) + "," + Double.toString(box[2]) + "," + Double.toString(box[3]);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray jsonArray) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = null;
                        String displayName = "";
                        try {
                            obj = jsonArray.getJSONObject(i);
                            displayName = obj.getString("display_name");
                        } catch(JSONException e) {

                        }
                        list.add(displayName);
                    }
                    adapter.notifyDataSetChanged();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                }
            });
            queue.add(jsonArrayRequest);
        }
    }
}