package nitis.gravillaso.graphics;

import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;

public class PlanetRingMesh implements GenericMesh {
    public Planet planet;
    public float innerRadius, outerRadius;
    public int segments = 80;
    public Color color = Color.white.cpy();
    public float alpha = 0.55f;
    public float tilt = 0f;

    private Mesh mesh;
    private RingShader shader;

    public PlanetRingMesh(Planet planet, float innerRadius, float outerRadius){
        this.planet = planet;
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        rebuild();
    }

    public PlanetRingMesh(Planet planet, float innerRadius, float outerRadius, Color color, float alpha){
        this.planet = planet;
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.color = color;
        this.alpha = alpha;
        rebuild();
    }

    public void rebuild(){
        int vertCount = (segments + 1) * 4;
        int idxCount = segments * 12;

        float[] verts = new float[vertCount * 7];
        short[] inds = new short[idxCount];

        float colBits = Color.toFloatBits(color.r, color.g, color.b, alpha);

        for(int i = 0; i <= segments; i++){
            float a = (float)i / segments * Mathf.PI2;
            float cos = Mathf.cos(a);
            float sin = Mathf.sin(a);

            float ox = cos * outerRadius, oz = sin * outerRadius;
            float ix = cos * innerRadius, iz = sin * innerRadius;

            int base = i * 4 * 7;

            // top outer
            verts[base] = ox; verts[base+1] = 0; verts[base+2] = oz;
            verts[base+3] = 0; verts[base+4] = 1; verts[base+5] = 0;
            verts[base+6] = colBits;

            // top inner
            verts[base+7] = ix; verts[base+8] = 0; verts[base+9] = iz;
            verts[base+10] = 0; verts[base+11] = 1; verts[base+12] = 0;
            verts[base+13] = colBits;

            // bottom inner
            verts[base+14] = ix; verts[base+15] = 0; verts[base+16] = iz;
            verts[base+17] = 0; verts[base+18] = -1; verts[base+19] = 0;
            verts[base+20] = colBits;

            // bottom outer
            verts[base+21] = ox; verts[base+22] = 0; verts[base+23] = oz;
            verts[base+24] = 0; verts[base+25] = -1; verts[base+26] = 0;
            verts[base+27] = colBits;
        }

        for(int i = 0; i < segments; i++){
            int to = i * 4, ti = to + 1, bo = ti + 1, bi = bo + 1;
            int no = (i + 1) * 4, ni = no + 1, nbo = ni + 1, nbi = nbo + 1;

            int idx = i * 12;
            inds[idx] = (short)to; inds[idx+1] = (short)no; inds[idx+2] = (short)ti;
            inds[idx+3] = (short)no; inds[idx+4] = (short)ni; inds[idx+5] = (short)ti;
            inds[idx+6] = (short)bi; inds[idx+7] = (short)nbi; inds[idx+8] = (short)bo;
            inds[idx+9] = (short)nbi; inds[idx+10] = (short)nbo; inds[idx+11] = (short)bo;
        }

        mesh = new Mesh(true, vertCount, idxCount,
            VertexAttribute.position3, VertexAttribute.normal, VertexAttribute.color);
        mesh.setVertices(verts);
        mesh.setIndices(inds);
    }

    @Override
    public void render(PlanetParams params, Mat3D projection, Mat3D transform){
        if(mesh == null || mesh.isDisposed()) return;

        if(shader == null){
            shader = new RingShader();
        }

        Gl.enable(Gl.blend);
        Gl.depthMask(false);
        Gl.disable(Gl.cullFace);

        shader.lightDir.set(planet.solarSystem.position).sub(planet.position).nor();
        shader.ambientColor.set(planet.solarSystem.lightColor);
        shader.alpha = 1f;
        shader.innerRadius = this.innerRadius;
        shader.outerRadius = this.outerRadius;

        shader.bind();
        shader.setUniformMatrix4("u_proj", projection.val);
        shader.setUniformMatrix4("u_trans", transform.val);
        shader.apply();
        mesh.render(shader, Gl.triangles);

        Gl.enable(Gl.cullFace);
        Gl.depthMask(true);
    }

    @Override
    public void dispose(){
        if(mesh != null) mesh.dispose();
        if(shader != null) shader.dispose();
    }
}
