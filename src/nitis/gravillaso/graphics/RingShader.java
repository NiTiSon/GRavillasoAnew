package nitis.gravillaso.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import mindustry.Vars;

public class RingShader extends Shader {
    public Vec3 lightDir = new Vec3(1, 1, 1).nor();
    public Color ambientColor = Color.white.cpy();
    public float alpha = 1f;
    public float innerRadius, outerRadius;

    public RingShader(){
        super(Vars.tree.get("shaders/ring.vert"), Vars.tree.get("shaders/ring.frag"));
    }

    @Override
    public void apply(){
        setUniformf("u_alpha", alpha);
        setUniformf("u_lightdir", lightDir);
        setUniformf("u_ambientColor", ambientColor.r, ambientColor.g, ambientColor.b);
        setUniformf("u_innerRadius", innerRadius);
        setUniformf("u_outerRadius", outerRadius);
    }
}
