package com.mapzen.open.transformers;

import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;

import com.davidstemmer.screenplay.scene.transformer.TweenTransformer;

import javax.inject.Inject;

/**
 * Created by weefbellington on 10/28/14.
 */
public class CrossfadeTransformer extends TweenTransformer {
    private static final Params params = new Params();

    static {
        params.forwardIn    = R.anim.fade_in;
        params.backIn       = R.anim.fade_in;
        params.backOut      = R.anim.fade_out;
        params.forwardOut   = R.anim.fade_out;
    }

    @Inject
    public CrossfadeTransformer(MapzenApplication context) {
        super(context, params);
    }
}
