package nitis.gravillaso.world.blocks.defense;

import mindustry.gen.*;

/** Basic interface for any block that carries boost.*/
public interface DirectionalBoostBlock extends Buildingc{
    float boost();
    /** @return boost as a fraction of max boost */
    float boostFrac();

    boolean isValidBoosterReceiver(Building receiver);

    static int contactPoints(Building a, Building b){
        float diff = Math.min(Math.abs(b.x - a.x), Math.abs(b.y - a.y)) / mindustry.Vars.tilesize;
        return Math.min((int)(a.block.size / 2f + b.block.size / 2f - diff), Math.min(a.block.size, b.block.size));
    }

    /** @return boost for provided {@code build}
     * @param addition is timescale stacking allowed */
    static float getBoost(float boost, float ratio, boolean addition){
        boost *= ratio;
        if(!addition){
            boost += 1f; // 1f = standalone 100%
        }

        return boost;
    }
}