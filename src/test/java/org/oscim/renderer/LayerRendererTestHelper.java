package org.oscim.renderer;

import org.oscim.backend.GL20;

/**
 * Wrapper for LayerRenderer used to invoke protected and package-private members in tests.
 */
public final class LayerRendererTestHelper {
    private final LayerRenderer layerRenderer;

    public static void init(GL20 gl) {
        LayerRenderer.init(gl);
    }

    public LayerRendererTestHelper(LayerRenderer layerRenderer) {
        this.layerRenderer = layerRenderer;
    }

    public void update(GLViewport v) {
        layerRenderer.update(v);
    }

    public void render(GLViewport v) {
        layerRenderer.render(v);
    }

    public static class TestGLViewport extends GLViewport {
        public TestGLViewport() {
        }

        public TestGLViewport(int zoomLevel) {
            pos.setZoomLevel(zoomLevel);
        }
    }
}
