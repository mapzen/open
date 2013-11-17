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
import com.mapzen.entity.Place;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.mapzen.MapzenApplication.LOG_TAG;

public class SearchActivity extends Activity {
    private ListView results;
    private final ArrayList<Place> list = new ArrayList<Place>();
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
                Intent intent = new Intent(getBaseContext(), BaseActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("place", (Place)view.getTag(R.string.tag_placeholder));
                intent.putExtras(bundle);
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
                        Place place = null;
                        try {
                            place = Place.fromJson(jsonArray.getJSONObject(i));
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, e.toString());
                        }
                        list.add(place);
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