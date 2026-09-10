package nitis.gravillaso.world.draw;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.draw.*;

public class DrawOverdriveTop extends DrawBlock{
    public String suffix = "-top";
    public Color baseColor = Color.valueOf("feb380");
    // TODO: use?
    public Color boostColor = Color.valueOf("ffd59e");
    public TextureRegion region;

    public DrawOverdriveTop(){
    }

    @Override
    public void draw(Building build){
        if(!Lod.l2) return;

        float f = 1f - (Time.time / 100f) % 1f;

        Draw.color(baseColor);
        Draw.alpha(build.warmup() * Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f * Lod.alpha2);
        Draw.rect(region, build.x, build.y);
    }

    @Override
    public void load(Block block){
        region = Core.atlas.find(block.name + suffix);
    }
}
