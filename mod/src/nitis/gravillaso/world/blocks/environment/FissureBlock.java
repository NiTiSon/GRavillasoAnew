package nitis.gravillaso.world.blocks.environment;

import arc.math.geom.*;

import static mindustry.Vars.*;

public class FissureBlock extends ReservoirBlock{
    public static final Point2[] offsets = {
        new Point2(0, 0),
        new Point2(1, 0),
        new Point2(1, 1),
        new Point2(0, 1),
        new Point2(-1, 1),
        new Point2(-1, 0),
        new Point2(-1, -1),
        new Point2(0, -1),
        new Point2(1, -1),
    };

    static{
        for(var p : offsets){
            p.sub(1, 1);
        }
    }

    public FissureBlock(String name){
        super(name, offsets);
    }
}