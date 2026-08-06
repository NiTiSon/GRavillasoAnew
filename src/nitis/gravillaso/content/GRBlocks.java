package nitis.gravillaso.content;

import arc.graphics.Color;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.content.Liquids;
import mindustry.entities.bullet.LightningBulletType;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.environment.Prop;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Env;
import nitis.gravillaso.world.blocks.distribution.DispentorBlock;
import nitis.gravillaso.world.blocks.environment.AdjoiningResourceWell;
import nitis.gravillaso.world.blocks.environment.MainResourceWell;

import static mindustry.type.ItemStack.with;
import static mindustry.content.Items.*;
import static mindustry.content.Blocks.*;
import static nitis.gravillaso.content.GRItems.*;

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

    // storage - gravillo
    public static Block coreBase;
    // turrets
    public static Block voltum;

    // distribution
    public static Block smartRouter, programmableRouter, dispentor;

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

        coreBase = new CoreBlock("core-base") {{
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

        smartRouter = new Block("smart-router") {{
            requirements(Category.distribution, with(cobalt, 5, lead, 5));
        }};

        programmableRouter = new Block("programmable-router") {{
            requirements(Category.distribution, with(cobalt, 20, silicon, 10));
        }};
        dispentor = new DispentorBlock("dispentor") {{
            requirements(Category.distribution, with(cobalt, 100, lead, 120, silicon, 40));
            size = 2;
        }};
    }
}
