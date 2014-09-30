package org.oscim.renderer;

import org.oscim.backend.GL20;

public final class LayerRendererTestHelper {
    private LayerRendererTestHelper() {
        // Prevents instantiation of utility class.
    }

    public static void init(GL20 gl20) {
        LayerRenderer.init(gl20);
    }
}
