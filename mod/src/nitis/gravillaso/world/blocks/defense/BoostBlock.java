package nitis.gravillaso.world.blocks.defense;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class BoostBlock extends Block{
    protected static @Nullable IntFloatMap accumulatedBoost;

    /** whenever true, target block will additively acquire speed boost, otherwise vanilla-like behaviour */
    public boolean additiveBoost = true;

    public BoostBlock(String name){
        super(name);
        update = solid = rotate = true;
        rotateDraw = false;
        drawArrow = true;

        group = BlockGroup.projectors;
        envEnabled |= Env.space;
        canOverdrive = false;
    }

    @Override
    public void init(){
        super.init();

        if(additiveBoost && accumulatedBoost == null){
            accumulatedBoost = new IntFloatMap(8);

            Events.run(Trigger.afterGameUpdate, BoostBlock::postUpdate);
            Events.run(WorldLoadEvent.class, BoostBlock::worldLoad);
        }
    }

    static void postUpdate(){
        for(IntFloatMap.Entry entry : accumulatedBoost){ // PERF[low]: maybe we can iterate without memory allocation?
            Building target = Vars.world.build(entry.key - 1);

            if(target == null) continue;

            target.applyBoost(1f + entry.value, 2f);
            accumulatedBoost.put(entry.key, 0f);
        }
    }

    static void worldLoad(){
        if (accumulatedBoost != null){
            accumulatedBoost.clear(64);
        }
    }

    <T extends Building & DirectionalBoostBlock> void tryBoostBuild(T self, Building build){
        int contact = DirectionalBoostBlock.contactPoints(self, build);

        if(contact > 0 && self.isValidBoosterReceiver(build)){
            float ratio = (float)contact / Math.min(build.block.size, self.block.size);
            float blockBoost = DirectionalBoostBlock.getBoost(self.boost(), ratio, additiveBoost);
            if(additiveBoost){
                int key = build.tile.pos() + 1;
                blockBoost += accumulatedBoost.get(key, 0f);
                accumulatedBoost.put(key, blockBoost);
            }else{
                build.applyBoost(blockBoost, 2F);
            }
        }
    }
}