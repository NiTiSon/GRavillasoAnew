package nitis.gravillaso.content;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.distribution.StackConveyor;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.StaticWall;
import static mindustry.content.Items.*;
import static mindustry.content.Liquids.*;
import static nitis.gravillaso.content.GrItems.*;
import static nitis.gravillaso.content.GrLiquids.*;

import static mindustry.type.ItemStack.with;

public class GrBlocks{
    // environment
    public static Block corundum, corundumWall;
    // distribution
    public static Block cobaltConveyor, phaseConveyor;

    public static void load(){
        corundum = new Floor("corundum-floor", 3);

        corundumWall = new StaticWall("corundum-wall") {{
            corundum.asFloor().wall = this;
        }};

        cobaltConveyor = new StackConveyor("cobalt-conveyor"){{
            requirements(Category.distribution, with(cobalt, 1));
            health = 120;
            speed = 2f / 60f;
            itemCapacity = 8;
        }};
    }
}