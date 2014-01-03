package com.mapzen.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mapzen.R;
import com.mapzen.osrm.Instruction;

import org.oscim.map.Map;

import java.util.ArrayList;

public class RouteWidgetFragment extends Fragment {
    private ArrayList<Instruction> instructions;
    private MapFragment mapFragment;
    private TextView title, street;
    private int routeIndex;
    private Button nextBtn;

    public RouteWidgetFragment(ArrayList<Instruction> instructions, MapFragment mapFragment) {
        this.instructions = instructions;
        this.mapFragment = mapFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        routeIndex = 0;
        View rootView = inflater.inflate(R.layout.route_widget, container, false);
        FrameLayout frame = (FrameLayout)container;
        frame.setVisibility(View.VISIBLE);
        title = (TextView) rootView.findViewById(R.id.instruction_title);
        street = (TextView) rootView.findViewById(R.id.instruction_street);
        nextBtn = (Button) rootView.findViewById(R.id.next_btn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeIndex++;
                setRoute(routeIndex);
            }
        });
        setRoute(routeIndex);
        return rootView;
    }

    private void setRoute(int index) {
        title.setText(instructions.get(index).getHumanTurnInstruction());
        street.setText(instructions.get(index).getName());

        double[] firstPoint = instructions.get(index).getPoint();
        Map map = mapFragment.getMap();
        map.setMapPosition(firstPoint[0], firstPoint[1], Math.pow(2, 19));
        map.getViewport().setTilt(150.0f);
        map.getViewport().setRotation(instructions.get(index).getBearing());
    }
}
