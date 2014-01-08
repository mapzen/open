package com.mapzen.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapzen.R;
import com.mapzen.osrm.Instruction;
import com.mapzen.util.Logger;

import org.oscim.map.Map;

public class InstructionFragment extends Fragment {

    private Instruction instruction;
    private Map map;
    public static final int ROUTE_ZOOM_LEVEL = 19;
    public static final float ROUTE_TILT_LEVEL = 150.0f;

    public InstructionFragment() {
    }

    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instruction, container, false);
        TextView title = (TextView) view.findViewById(R.id.instruction_title);
        title.setText(instruction.getHumanTurnInstruction());
        TextView street = (TextView) view.findViewById(R.id.instruction_street);
        street.setText(instruction.getName());
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            double[] point = instruction.getPoint();
            Logger.d("Instructions: " + instruction.toString());
            map.setMapPosition(point[0], point[1], Math.pow(2, ROUTE_ZOOM_LEVEL));
            map.getViewport().setTilt(ROUTE_TILT_LEVEL);
            map.getViewport().setRotation(instruction.getBearing());
        }
    }
}
