package com.mapzen.widget;

import com.mapzen.R;
import com.mapzen.osrm.Instruction;

import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.mapzen.helpers.ZoomController.metersPerSecondToMilesPerHour;
import static java.lang.Math.round;

public class DebugView extends RelativeLayout {

    @InjectView(R.id.current_coordinates) TextView currentCoordinates;
    @InjectView(R.id.current_bearing) TextView currentBearing;
    @InjectView(R.id.current_speed) TextView currentSpeed;

    @InjectView(R.id.instruction_index) TextView instructionIndex;
    @InjectView(R.id.instruction_turn) TextView instructionTurn;
    @InjectView(R.id.instruction_name) TextView instructionName;
    @InjectView(R.id.instruction_distance) TextView instructionDistance;
    @InjectView(R.id.instruction_bearing) TextView instructionBearing;
    @InjectView(R.id.instruction_coordinates) TextView instructionCoordinates;
    @InjectView(R.id.instruction_displacement) TextView instructionDisplacement;

    public DebugView(Context context) {
        this(context, null);
    }

    public DebugView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(getContext(), R.layout.debug, this);
        ButterKnife.inject(this);
    }

    public void setClosestInstruction(Instruction instruction, int meters, int index) {
        final Location location = instruction.getLocation();
        instructionIndex.setText("index " + index);
        instructionTurn.setText(instruction.getHumanTurnInstruction());
        instructionName.setText(instruction.getName());
        instructionDistance.setText(instruction.getFormattedDistance());
        instructionBearing.setText(instruction.getDirection() + " "
                + instruction.getBearing() + "°");
        instructionCoordinates.setText(location.getLatitude() + ", " + location.getLongitude());
        instructionDisplacement.setText(meters + " meters away");
    }

    public void setCurrentLocation(Location location) {
        currentCoordinates.setText(location.getLatitude() + ", " + location.getLongitude());
        currentBearing.setText(round(location.getBearing()) + "°");
        currentSpeed.setText(round(metersPerSecondToMilesPerHour(location.getSpeed())) + " mph");
    }
}
