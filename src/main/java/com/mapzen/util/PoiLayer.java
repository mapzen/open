package com.mapzen.util;

import com.mapzen.R;

import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapElement;
import org.oscim.core.PointF;
import org.oscim.core.Tag;
import org.oscim.core.Tile;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.tile.MapTile;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.map.Map;
import org.oscim.renderer.elements.ElementLayers;
import org.oscim.tiling.source.mapfile.Projection;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;

public class PoiLayer extends ItemizedLayer<MarkerItem> {

    public PoiLayer(Map map, VectorTileLayer vectorTileLayer, final Context context) {
        super(map, new ArrayList<MarkerItem>(), AndroidGraphics.makeMarker(
                context.getResources().getDrawable(R.drawable.transparent),
                MarkerItem.HotspotPlace.CENTER), new OnItemGestureListener<MarkerItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, MarkerItem item) {
                Toast.makeText(context, item.getTitle(), Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onItemLongPress(int index, MarkerItem item) {
                return false;
            }
        });
        vectorTileLayer.addHook(new ProcessTileHook());
    }

    public class ProcessTileHook implements VectorTileLayer.TileLoaderProcessHook {
        @Override
        public boolean process(MapTile tile, ElementLayers layers, MapElement element) {
            Tag tag = element.tags.get("amenity");
            Tag tagName = element.tags.get("name");
            if (tag != null) {
                if (tag.value.matches("cafe|restaurant|pub|fast_food")) {
                    PointF point = element.getPoint(element.indexPos);
                    addItem(new MarkerItem(tagName.value, tag.value, new GeoPoint(
                            Projection.pixelYToLatitude(
                                    (tile.tileY * Tile.SIZE) + point.getY(),
                                    tile.zoomLevel),
                            Projection.pixelXToLongitude(
                                    (tile.tileX * Tile.SIZE) + point.getX(),
                                    tile.zoomLevel))));
                }
            }
            return false;
        }

    }
}

