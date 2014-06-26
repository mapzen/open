package com.mapzen.shadows;

import org.oscim.layers.tile.TileLoader;
import org.oscim.layers.tile.TileManager;
import org.oscim.layers.tile.VectorTileRenderer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.map.Map;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.fest.reflect.core.Reflection.field;

/**
 * Shadow of {@link VectorTileLayer} that prevents spawning of {@code TileLoader} threads during
 * testing.
 */
@SuppressWarnings("unused")
@Implements(VectorTileLayer.class)
public class ShadowVectorTileLayer {
    @RealObject
    private VectorTileLayer realVectorTileLayer;

    public void __constructor__(Map map, TileManager tileManager,
            VectorTileRenderer renderer, int numLoaders) {
        field("mTileLoader").ofType(TileLoader[].class).in(realVectorTileLayer)
                .set(new TileLoader[0]);
        field("mLoaderThemeHooks").ofType(VectorTileLayer.TileLoaderThemeHook[].class)
                .in(realVectorTileLayer).set(new VectorTileLayer.TileLoaderThemeHook[0]);
        field("mLoaderProcessHooks").ofType(VectorTileLayer.TileLoaderProcessHook[].class)
                .in(realVectorTileLayer).set(new VectorTileLayer.TileLoaderProcessHook[0]);
    }
}