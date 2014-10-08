package com.mapzen.open.route;

import com.mapzen.open.R;
import com.mapzen.osrm.Instruction;
import com.mapzen.open.util.DisplayHelper;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RouteAdapter extends PagerAdapter {
    public static final String TAG_BASE = "Instruction_";

    private List<Instruction> instructions = new ArrayList<Instruction>();
    private Context context;
    private Instruction currentInstruction;
    private RouteFragment fragment;
    private String destinationName;

    public RouteAdapter(Context context, List<Instruction> instructions, RouteFragment fragment) {
        this.context = context;
        this.instructions = instructions;
        this.fragment = fragment;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        currentInstruction = instructions.get(position);
        final View view = View.inflate(context, R.layout.instruction, null);
        setBackgroundColor(position, view);
        setFullInstruction(view);
        setFullInstructionAfterAction(view);
        setTurnIcon(view);
        setTurnIconAfterAction(view);
        setDistance(view);
        showLeftAndRightArrows(position, view);
        initArrowOnClickListeners(position, view);
        setTag(position, view);
        container.addView(view);
        if (position == instructions.size() - 1) {
            showDestinationView(view);
        }

        return view;
    }

    private void showDestinationView(View view) {
        view.findViewById(R.id.full_instruction).setVisibility(View.GONE);
        view.findViewById(R.id.full_instruction_after_action).setVisibility(View.GONE);
        view.findViewById(R.id.you_have_arrived).setVisibility(View.VISIBLE);
        view.findViewById(R.id.turn_container).setVisibility(View.GONE);
        view.findViewById(R.id.destination_icon).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.destination_banner)).setText(destinationName);
    }

    private void setBackgroundColor(int position, View view) {
        if (position == instructions.size() - 1) {
            view.setBackgroundColor(context.getResources().getColor(R.color.destination_color));
        } else {
            view.setBackgroundColor(context.getResources().getColor(R.color.transparent_gray));
        }
    }

    private void showLeftAndRightArrows(int position, View view) {
        if (position == 0) {
            view.findViewById(R.id.left_arrow).setVisibility(View.INVISIBLE);
        } else if (position == instructions.size() - 1) {
            view.findViewById(R.id.right_arrow).setVisibility(View.INVISIBLE);
        } else {
            view.findViewById(R.id.left_arrow).setVisibility(View.VISIBLE);
            view.findViewById(R.id.right_arrow).setVisibility(View.VISIBLE);
        }
    }

    private void initArrowOnClickListeners(int position, View view) {
        final int pos = position;
        view.findViewById(R.id.left_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.turnAutoPageOff();
                fragment.pageToPrevious(pos);
            }
        });
        view.findViewById(R.id.right_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.turnAutoPageOff();
                fragment.pageToNext(pos);
            }
        });
    }

    private void setFullInstruction(View view) {
        final TextView fullInstruction = (TextView) view.findViewById(R.id.full_instruction);
        fullInstruction.setText(getFullInstructionWithBoldName(currentInstruction
                .getSimpleInstruction()));
    }

    private void setDistance(View view) {
        final TextView distance = (TextView) view.findViewById(R.id.distance_instruction);
        distance.setText(currentInstruction.getFormattedDistance());
    }

    private void setFullInstructionAfterAction(View view) {
        final TextView fullInstructionAfterAction =
                (TextView) view.findViewById(R.id.full_instruction_after_action);
        fullInstructionAfterAction.setText(
                getFullInstructionWithBoldName(
                        currentInstruction.getSimpleInstructionAfterAction()));
    }

    private void setTurnIcon(View view) {
        final ImageView turnIcon = (ImageView) view.findViewById(R.id.turn_icon);
        turnIcon.setImageResource(DisplayHelper.getRouteDrawable(context,
                currentInstruction.getTurnInstruction(), DisplayHelper.IconStyle.GRAY));
    }

    private void setTurnIconAfterAction(View view) {
        final ImageView turnIconAfterAction =
                (ImageView) view.findViewById(R.id.turn_icon_after_action);
        turnIconAfterAction.setImageResource(DisplayHelper.getRouteDrawable(context,
                10, DisplayHelper.IconStyle.GRAY));
    }

    private void setTag(int position, View view) {
        view.setTag(TAG_BASE + String.valueOf(position));
    }

    private SpannableStringBuilder getFullInstructionWithBoldName(String fullInstruction) {
        final String name = currentInstruction.getName();
        final int startOfName = fullInstruction.indexOf(name);
        final int endOfName = startOfName + name.length();
        final StyleSpan boldStyleSpan = new StyleSpan(Typeface.BOLD);

        final SpannableStringBuilder ssb = new SpannableStringBuilder(fullInstruction);
        ssb.setSpan(boldStyleSpan, startOfName, endOfName, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return ssb;
    }

    @Override
    public int getCount() {
        if (instructions == null) {
            return 0;
        }

        return instructions.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void setTurnIcon(View view, int resId) {
        if (view != null) {
            ImageView turnIcon = (ImageView) view.findViewById(R.id.turn_icon);
            turnIcon.setImageResource(resId);
            setTurnIconAfterAction(view, resId);
        }
    }

    public void setTurnIconAfterAction(View view, int resId) {
        if (view != null) {
            ImageView turnIconAfterAction = (ImageView) view
                    .findViewById(R.id.turn_icon_after_action);
            turnIconAfterAction.setImageResource(resId);
        }
    }

    public void setBackgroundColorActive(View view) {
        if (view != null) {
            view.setBackgroundColor(context.getResources().getColor(R.color.transparent_white));
        }
    }

    public void setBackgroundColorInactive(View view) {
        if (view != null) {
            view.setBackgroundColor(context.getResources().getColor(R.color.transparent_gray));
        }
    }

    public void setBackgroundColorComplete(View view) {
        if (view != null) {
            view.setBackgroundColor(context.getResources().getColor(R.color.destination_color));
        }
    }
}
