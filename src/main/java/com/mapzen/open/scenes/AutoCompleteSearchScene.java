package com.mapzen.open.scenes;

import com.mapzen.open.R;
import com.mapzen.open.transformers.CrossfadeTransformer;

import com.davidstemmer.screenplay.scene.StandardScene;

import javax.inject.Inject;

import flow.Layout;

@Layout(R.layout.auto_complete_search)
public class AutoCompleteSearchScene extends StandardScene {

    @Inject CrossfadeTransformer crossfadeTransformer;

    @Override public Transformer getTransformer() {
        return crossfadeTransformer;
    }
}
