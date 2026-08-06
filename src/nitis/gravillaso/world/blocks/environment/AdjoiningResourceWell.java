package nitis.gravillaso.world.blocks.environment;

import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;

// 2x2 well floor block, which resource collector is placed on.
public class AdjoiningResourceWell extends Floor{
    public Block parent = Blocks.air;

    public AdjoiningResourceWell(String name) {
        super(name);
        variants = 2;
    }
}
