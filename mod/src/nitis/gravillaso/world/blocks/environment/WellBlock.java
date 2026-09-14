package nitis.gravillaso.world.blocks.environment;

import arc.math.geom.*;

import static mindustry.Vars.*;

public class WellBlock extends ReservoirBlock{
    public static final Point2[] offsets = {
        new Point2(-1, -1),
        new Point2(0, -1),
        new Point2(-1, 0),
        new Point2(0, 0),
    };

    public WellBlock(String name){
        super(name, offsets);
        spriteSpacing = tilesize / 2f;
    }
}