package com.mapzen;

import org.oscim.layers.marker.ItemizedIconLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.map.Map;

import java.util.List;

public class PoiLayer<Item extends MarkerItem> extends ItemizedIconLayer<Item>
        implements ItemizedIconLayer.OnItemGestureListener<Item> {

    public PoiLayer(Map map, List<Item> list, MarkerSymbol defaultMarker,
                    OnItemGestureListener<Item> onItemGestureListener) {
        super(map, list, defaultMarker, null);
    }

    @Override
    public boolean onItemSingleTapUp(int index, MarkerItem item) {
        return false;
    }

    @Override
    public boolean onItemLongPress(int index, MarkerItem item) {
        return false;
    }
}
