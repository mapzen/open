package com.mapzen.shadows;

import org.oscim.renderer.GLShader;
import org.robolectric.annotation.Implements;

@SuppressWarnings("unused")
@Implements(GLShader.class)
public final class ShadowGLShader {
    private static String vertexSource;
    private static String fragmentSource;

    private ShadowGLShader() {
        // Prevents instantiation of utility class.
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        ShadowGLShader.vertexSource = vertexSource;
        ShadowGLShader.fragmentSource = fragmentSource;
        return 0;
    }

    public static String getVertexSource() {
        return vertexSource;
    }

    public static String getFragmentSource() {
        return fragmentSource;
    }
}
