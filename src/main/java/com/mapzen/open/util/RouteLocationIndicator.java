package com.mapzen.open.util;

import org.oscim.backend.GL20;
import org.oscim.core.Box;
import org.oscim.core.Point;
import org.oscim.core.Tile;
import org.oscim.layers.Layer;
import org.oscim.map.Map;
import org.oscim.renderer.GLShader;
import org.oscim.renderer.GLState;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.LayerRenderer;
import org.oscim.utils.FastMath;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static org.oscim.core.MercatorProjection.latitudeToY;
import static org.oscim.core.MercatorProjection.longitudeToX;

public class RouteLocationIndicator extends Layer {
    private final Point location = new Point();
    private float degrees = 0;
    private int visible;
    private LayerRenderer renderer;

    public static final int MAX_SCALE = 80;
    public static final int MIN_SCALE = 40;
    public static final float SCALE_FACTOR = 4.0f;

    public static final String VERTEX_SHADER = ""
            + "precision mediump float;"
            + "uniform float u_degree;"
            + "uniform float u_scale;"
            + "uniform mat4 u_mvp;"
            + "attribute vec2 a_pos;"
            + "void main() {"
            + "  float deg = u_degree*3.141592653589793/180.0;"
            + "  vec2 angle = vec2(sin(deg), cos(deg));"
            + "  gl_Position = u_mvp * vec4("
            + "    vec2("
            + "      ( a_pos.x * angle.y + a_pos.y * angle.x ), "
            + "      ( a_pos.y * angle.y - a_pos.x * angle.x )) "
            + "    * u_scale, 0.0, 1.0);"
            + "}";

    public static final String FRAGMENT_SHADER = ""
            + "precision mediump float;"
            + "void main() {"
            + "  gl_FragColor = vec4(0.828125, 0.390625, 0.359375, 1.0);"
            + "}";

    public RouteLocationIndicator(Map map) {
        super(map);
    }

    @Override
    public LayerRenderer getRenderer() {
        if (renderer == null) {
            renderer = new LocationIndicator();
        }
        return renderer;
    }

    public void setPosition(double latitude, double longitude) {
        location.x = longitudeToX(longitude);
        location.y = latitudeToY(latitude);
    }

    public void setRotation(float degrees) {
        float offsetDegrees = degrees + 90.0f;
        if (offsetDegrees > 360) {
            offsetDegrees = offsetDegrees - 360;
        }
        this.degrees = offsetDegrees;
    }

    public class LocationIndicator extends LayerRenderer {
        private int shader;
        private int vertexPosition;
        private int matrixPosition;
        private int rotation;
        private int scale;
        private final Point indicatorPosition = new Point();
        private final Point screenPoint = new Point();
        private final Box bBox = new Box();
        private boolean initialized;

        public LocationIndicator() {
            super();
        }

        @Override
        public void update(GLViewport v) {
            if (!initialized) {
                shader = GLShader.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
                vertexPosition = GL.glGetAttribLocation(shader, "a_pos");
                matrixPosition = GL.glGetUniformLocation(shader, "u_mvp");
                rotation = GL.glGetUniformLocation(shader, "u_degree");
                scale = GL.glGetUniformLocation(shader, "u_scale");
                initialized = true;
            }

            if (!isEnabled()) {
                setReady(false);
                return;
            }

            setReady(true);

            int width = mMap.getWidth();
            int height = mMap.getHeight();

            v.getBBox(bBox, 0);

            double x = location.x;
            double y = location.y;

            if (!bBox.contains(location)) {
                x = FastMath.clamp(x, bBox.xmin, bBox.xmax);
                y = FastMath.clamp(y, bBox.ymin, bBox.ymax);
            }

            v.toScreenPoint(x, y, screenPoint);

            x = screenPoint.x + width / 2;
            y = screenPoint.y + height / 2;

            visible = 0;

            if (x > width - 5) {
                x = width;
            } else if (x < 5) {
                x = 0;
            } else {
                visible++;
            }

            if (y > height - 5) {
                y = height;
            } else if (y < 5) {
                y = 0;
            } else {
                visible++;
            }

            v.fromScreenPoint(x, y, indicatorPosition);
        }

        @Override
        public void render(GLViewport v) {
            GLState.useProgram(shader);
            GLState.blend(true);
            GLState.test(false, false);

            GL.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);

            FloatBuffer mVertices;
            final float[] mVerticesData = {
                    -0.4f, 0.0f, 0.0f,
                    -0.9f, 0.7f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    -0.9f, -0.7f, 0.0f,
            };

            mVertices = ByteBuffer.allocateDirect(mVerticesData.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();

            mVertices.clear();
            mVertices.put(mVerticesData);
            mVertices.flip();

            GL.glVertexAttribPointer(vertexPosition, 3, GL20.GL_FLOAT, false, 0, mVertices);

            GLState.enableVertexArrays(vertexPosition, -1);
            GL.glUniform1f(rotation, degrees);

            float scaleValue = SCALE_FACTOR * v.pos.getZoomLevel();
            if (scaleValue > MAX_SCALE) {
                scaleValue = MAX_SCALE;
            } else if (scaleValue < MIN_SCALE) {
                scaleValue = MIN_SCALE;
            }

            GL.glUniform1f(scale, scaleValue);

            double x = indicatorPosition.x - v.pos.x;
            double y = indicatorPosition.y - v.pos.y;
            double tileScale = Tile.SIZE * v.pos.scale;

            v.mvp.setTransScale((float) (x * tileScale), (float) (y * tileScale), 1);
            v.mvp.multiplyMM(v.viewproj, v.mvp);
            v.mvp.setAsUniform(matrixPosition);

            if (visible > 1) {
                GL.glDrawArrays(GL20.GL_TRIANGLE_FAN, 0, 4);
            }
        }
    }
}
