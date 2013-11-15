package com.mapzen.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.*;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.SearchViewAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.mapzen.MapzenApplication.LOG_TAG;

public class SearchActivity extends Activity {
    private ListView results;
    private final ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String, String>>();
    SearchViewAdapter adapter;
    private String baseUrl = "http://open.mapquestapi.com/nominatim/v1/search.php?format=json&q=";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results);
        results = (ListView) findViewById(R.id.results);
        adapter = new SearchViewAdapter(this,
                R.id.results, list);
        results.setAdapter(adapter);
        results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView lat = (TextView)view.findViewById(R.id.lat);
                TextView lon = (TextView)view.findViewById(R.id.lon);
                int[] coordinates = {
                        (int) (Float.parseFloat(lat.getText().toString()) * 1E6),
                        (int) (Float.parseFloat(lon.getText().toString()) * 1E6)
                };
                Intent intent = new Intent(getBaseContext(), BaseActivity.class);
                intent.putExtra("lat", coordinates[0]);
                intent.putExtra("lon", coordinates[1]);
                startActivity(intent);
            }
        });
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
                    Log.v(LOG_TAG, jsonArray.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = null;
                        String displayName = "";
                        HashMap<String, String> hashObj = new HashMap<String, String>();
                        try {
                            obj = jsonArray.getJSONObject(i);
                            displayName = obj.getString("display_name");
                            hashObj.put("display_name", displayName);
                            hashObj.put("lon", obj.getString("lon"));
                            hashObj.put("lat", obj.getString("lat"));
                        } catch(JSONException e) {

                        }
                        list.add(hashObj);
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