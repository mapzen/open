package com.mapzen;

import android.content.Context;
import android.widget.Toast;

import com.mapzen.util.Logger;

import org.oscim.layers.marker.ItemizedIconLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.map.Map;

import java.util.ArrayList;

public class PoiLayer<Item extends MarkerItem> extends ItemizedIconLayer<Item> {
    public PoiLayer(final Context context, Map map, MarkerSymbol defaultMarker) {
        super(map, new ArrayList<Item>(), defaultMarker, new OnItemGestureListener<Item>() {
            @Override
            public boolean onItemSingleTapUp(int index, MarkerItem item) {
                Logger.d("onItemSingleTapUp: index=" + index + ", title=" + item.getTitle());
                Toast.makeText(context, item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onItemLongPress(int index, MarkerItem item) {
                // Not yet implemented.
                return false;
            }
        });
    }
}
