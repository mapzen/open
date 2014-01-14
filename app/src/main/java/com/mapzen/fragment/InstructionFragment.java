package com.mapzen.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapzen.R;
import com.mapzen.osrm.Instruction;
import com.mapzen.util.Logger;

public class InstructionFragment extends BaseFragment {

    private Instruction instruction;
    public static final int ROUTE_ZOOM_LEVEL = 17;
    private RouteFragment parent;

    public InstructionFragment() {
    }

    public void setParent(RouteFragment parent) {
        this.parent = parent;
    }

    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instruction, container, false);

        TextView fullInstruction = (TextView) view.findViewById(R.id.full_instruction);
        fullInstruction.setText(instruction.getFullInstruction());

        ImageView turnIcon = (ImageView) view.findViewById(R.id.turn_icon);
        turnIcon.setImageResource(getRouteDrawable(instruction.getTurnInstruction()));

        ImageButton next = (ImageButton) view.findViewById(R.id.route_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.next();
            }
        });

        ImageButton prev = (ImageButton) view.findViewById(R.id.route_previous);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.prev();
            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            double[] point = instruction.getPoint();
            Logger.d("Instructions: " + instruction.toString());
            map.setMapPosition(point[0], point[1], Math.pow(2, ROUTE_ZOOM_LEVEL));
            map.getViewport().setRotation(instruction.getBearing());
        }
    }

    private int getRouteDrawable(int turnInstruction) {
        int drawableId = getResources().getIdentifier("ic_route_wh_" + String.valueOf(turnInstruction), "drawable", getActivity().getPackageName());
        if (drawableId == 0) {
            drawableId = R.drawable.ic_route_wh_10;
        }
        return drawableId;
    }
}
