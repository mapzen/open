package com.mapzen.route;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.mapzen.R;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.osrm.Instruction;
import com.mapzen.util.DisplayHelper;
import com.mapzen.widget.DistanceView;

import java.util.List;

import static com.mapzen.entity.SimpleFeature.TEXT;

public class DirectionListFragment extends ListFragment {
    public static final String TAG = DirectionListFragment.class.getSimpleName();
    private List<Instruction> instructions;
    private DirectionListener listener;
    private SimpleFeature destination;
    private boolean reverse;

    @InjectView(R.id.starting_point_list) TextView startingPointTextView;
    @InjectView(R.id.destination_list) TextView destinationTextView;
    @InjectView(R.id.starting_location_icon_list) ImageView startLocationIcon;
    @InjectView(R.id.destination_location_icon_list) ImageView destinationLocationIcon;
    public static DirectionListFragment newInstance(List<Instruction> instructions,
            DirectionListener listener,  SimpleFeature destination, boolean reverse) {
        final DirectionListFragment fragment = new DirectionListFragment();
        fragment.instructions = instructions;
        fragment.listener = listener;
        fragment.destination = destination;
        fragment.reverse = reverse;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_direction_list, container, false);
        ButterKnife.inject(this, view);
        final ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(new DirectionListAdapter(getActivity(), instructions, reverse));
        setOriginAndDestination();
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (listener != null) {
            listener.onInstructionSelected(position - 1);
        }

        getActivity().onBackPressed();
    }

    public interface DirectionListener {
        public void onInstructionSelected(int index);
    }

    private static class DirectionListAdapter extends BaseAdapter {
        private static final int CURRENT_LOCATION_OFFSET = 1;
        private Context context;
        private List<Instruction> instructions;
        private boolean reversed;

        public DirectionListAdapter(Context context, List<Instruction> instructions,
                                    boolean reversed) {
            this.context = context;
            this.instructions = instructions;
            this.reversed = reversed;
        }

        @Override
        public int getCount() {
            return instructions.size() + CURRENT_LOCATION_OFFSET;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view = View.inflate(context, R.layout.direction_list_item, null);
            if (reversed) {
                setReversedDirectionListItem(position, view);
            } else {
                setDirectionListItem(position, view);
            }
            return view;
        }

        private void setDirectionListItem(int position, View view) {
            if (position == 0) {
                setListItemToCurrentLocation(view);
            } else {
                setListItemToInstruction(view, position - CURRENT_LOCATION_OFFSET);
            }
        }

        private void setReversedDirectionListItem(int position, View view) {
            if (position == instructions.size()) {
                setListItemToCurrentLocation(view);
            } else {
                setListItemToInstruction(view, position);
            }
        }

        public void setListItemToCurrentLocation(View view) {
            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            TextView simpleInstruction = (TextView)
                    view.findViewById(R.id.simple_instruction);

            icon.setImageResource(R.drawable.ic_locate_active);
            simpleInstruction.setText(context.getResources()
                    .getString(R.string.current_location));
        }

        public void setListItemToInstruction(View view, int position) {
            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            TextView simpleInstruction = (TextView)
                    view.findViewById(R.id.simple_instruction);
            DistanceView distance = (DistanceView) view.findViewById(R.id.distance);
            Instruction current = instructions.get(position);

            icon.setImageResource(DisplayHelper.getRouteDrawable(context,
                    current.getTurnInstruction(), DisplayHelper.IconStyle.GRAY));
            simpleInstruction.setText(current.getSimpleInstruction());
            distance.setDistance(current.getDistance());
        }
    }

    private void setOriginAndDestination() {
        if (reverse) {
            startingPointTextView.setText(destination.getProperty(TEXT));
            destinationTextView.setText(getString(R.string.current_location));
            startLocationIcon.setVisibility(View.GONE);
            destinationLocationIcon.setVisibility(View.VISIBLE);
        } else {
            startingPointTextView.setText(getString(R.string.current_location));
            destinationTextView.setText(destination.getProperty(TEXT));
            startLocationIcon.setVisibility(View.VISIBLE);
            destinationLocationIcon.setVisibility(View.GONE);
        }
    }
}
