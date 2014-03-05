package com.mapzen.support;

import com.mapzen.shadows.ShadowCrashlytics;
import com.mapzen.shadows.ShadowGLMatrix;
import com.mapzen.shadows.ShadowLocationClient;
import com.mapzen.shadows.ShadowMapView;
import com.mapzen.shadows.ShadowMapzenLocationManager;
import com.mapzen.shadows.ShadowTextToSpeech;
import com.mapzen.shadows.ShadowVolley;

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
 * <li>Be sure to use {@code @RunWith(CustomTestRunner.class)} at the top of your tests.</li>
 * </ol>
 */
public class MapzenTestRunner extends RobolectricTestRunner {

    /**
     * List of fully qualified class names backed by custom shadows in the test harness.
     */
    private static final List<String> CUSTOM_SHADOW_TARGETS =
            Collections.unmodifiableList(Arrays.asList(
                    "com.crashlytics.android.Crashlytics",
                    "org.oscim.android.MapView",
                    "org.oscim.renderer.GLMatrix",
                    "com.android.volley.toolbox.Volley"
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
                .addShadowClass(ShadowCrashlytics.class)
                .addShadowClass(ShadowMapView.class)
                .addShadowClass(ShadowGLMatrix.class)
                .addShadowClass(ShadowVolley.class)
                .addShadowClass(ShadowLocationClient.class)
                .addShadowClass(ShadowTextToSpeech.class)
                .addShadowClass(ShadowMapzenLocationManager.class)
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
