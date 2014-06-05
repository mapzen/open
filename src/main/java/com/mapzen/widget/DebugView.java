package com.mapzen.widget;

import com.mapzen.R;
import com.mapzen.osrm.Instruction;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DebugView extends RelativeLayout {
    private TextView position;
    private TextView turn;
    private TextView name;
    private TextView distance;
    private TextView bearing;
    private TextView howFarAway;

    public DebugView(Context context) {
        this(context, null);
    }

    public DebugView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(getContext(), R.layout.debug, this);
        position = (TextView) findViewById(R.id.position);
        turn = (TextView) findViewById(R.id.turn);
        name = (TextView) findViewById(R.id.name);
        distance = (TextView) findViewById(R.id.distance);
        bearing = (TextView) findViewById(R.id.bearing);
        howFarAway = (TextView) findViewById(R.id.how_far_away);
    }

    public void setClosestInstruction(Instruction instruction, int meters, int index) {
        position.setText("position " + index);
        turn.setText(instruction.getHumanTurnInstruction());
        name.setText(instruction.getName());
        distance.setText(instruction.getFormattedDistance());
        bearing.setText(instruction.getDirection() + " " + instruction.getBearing() + "°");
        howFarAway.setText(meters + " meter(s) away");
    }
}
