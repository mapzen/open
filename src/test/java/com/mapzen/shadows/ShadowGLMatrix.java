package com.mapzen.shadows;

import org.oscim.renderer.GLMatrix;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow implementation for {@link org.oscim.renderer.GLMatrix}.
 */
@SuppressWarnings("unused")
@Implements(GLMatrix.class)
public class ShadowGLMatrix {
    public void __constructor__() {
    }

    @Implementation
    public void setAsUniform(int location) {
    }
}
