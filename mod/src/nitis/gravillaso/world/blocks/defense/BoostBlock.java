package nitis.gravillaso.world.blocks.defense;

import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class BoostBlock extends Block{
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
}