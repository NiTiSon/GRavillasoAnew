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

public class DrawDirectionalPulse extends DrawBlock{
    public Color color = Color.white;

    public DrawDirectionalPulse(){
    }

    public DrawDirectionalPulse(Color color){
        this.color = color;
    }

    @Override
    public void draw(Building build){
        if(!Lod.l2) return;

        float f = (Time.time / 100f) % 1f;
        float nearness = 1 - 2 * Math.abs(f - 0.5f);

        float heat = build.warmup();
        float half = build.block.size * tilesize / 2f;

        float pos = Mathf.lerp(-half, half, f);

        // travel = facing direction, line perpendicular to it
        var dir = Geometry.d4(build.rotation);
        float dx = dir.x, dy = dir.y;
        float px = -dy, py = dx;

        Draw.color(color);
        Draw.alpha(Lod.alpha2);
        float thickness;
        Lines.stroke(thickness = (1.5f * nearness + 0.1f) * heat);
        thickness /= 2;

        Lines.line(
        build.x + dx * pos + px * (-half + thickness),
        build.y + dy * pos + py * (-half + thickness),
        build.x + dx * pos + px * (half - thickness),
        build.y + dy * pos + py * (half - thickness)
        );

        Draw.reset();
    }
}