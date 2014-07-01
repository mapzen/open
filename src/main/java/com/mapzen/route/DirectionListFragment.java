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
import com.mapzen.R;
import com.mapzen.osrm.Instruction;
import com.mapzen.util.DisplayHelper;
import com.mapzen.widget.DistanceView;

import java.util.List;

public class DirectionListFragment extends ListFragment {
    public static final String TAG = DirectionListFragment.class.getSimpleName();
    private List<Instruction> instructions;
    private DirectionListener listener;

    public static DirectionListFragment newInstance(List<Instruction> instructions,
            DirectionListener listener) {
        final DirectionListFragment fragment = new DirectionListFragment();
        fragment.instructions = instructions;
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_direction_list, container, false);
        final ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(new DirectionListAdapter(getActivity(), instructions));
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

        public DirectionListAdapter(Context context, List<Instruction> instructions) {
            this.context = context;
            this.instructions = instructions;
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
            final ImageView icon = (ImageView) view.findViewById(R.id.icon);
            final TextView simpleInstruction = (TextView)
                    view.findViewById(R.id.simple_instruction);
            final DistanceView distance = (DistanceView) view.findViewById(R.id.distance);

            if (position == 0) {
                icon.setImageResource(R.drawable.ic_locate_active);
                simpleInstruction.setText(context.getResources()
                        .getString(R.string.current_location));
            } else {
                final Instruction current = instructions.get(position - CURRENT_LOCATION_OFFSET);
                icon.setImageResource(DisplayHelper.getRouteDrawable(context,
                        current.getTurnInstruction()));
                simpleInstruction.setText(current.getSimpleInstruction());
                distance.setDistance(current.getDistance());
            }

            return view;
        }
    }
}