package nitis.gravillaso.content;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.graphics.*;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.StackConveyor;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import nitis.gravillaso.graphics.*;
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
    // boulders
    // ores
    // wall ores
    // crafting
    // sandbox
    // walls
    public static Block cobaltWall, cobaltWallLarge;
    // defense
    // transport
    public static Block cobaltConveyor, phaseConveyor;
    // liquid
    // power
    // production
    // storage
    public static Block coreBase;
    // turrets
    public static Block sight, destiny, voltum, lighter, finale;
    // units
    // payloads
    // logic
    // campaign

    public static void load(){
        corundum = new Floor("corundum-floor", 3);

        corundumWall = new StaticWall("corundum-wall") {{
            corundum.asFloor().wall = this;
        }};

        cobaltWall = new Wall("cobalt-wall"){{

        }};
        cobaltWallLarge = new Wall("cobalt-wall-large"){{

        }};



        cobaltConveyor = new StackConveyor("cobalt-conveyor"){{
            requirements(Category.distribution, with(cobalt, 1));
            health = 120;

            recharge = 3f;
            speed = 2f / 60f;
            itemCapacity = 8;
        }};

        phaseConveyor = new MaglevConveyor("phase-conveyor"){{
            requirements(Category.distribution, with(tungsten, 2, silicon, 2, phaseFabric, 1));
            buildVisibility = BuildVisibility.hidden; // To buggy now
            health = 250;

            recharge = 1f;
            speed = 6f / 60f;
            itemCapacity = 10;
        }};

        // region storage
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
        // endregion

        // region turrets
        sight = new ItemTurret("sight"){{
            requirements(Category.turret, with(cobalt, 100, lead, 80));

            Effect sfe = new MultiEffect(Fx.shootSmallColor, Fx.colorSpark);

            ammo(
            cobalt, new BasicBulletType(7.5f, 85){{
                width = 12f;
                hitSize = 7f;
                height = 20f;
                shootEffect = sfe;
                smokeEffect = Fx.shootBigSmoke;
                ammoMultiplier = 1;
                pierceCap = 2;
                pierce = true;
                pierceBuilding = true;
                hitColor = backColor = trailColor = GrPal.cobaltShot;
                frontColor = Color.white;
                trailWidth = 2.1f;
                trailLength = 10;
                hitEffect = despawnEffect = Fx.hitBulletColor;
                buildingDamageMultiplier = 0.3f;
            }}
            );

            shake = 1f;
            ammoPerShot = 2;
            drawer = new DrawTurret("reinforced-");
            shootY = -2;
            outlineColor = GrPal.outline;
            size = 2;
            envEnabled |= Env.space;
            reload = 40f;
            recoil = 2f;
            range = 190;
            shootCone = 3f;
            scaledHealth = 180;
            rotateSpeed = 1.5f;
            researchCostMultiplier = 0.05f;

            limitRange();
        }};
        // endregion
    }
}