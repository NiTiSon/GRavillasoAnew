package nitis.gravillaso.world.blocks.power;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.core.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;

import static mindustry.Vars.*;

public class SquarePowerNode extends PowerNode{

    public SquarePowerNode(String name){
        super(name);
        laserColor2 = Color.valueOf("#b8ecff");
    }

    @Override
    protected boolean overlaps(float srcx, float srcy, Tile other, Block otherBlock, float range){
        float cx = other.worldx() + otherBlock.offset;
        float cy = other.worldy() + otherBlock.offset;
        float hs = otherBlock.size * tilesize / 2f;
        return srcx + range > cx - hs && srcx - range < cx + hs
            && srcy + range > cy - hs && srcy - range < cy + hs;
    }

    @Override
    protected boolean overlaps(float srcx, float srcy, Tile other, float range){
        Rect hitbox = other.getHitbox(Tmp.r1);
        float cx = hitbox.x + hitbox.width / 2f;
        float cy = hitbox.y + hitbox.height / 2f;
        float hw = hitbox.width / 2f;
        float hh = hitbox.height / 2f;
        return srcx + range > cx - hw && srcx - range < cx + hw
            && srcy + range > cy - hh && srcy - range < cy + hh;
    }

    @Override
    public boolean overlaps(@Nullable Tile src, @Nullable Tile other){
        if(src == null || other == null) return true;
        float range = laserRange * tilesize;
        float sx = src.worldx() + offset;
        float sy = src.worldy() + offset;
        float ox = other.worldx() + offset;
        float oy = other.worldy() + offset;
        float hs = size * tilesize / 2f;
        return sx + range > ox - hs && sx - range < ox + hs
            && sy + range > oy - hs && sy - range < oy + hs;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        Tile tile = world.tile(x, y);
        if(tile == null || !autolink) return;

        float hs = laserRange * tilesize;
        float cx = x * tilesize + offset;
        float cy = y * tilesize + offset;

        Lines.stroke(1f);
        Draw.color(Pal.placing);
        Lines.rect(cx - hs, cy - hs, hs * 2, hs * 2);

        getPotentialLinks(tile, player.team(), other -> {
            Draw.color(laserColor1, Renderer.laserOpacity * 0.5f);
            drawLaser(cx, cy, other.x, other.y, size, other.block.size);
            Drawf.square(other.x, other.y, other.block.size * tilesize / 2f + 2f, Pal.place);
        });

        Draw.reset();
    }

    public class SquarePowerNodeBuild extends PowerNodeBuild{
        @Override
        public void drawSelect(){
            if(!drawRange) return;
            Lines.stroke(1f);
            Draw.color(Pal.accent);
            float hs = laserRange * tilesize;
            Lines.rect(x - hs, y - hs, hs * 2, hs * 2);
            Draw.reset();
        }

        @Override
        public void drawConfigure(){
            Drawf.circles(x, y, tile.block().size * tilesize / 2f + 1f + Mathf.absin(Time.time, 4f, 1f));

            if(drawRange){
                float hs = laserRange * tilesize;
                Lines.stroke(1f);
                Draw.color(Pal.accent);
                Lines.rect(x - hs, y - hs, hs * 2, hs * 2);

                for(int tx = (int)(tile.x - laserRange - 2); tx <= tile.x + laserRange + 2; tx++){
                    for(int ty = (int)(tile.y - laserRange - 2); ty <= tile.y + laserRange + 2; ty++){
                        Building link = world.build(tx, ty);
                        if(link != this && linkValid(this, link, false)){
                            if(linked(link)){
                                Drawf.square(link.x, link.y, link.block.size * tilesize / 2f + 1f, Pal.place);
                            }
                        }
                    }
                }
            }else{
                power.links.each(i -> {
                    var link = world.build(i);
                    if(link != null && linkValid(this, link, false)){
                        Drawf.square(link.x, link.y, link.block.size * tilesize / 2f + 1f, Pal.place);
                    }
                });
            }
            Draw.reset();
        }
    }
}
