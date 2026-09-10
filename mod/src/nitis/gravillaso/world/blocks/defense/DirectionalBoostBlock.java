package nitis.gravillaso.world.blocks.defense;

import mindustry.gen.*;
import mindustry.world.*;

import static mindustry.Vars.tilesize;

/** Basic interface for any block that carries boost.*/
public interface DirectionalBoostBlock{
    float boost();
    /** @return boost as a fraction of max boost */
    float boostFrac();

    static int contactPoints(Building a, Building b){
        float diff = Math.min(Math.abs(b.x - a.x), Math.abs(b.y - a.y)) / mindustry.Vars.tilesize;
        return Math.min((int)(a.block.size / 2f + b.block.size / 2f - diff), Math.min(a.block.size, b.block.size));
    }
}