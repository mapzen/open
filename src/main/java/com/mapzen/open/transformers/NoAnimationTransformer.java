package com.mapzen.open.transformers;

import com.davidstemmer.screenplay.SceneCut;
import com.davidstemmer.screenplay.flow.Screenplay;
import com.davidstemmer.screenplay.scene.Scene;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NoAnimationTransformer implements Scene.Transformer {
    @Inject public NoAnimationTransformer() {
    }

    @Override public void applyAnimations(SceneCut cut, Screenplay listener) {
        listener.endCut(cut);
    }
}
