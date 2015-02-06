package com.mapzen.open.util;

import com.mapzen.open.shadows.ShadowGLShader;
import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.oscim.backend.GL20;
import org.oscim.map.TestMap;
import org.oscim.renderer.LayerRendererTestHelper;

import static com.mapzen.open.util.RouteLocationIndicator.FRAGMENT_SHADER;
import static com.mapzen.open.util.RouteLocationIndicator.MAX_SCALE;
import static com.mapzen.open.util.RouteLocationIndicator.MIN_SCALE;
import static com.mapzen.open.util.RouteLocationIndicator.SCALE_FACTOR;
import static com.mapzen.open.util.RouteLocationIndicator.VERTEX_SHADER;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MapzenTestRunner.class)
public class RouteLocationIndicatorTest {
    private RouteLocationIndicator indicator;
    private LayerRendererTestHelper layerRendererTestHelper;
    private GL20 mockGL20;

    @Before
    public void setUp() throws Exception {
        indicator = new RouteLocationIndicator(new TestMap());
        mockGL20 = Mockito.mock(GL20.class);
        layerRendererTestHelper = new LayerRendererTestHelper(indicator.getRenderer());
        LayerRendererTestHelper.init(mockGL20);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(indicator).isNotNull();
    }

    @Test
    public void shouldCreateShader() throws Exception {
        layerRendererTestHelper.update(new LayerRendererTestHelper.TestGLViewport());
        assertThat(ShadowGLShader.getVertexSource()).isEqualTo(VERTEX_SHADER);
        assertThat(ShadowGLShader.getFragmentSource()).isEqualTo(FRAGMENT_SHADER);
    }

    @Test
    public void shouldSetScaleBasedOnMapZoomLevel() throws Exception {
        layerRendererTestHelper.render(new LayerRendererTestHelper.TestGLViewport(18));
        ArgumentCaptor<Float> values = ArgumentCaptor.forClass(float.class);
        verify(mockGL20, times(2)).glUniform1f(any(int.class), values.capture());
        assertThat(values.getAllValues().get(1)).isEqualTo(SCALE_FACTOR * 18);
    }

    @Test
    public void shouldHaveMaximumScale() throws Exception {
        layerRendererTestHelper.render(new LayerRendererTestHelper.TestGLViewport(21));
        ArgumentCaptor<Float> values = ArgumentCaptor.forClass(float.class);
        verify(mockGL20, times(2)).glUniform1f(any(int.class), values.capture());
        assertThat(values.getAllValues().get(1)).isEqualTo(MAX_SCALE);
    }

    @Test
    public void shouldHaveMinimumScale() throws Exception {
        layerRendererTestHelper.render(new LayerRendererTestHelper.TestGLViewport(9));
        ArgumentCaptor<Float> values = ArgumentCaptor.forClass(float.class);
        verify(mockGL20, times(2)).glUniform1f(any(int.class), values.capture());
        assertThat(values.getAllValues().get(1)).isEqualTo(MIN_SCALE);
    }
}
