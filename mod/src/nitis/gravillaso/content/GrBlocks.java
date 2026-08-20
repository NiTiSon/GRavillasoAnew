package nitis.gravillaso.content;

import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.distribution.StackConveyor;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.StaticWall;
import nitis.gravillaso.world.blocks.distribution.*;
import nitis.gravillaso.world.blocks.storage.FactoryCoreBlock;

import static mindustry.content.Items.*;
import static mindustry.content.Liquids.*;
import static nitis.gravillaso.content.GrItems.*;
import static nitis.gravillaso.content.GrLiquids.*;

import static mindustry.type.ItemStack.with;

public class GrBlocks{
    // environment
    public static Block corundum, corundumWall;
    // storage
    public static Block coreBase;
    // distribution
    public static Block cobaltConveyor, phaseConveyor;

    public static void load(){
        corundum = new Floor("corundum-floor", 3);

        corundumWall = new StaticWall("corundum-wall") {{
            corundum.asFloor().wall = this;
        }};

        coreBase = new FactoryCoreBlock("core-base"){{
            requirements(Category.effect, with(cobalt, 1000, lead, 800));
            isFirstTier = true;
            size = 4;

            unitType = UnitTypes.gamma;
            health = 3500;
            itemCapacity = 2000;
            thrusterLength = 34/4f;
            armor = 5f;

            alwaysUnlocked = true;
            incinerateNonBuildable = true;
            buildCostMultiplier = 0.7f;
            requiresCoreZone = true;

            unitCapModifier = 5;
        }};

        cobaltConveyor = new StackConveyor("cobalt-conveyor"){{
            requirements(Category.distribution, with(cobalt, 1));
            health = 120;
            speed = 2f / 60f;
            itemCapacity = 8;
        }};

        phaseConveyor = new MaglevConveyor("phase-conveyor"){{
            requirements(Category.distribution, with(tungsten, 2, silicon, 2, phaseFabric, 1));
            health = 250;
        }};
    }
}