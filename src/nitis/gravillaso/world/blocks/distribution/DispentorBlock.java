package nitis.gravillaso.world.blocks.distribution;

import mindustry.world.Block;
import mindustry.world.Build;

// the misspelling is deliberately
public class DispentorBlock extends Block {
    // range within this block works
    public float range = 32f;

    public DispentorBlock(String name) {
        super(name);
    }

    public class DispentorBuild extends Build {

    }
}
