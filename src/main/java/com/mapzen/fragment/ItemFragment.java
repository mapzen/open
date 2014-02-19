package com.mapzen.fragment;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Feature;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.mapzen.MapController.getMapController;
import static com.mapzen.util.ApiHelper.getRouteUrlForCar;

public class ItemFragment extends BaseFragment {
    private Feature feature;

    @InjectView(R.id.title)
    TextView title;

    @InjectView(R.id.address)
    TextView address;

    @InjectView(R.id.start)
    TextView startButton;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.search_item, container, false);
        ButterKnife.inject(this, view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity) getActivity()).showPlace(feature, false);
            }
        });

        initTitle();
        initAddress();
        initStartButton();
        return view;
    }

    private void initTitle() {
        title.setText(feature.getProperty(Feature.NAME));
    }

    private void initAddress() {
        address.setText(String.format(Locale.getDefault(), "%s, %s",
                feature.getProperty(Feature.ADMIN1_NAME),
                feature.getProperty(Feature.ADMIN1_ABBR)));
    }

    private void initStartButton() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RouteFragment routeFragment = RouteFragment.newInstance(act, feature);
                act.hideActionBar();
                act.showProgressDialog();
                mapFragment.clearMarkers();
                mapFragment.updateMap();
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        getRouteUrlForCar(getMapController().getZoomLevel(),
                                mapFragment.getUserLocationPoint(),
                                routeFragment.getDestinationPoint()),
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                routeFragment.onRouteSuccess(response);
                                act.dismissProgressDialog();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        onServerError(volleyError);
                    }
                }
                );
                app.enqueueApiRequest(jsonObjectRequest);
            }
        });

        startButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(app, "Start where you are...", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }
}
