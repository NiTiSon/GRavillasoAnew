package nitis.gravillaso.content;

import arc.util.Time;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.distribution.StackConveyor;
import mindustry.world.blocks.distribution.StackRouter;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.environment.Prop;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.BuildVisibility;
import nitis.gravillaso.world.blocks.distribution.DispentorBlock;
import nitis.gravillaso.world.blocks.environment.AdjoiningResourceWell;
import nitis.gravillaso.world.blocks.environment.MainResourceWell;
import nitis.gravillaso.world.blocks.storage.FactoryCoreBlock;

import static mindustry.content.Blocks.*;
import static mindustry.content.Items.*;
import static mindustry.type.ItemStack.with;
import static nitis.gravillaso.content.GRItems.*;
import static nitis.gravillaso.content.GRLiquids.*;

public class GRBlocks {
    // environment
    public static Block bauxite, bauxiteWall;
    // environment - wells
    public static Block
            stoneOilWell, stoneAdjoiningWell; // stone
    // environment - boulders
    public static Block bauxiteBoulder;

    // ores
    public static Block oreCobalt;

    //crafting - gravillo
    public static Block oxidePrecipitator;

    // storage - gravillo
    public static Block coreBase;
    // turrets
    public static Block voltum;

    // distribution
    public static Block cobaltConveyor, cobaltRouter, cobaltJunction, smartRouter, programmableRouter, dispentor;

    public static void load() {
        bauxite = new Floor("bauxite-floor") {{
            itemDrop = GRItems.bauxite;
            playerUnmineable = true;
            attributes.set(Attribute.water, 0.3f);
        }};

        bauxiteWall = new StaticWall("bauxite-wall") {{
            bauxite.asFloor().wall = this;
            attributes.set(Attribute.water, 0.3f);
        }};

        stoneOilWell = new MainResourceWell("stone-oil-well") {{
            parent = blendGroup = stone;
            resource = Liquids.oil;
        }};

        stoneAdjoiningWell = new AdjoiningResourceWell("stone-well") {{
            parent = blendGroup = stone;
        }};

        bauxiteBoulder = new Prop("bauxite-boulder") {{ // TODO: rework texture
            variants = 2;
            bauxite.asFloor().decoration = this;
        }};

        oreCobalt = new OreBlock("ore-cobalt", cobalt) {{
            oreDefault = true;
            oreThreshold = 0.78f;
            oreScale = 23.47512f;
        }};

        oxidePrecipitator = new GenericCrafter("oxide-precipitator") {{
            requirements(Category.crafting, with(cobalt, 150, silicon, 120));

            craftEffect = Fx.none;
            craftTime = (1 + 1 / 3f) * Time.toSeconds;
            size = 3;
            hasPower = true;
            hasLiquids = true;
            itemCapacity = 40;
            // drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt(), new DrawDefault());
            // fogRadius = 3;
            ambientSound = Sounds.loopExtract;
            ambientSoundVolume = 0.6f;

            consumeItems(with(titanium, 6));
            consumeLiquid(brine, 0.2f);
            consumePower(2.25f);
            outputItem = new ItemStack(oxide, 5);
        }};

        coreBase = new FactoryCoreBlock("core-base") {{
            requirements(Category.effect, with(cobalt, 1000, lead, 800));
            isFirstTier = true;
            size = 4;

            unitType = GRUnitTypes.test;
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

        // distribution
        cobaltConveyor = new StackConveyor("cobalt-conveyor") {{
            requirements(Category.distribution, with(cobalt, 1));
            health = 70;
            speed = 2.5f / 60f;
            itemCapacity = 8;
        }};

        cobaltRouter = new StackRouter("cobalt-router"){{
            requirements(Category.distribution, with(cobalt, 4));
            health = 100;

            speed = 25f;

            underBullets = true;
            solid = false;
        }};

        // either will be maglev conveyor or aluminium conveyor
        // new StackConveyor("aluminum-conveyor"){{
        //     requirements(Category.distribution, with(aluminium, 1, silicon, 1));
        //     health = 250;
        //     speed = 3f / 60f;
        //     itemCapacity = 30;
        // }};

        smartRouter = new Block("smart-router") {{
            requirements(Category.distribution, with(cobalt, 5, lead, 5));
            buildVisibility = BuildVisibility.hidden;
        }};

        programmableRouter = new Block("programmable-router") {{
            requirements(Category.distribution, with(cobalt, 20, silicon, 10));
            buildVisibility = BuildVisibility.hidden;
        }};
        dispentor = new DispentorBlock("dispentor") {{
            requirements(Category.distribution, with(cobalt, 100, lead, 120, silicon, 40));
            buildVisibility = BuildVisibility.hidden;
            size = 2;
        }};
    }
}
