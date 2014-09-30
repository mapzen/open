package com.mapzen.util;

import com.mapzen.shadows.ShadowGLShader;
import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.oscim.backend.GL20;
import org.oscim.map.TestMap;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.LayerRendererTestHelper;
import org.robolectric.annotation.Config;

import static com.mapzen.util.RouteLocationIndicator.FRAGMENT_SHADER;
import static com.mapzen.util.RouteLocationIndicator.VERTEX_SHADER;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class RouteLocationIndicatorTest {
    private RouteLocationIndicator indicator;

    @Before
    public void setUp() throws Exception {
        indicator = new RouteLocationIndicator(new TestMap());
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(indicator).isNotNull();
    }

    @Test
    public void shouldCreateShader() throws Exception {
        LayerRendererTestHelper.init(Mockito.mock(GL20.class));
        ((RouteLocationIndicator.LocationIndicator) indicator.getRenderer())
                .update(Mockito.mock(GLViewport.class));
        assertThat(ShadowGLShader.getVertexSource()).isEqualTo(VERTEX_SHADER);
        assertThat(ShadowGLShader.getFragmentSource()).isEqualTo(FRAGMENT_SHADER);
    }
}
