package nitis.gravillaso.content;

import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.StaticWall;

public class GrBlocks{
    // environment
    public static Block corundum, corundumWall;

    public static void load(){
        corundum = new Floor("corundum-floor", 3);

        corundumWall = new StaticWall("corundum-wall") {{
            corundum.asFloor().wall = this;
        }};
    }
}