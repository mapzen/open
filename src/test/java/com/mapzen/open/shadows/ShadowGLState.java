package com.mapzen.open.shadows;

import org.oscim.renderer.GLState;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings("unused")
@Implements(GLState.class)
public final class ShadowGLState {
    private ShadowGLState() {
    }

    @Implementation
    public static void blend(boolean enable) {
    }

    @Implementation
    public static void enableVertexArrays(int va1, int va2) {
    }
}
