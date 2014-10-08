package com.mapzen.open.support;

import com.mapzen.open.shadows.ShadowBugSenseHandler;
import com.mapzen.open.shadows.ShadowGLMatrix;
import com.mapzen.open.shadows.ShadowGLShader;
import com.mapzen.open.shadows.ShadowGLState;
import com.mapzen.open.shadows.ShadowMapView;
import com.mapzen.open.shadows.ShadowVectorTileLayer;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.bytecode.ClassInfo;
import org.robolectric.bytecode.Setup;
import org.robolectric.bytecode.ShadowMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Custom unit test runner. Enables custom shadows for testing third party library integrations.
 * <p/>
 * <strong>Adding a Custom Shadow</strong>
 * <ol>
 * <li>Create a new custom shadow class using instructions outlined here:
 * http://robolectric.blogspot.com/2011/01/how-to-create-your-own-shadow-classes.html</li>
 * <li>Map the shadow class to the original using
 * {@link org.robolectric.annotation.Implements}.</li>
 * <li>Customize behavior of the shadow using
 * {@link org.robolectric.annotation.Implementation}.</li>
 * <li>Add the original class name to the {@link #CUSTOM_SHADOW_TARGETS} list.</li>
 * <li>Bind the shadow class by calling
 * {@link ShadowMap.Builder#addShadowClass(Class)} in {@link #createShadowMap()}.</li>
 * <li>Be sure to use {@code @RunWith(MapzenTestRunner.class)} at the top of your tests.</li>
 * </ol>
 */
public class MapzenTestRunner extends RobolectricTestRunner {
    /**
     * List of fully qualified class names backed by custom shadows in the test harness.
     */
    private static final List<String> CUSTOM_SHADOW_TARGETS =
            Collections.unmodifiableList(Arrays.asList(
                    "org.oscim.android.MapView",
                    "org.oscim.layers.tile.vector.VectorTileLayer",
                    "org.oscim.renderer.GLMatrix",
                    "org.oscim.renderer.GLShader",
                    "org.oscim.renderer.GLState",
                    "com.bugsense.trace.BugSenseHandler"
            ));

    public MapzenTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    /**
     * Adds custom shadow classes to Robolectric shadow map.
     */
    @Override
    protected ShadowMap createShadowMap() {
        return super.createShadowMap()
                .newBuilder()
                .addShadowClass(ShadowMapView.class)
                .addShadowClass(ShadowVectorTileLayer.class)
                .addShadowClass(ShadowGLMatrix.class)
                .addShadowClass(ShadowGLShader.class)
                .addShadowClass(ShadowGLState.class)
                .addShadowClass(ShadowBugSenseHandler.class)
                .build();
    }

    /**
     * Replaces Robolectric {@link Setup} with {@link MapzenSetup} subclass.
     */
    @Override
    public Setup createSetup() {
        return new MapzenSetup();
    }

    /**
     * Modified Robolectric {@link Setup} that instruments third party classes with custom shadows.
     */
    public class MapzenSetup extends Setup {
        @Override
        public boolean shouldInstrument(ClassInfo classInfo) {
            return CUSTOM_SHADOW_TARGETS.contains(classInfo.getName())
                    || super.shouldInstrument(classInfo);
        }
    }
}
