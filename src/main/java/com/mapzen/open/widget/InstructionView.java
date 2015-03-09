package com.mapzen.open.widget;

import com.mapzen.open.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class InstructionView extends LinearLayout {
    @InjectView(R.id.turn_container) View turnContainer;
    @InjectView(R.id.instruction_container) View instructionContainer;
    @InjectView(R.id.turn_icon) View turnIcon;
    @InjectView(R.id.full_instruction) View fullInstruction;
    @InjectView(R.id.full_instruction_after_action) View fullInstructionAfterAction;
    @InjectView(R.id.you_have_arrived) View youHaveArrived;
    @InjectView(R.id.destination_icon) View destinationIcon;
    @InjectView(R.id.destination_banner) TextView destinationBanner;

    public InstructionView(Context context) {
        super(context);
        initLayoutParams();
        inflateLayout(context);
        ButterKnife.inject(this);
        setBackgroundColor(getResources().getColor(R.color.transparent_gray));
    }

    private void initLayoutParams() {
        final LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.instruction_height));
        setLayoutParams(params);
    }

    private void inflateLayout(Context context) {
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.instruction, this, true);
    }

    @Override
    public void setBackgroundColor(int color) {
        turnContainer.setBackgroundColor(color);
        instructionContainer.setBackgroundColor(color);
    }

    public void setDestination(String destinationName) {
        turnIcon.setVisibility(GONE);
        fullInstruction.setVisibility(GONE);
        fullInstructionAfterAction.setVisibility(GONE);
        youHaveArrived.setVisibility(VISIBLE);
        destinationIcon.setVisibility(VISIBLE);
        destinationBanner.setText(destinationName);
        setBackgroundColor(getResources().getColor(R.color.destination_color));
    }
}
