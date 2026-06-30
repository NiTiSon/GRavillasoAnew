package nitis.gravillaso.content;

import arc.graphics.Color;
import arc.util.Time;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.entities.bullet.LightningBulletType;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.environment.Prop;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Env;
import nitis.gravillaso.world.blocks.distribution.AccelerationConveyor;
import nitis.gravillaso.world.blocks.distribution.DispentorBlock;
import nitis.gravillaso.world.blocks.drone.CargoDepot;
import nitis.gravillaso.world.blocks.drone.CargoPort;
import nitis.gravillaso.world.blocks.drone.CargoTerminal;

import static mindustry.type.ItemStack.with;
import static mindustry.content.Items.*;
import static nitis.gravillaso.content.GRItems.*;

public class GRBlocks {
    // environment
    public static Block bauxite, bauxiteWall;
    // environment - boulders
    public static Block bauxiteBoulder;

    // ores
    public static Block oreCobalt;

    // storage - gravillo
    public static Block coreFortress, coreTier2, coreTier3;
    // turrets
    public static Block voltum;

    // distribution
    public static Block vectorConveyor, smartRouter, programmableRouter, dispentor;
    // distribution - cargo
    public static Block cargoDepot, cargoDepotLarge, cargoPort, cargoTerminal;

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

        bauxiteBoulder = new Prop("bauxite-boulder") {{ // TODO: rework texture
            variants = 2;
            bauxite.asFloor().decoration = this;
        }};

        oreCobalt = new OreBlock("ore-cobalt", cobalt) {{
            oreDefault = true;
            oreThreshold = 0.78f;
            oreScale = 23.47512f;
        }};

        coreFortress = new CoreBlock("core-fortress") {{
            requirements(Category.effect, with(cobalt, 1000, lead, 800));
            isFirstTier = true;
            size = 4;

            unitType = GRUnitTypes.releaseChan;
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

        coreTier2 = new CoreBlock("core-2") {{
            requirements(Category.effect, with(cobalt, 67, lead, 67));
            size = 5;

            unitType = GRUnitTypes.releaseChan;
            health = 9000;
            itemCapacity = 3000;
            thrusterLength = 40/4f;
            armor = 10f;

            incinerateNonBuildable = true;
            buildCostMultiplier = 0.7f;
            requiresCoreZone = true;

            unitCapModifier = 13;
            researchCostMultiplier = 0.1f;
        }};

        coreTier3 = new CoreBlock("core-3") {{
            requirements(Category.effect, with(cobalt, 67, lead, 67));
            size = 6;

            unitType = GRUnitTypes.releaseChan;
            health = 21000;
            itemCapacity = 4000;
            thrusterLength = 48/4f;
            armor = 10f;

            incinerateNonBuildable = true;
            buildCostMultiplier = 0.7f;
            requiresCoreZone = true;

            unitCapModifier = 23;
            researchCostMultiplier = 0.1f;
        }};

        voltum = new PowerTurret("voltum"){{
            requirements(Category.turret, ItemStack.with(cobalt, 120, aluminium, 100));
            size = 3;
            health = 1200;
            reload = 12;
            inaccuracy = 3.5f;
            range = 175;
            // shots = 3;
            shootType = new LightningBulletType(){{
                lightningColor = Color.valueOf("a4ded0");
                buildingDamageMultiplier = 0.15f;
                speed = 2;
                lifetime = Time.toSeconds / 1.5f;
                damage = 83;
                reloadMultiplier = 1.15f;
                shootEffect = Fx.lightningShoot;
            }};
            shootSound = Sounds.shootArc;
            heatColor = Color.red;
            consumePower(8.5f);
            targetAir = true;
            targetGround = true;
        }};

        // distribution
        vectorConveyor = new AccelerationConveyor("vector-conveyor") {{
            requirements(Category.distribution, with(cobalt, 2));
        }};

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

        cargoDepot = new CargoDepot("cargo-depot") {{
            requirements(Category.distribution, with(cobalt, 80, silicon, 120));
            size = 3;
            droneType = GRUnitTypes.phortotis;
            consumePower(1f);
            envEnabled |= Env.any;
        }};

        cargoDepotLarge = new CargoDepot("cargo-depot-large") {{
            requirements(Category.distribution, with(cobalt, 160, silicon, 300));
            size = 4;
            droneType = GRUnitTypes.skoros;
            consumePower(3f);
            envEnabled |= Env.any;
        }};

        cargoPort = new CargoPort("cargo-port") {{
            requirements(Category.distribution, with(cobalt, 160, silicon, 120));
            size = 3;

            itemCapacity = 350;
        }};

        cargoTerminal = new CargoTerminal("cargo-terminal") {{
            requirements(Category.distribution, with(cobalt, 160, silicon, 220));
            size = 3;

            itemCapacity = 500;
        }};
    }
}
