package com.mapzen.open.scenes;

import com.mapzen.open.R;
import com.mapzen.open.transformers.NoAnimationTransformer;

import com.davidstemmer.screenplay.scene.StandardScene;

import javax.inject.Inject;

import flow.Layout;

@Layout(R.layout.welcome_scene)
public class WelcomeScene extends StandardScene {
    @Inject NoAnimationTransformer noAnimationTransformer;

    @Override public Transformer getTransformer() {
        return noAnimationTransformer;
    }
}
