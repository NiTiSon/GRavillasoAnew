package nitis.gravillaso.world.draw;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.draw.*;

import static mindustry.Vars.tilesize;

public class DrawOverdriveProjectorPulse extends DrawBlock{
    public Color color = Color.white;

    public DrawOverdriveProjectorPulse(){
    }

    public DrawOverdriveProjectorPulse(Color color){
        this.color = color;
    }

    @Override
    public void draw(Building build){
        if(!Lod.l2) return;

        float f = 1f - (Time.time / 100f) % 1f;

        float heat = build.warmup();
        float size = build.block.size;
        float x = build.x, y = build.y;

        Draw.color(color);
        Draw.alpha(Lod.alpha2);
        Lines.stroke((1.5f * f + 0.1f) * heat);

        float r = Math.max(0f, Mathf.clamp(2f - f * 2f) * size * tilesize / 2f - f - 0.2f), w = Mathf.clamp(0.5f - f) * size * tilesize;
        Lines.beginLine();
        for(int i = 0; i < 4; i++){
            Lines.linePoint(x + Geometry.d4(i).x * r + Geometry.d4(i).y * w, y + Geometry.d4(i).y * r - Geometry.d4(i).x * w);
            if(f < 0.5f) Lines.linePoint(x + Geometry.d4(i).x * r - Geometry.d4(i).y * w, y + Geometry.d4(i).y * r + Geometry.d4(i).x * w);
        }
        Lines.endLine(true);

        Draw.reset();
    }
}
