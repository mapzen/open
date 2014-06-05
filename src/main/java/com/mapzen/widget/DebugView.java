package com.mapzen.widget;

import com.mapzen.R;
import com.mapzen.osrm.Instruction;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DebugView extends RelativeLayout {
    @InjectView(R.id.position) TextView position;
    @InjectView(R.id.turn) TextView turn;
    @InjectView(R.id.name) TextView name;
    @InjectView(R.id.distance) TextView distance;
    @InjectView(R.id.bearing) TextView bearing;
    @InjectView(R.id.displacement) TextView displacement;

    public DebugView(Context context) {
        this(context, null);
    }

    public DebugView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(getContext(), R.layout.debug, this);
        ButterKnife.inject(this);
    }

    public void setClosestInstruction(Instruction instruction, int meters, int index) {
        position.setText("position " + index);
        turn.setText(instruction.getHumanTurnInstruction());
        name.setText(instruction.getName());
        distance.setText(instruction.getFormattedDistance());
        bearing.setText(instruction.getDirection() + " " + instruction.getBearing() + "°");
        displacement.setText(meters + " meters away");
    }
}
