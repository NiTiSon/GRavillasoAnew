package nitis.gravillaso.graphics;

import arc.graphics.gl.Shader;

public class GRShaders {
    public static Shader ring;

    public static void init() {
        ring = new RingShader();
    }

    public static void dispose() {

    }
}
