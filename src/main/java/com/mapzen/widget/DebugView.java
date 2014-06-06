package com.mapzen.widget;

import com.mapzen.R;
import com.mapzen.osrm.Instruction;

import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.mapzen.helpers.ZoomController.metersPerSecondToMilesPerHour;

public class DebugView extends RelativeLayout {
    @InjectView(R.id.current_coordinates) TextView currentCoordinates;
    @InjectView(R.id.current_bearing) TextView currentBearing;
    @InjectView(R.id.current_speed) TextView currentSpeed;
    @InjectView(R.id.snap_coordinates) TextView snapCoordinates;
    @InjectView(R.id.instruction_coordinates) TextView instructionCoordinates;
    @InjectView(R.id.instruction_bearing) TextView instructionBearing;
    @InjectView(R.id.instruction_turn) TextView instructionTurn;
    @InjectView(R.id.instruction_name) TextView instructionName;
    @InjectView(R.id.instruction_distance) TextView instructionDistance;
    @InjectView(R.id.instruction_displacement) TextView instructionDisplacement;

    public DebugView(Context context) {
        this(context, null);
    }

    public DebugView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(getContext(), R.layout.debug, this);
        ButterKnife.inject(this);
    }

    public void setClosestInstruction(Instruction instruction, int meters) {
        instructionCoordinates.setText(formatCoordinates(instruction.getLocation()));
        instructionBearing.setText(formatBearing(instruction));
        instructionTurn.setText(instruction.getHumanTurnInstruction());
        instructionName.setText(instruction.getName());
        instructionDistance.setText(instruction.getFormattedDistance());
        instructionDisplacement.setText(meters + " meters away");
    }

    public void setCurrentLocation(Location location) {
        currentCoordinates.setText(formatCoordinates(location));
        currentBearing.setText(formatBearing(location));
        currentSpeed.setText(formatSpeed(location));
    }

    public void setSnapLocation(Location location) {
        snapCoordinates.setText(formatCoordinates(location));
    }

    private String formatCoordinates(Location location) {
        final DecimalFormat df = new DecimalFormat("#.######");
        return df.format(location.getLatitude()) + ", " + df.format(location.getLongitude());
    }

    private String formatBearing(Location location) {
        return Math.round(location.getBearing()) + "°";
    }

    private String formatBearing(Instruction instruction) {
        return instruction.getDirection() + " " + instruction.getBearing() + "°";
    }

    private String formatSpeed(Location location) {
        return Math.round(metersPerSecondToMilesPerHour(location.getSpeed())) + " mph";
    }
}
