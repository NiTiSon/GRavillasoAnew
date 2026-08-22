package nitis.gravillaso.content;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.StackConveyor;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import nitis.gravillaso.graphics.*;
import nitis.gravillaso.world.blocks.distribution.*;
import nitis.gravillaso.world.blocks.liquid.*;
import nitis.gravillaso.world.blocks.storage.FactoryCoreBlock;

import static mindustry.content.Items.*;
import static mindustry.content.Liquids.*;
import static nitis.gravillaso.content.GrItems.*;
import static nitis.gravillaso.content.GrLiquids.*;

import static mindustry.type.ItemStack.with;
import static mindustry.type.ItemStack.mult;

public class GrBlocks{
    // environment
    public static Block corundum, corundumWall;
    // boulders
    // ores
    // wall ores
    // crafting
    public static Block siliconFurnace, aluminiumFurnace;
    // sandbox
    // walls
    public static Block cobaltWall, cobaltWallLarge;
    // defense
    // transport
    public static Block cobaltConveyor, phaseConveyor;
    // liquid
    public static Block screenConduit, radiantConduit, screenLiquidRouter;
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
        // region environment
        corundum = new Floor("corundum-floor", 3);

        corundumWall = new StaticWall("corundum-wall") {{
            corundum.asFloor().wall = this;
        }};
        // endregion

        // region crafting
        siliconFurnace = new GenericCrafter("silicon-furnace"){{
            requirements(Category.crafting, with(cobalt, 120));
            craftEffect = Fx.none;
            outputItem = new ItemStack(silicon, 2);
            craftTime = 60f;
            size = 3;
            hasPower = true;
            hasLiquids = false;
            itemCapacity = 20;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawDefault());
            fogRadius = 3;
            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.12f;

            consumeItems(with(bauxite, 3));
            consumePower(3f);
        }};

        aluminiumFurnace = new GenericCrafter("aluminium-furnace"){{
            requirements(Category.crafting, with(cobalt, 120));
            craftEffect = Fx.none;
            outputItem = new ItemStack(aluminium, 2);
            craftTime = 60f;
            size = 3;
            hasPower = true;
            hasLiquids = false;
            itemCapacity = 20;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawDefault());
            fogRadius = 3;
            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.12f;

            consumeItems(with(bauxite, 3));
            consumeLiquid(brine, 1f);
            consumePower(7f);
        }};
        // endregion

        // region walls
        final int wallHealthMultiplier = 4;
        cobaltWall = new Wall("cobalt-wall"){{
            requirements(Category.defense, with(cobalt, 6));
            health = 120 * wallHealthMultiplier;
            armor = 2f;
            envDisabled |= Env.scorching;
        }};
        cobaltWallLarge = new Wall("cobalt-wall-large"){{
            requirements(Category.defense, mult(cobaltWall.requirements, 4));
            health = 120 * wallHealthMultiplier * 4;
            size = 2;
            envDisabled |= Env.scorching;
        }};
        // endregion walls

        // region transport
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
        // endregion

        // region liquid
        screenConduit = new ArmoredConduit("screen-conduit"){{
            requirements(Category.liquid, with(lead, 1));
            liquidCapacity = 60f;
            liquidPressure = 1.05f;
            health = 150;
            explosivenessScale = flammabilityScale = 16f / 60f; // holy magic anuke's numbers
        }};

        radiantConduit = new RadiantConduit("radiant-conduit"){{
            requirements(Category.liquid, with(tungsten, 2, aluminium, 1));
            liquidCapacity = 60f;
            liquidPressure = 1.05f;
            health = 300;
            explosivenessScale = flammabilityScale = 20f / 60f;
        }};

        screenLiquidRouter = new LiquidRouter("screen-liquid-router"){{
            requirements(Category.liquid, with(lead, 4));
            liquidCapacity = 120f;
            underBullets = true;
            solid = false;

            explosivenessScale = flammabilityScale = 20f / 120f;
        }};
        // endregion

        // region storage
        coreBase = new FactoryCoreBlock("core-base"){{
            requirements(Category.effect, with(cobalt, 1000, lead, 800));
            isFirstTier = true;
            size = 4;

            unitType = GrUnitTypes.tantalus;
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
            drawer = new DrawTurret("frost-resistant-");
            shootY = -1.5f;
            outlineColor = GrPal.outline;
            size = 2;
            reload = 40f;
            recoil = 2f;
            range = 150;
            shootCone = 3f;
            scaledHealth = 180;
            rotateSpeed = 2f;
            researchCostMultiplier = 0.05f;

            limitRange();
        }};

        voltum = new PowerTurret("voltum"){{
            requirements(Category.turret, with(cobalt, 100, lead, 80));

            shootType = new BasicBulletType(){{ // TODO: this is temporal shoot type
                shootEffect = new MultiEffect(Fx.shootTitan, new WaveEffect(){{
                    colorTo = Pal.surge;
                    sizeTo = 26f;
                    lifetime = 14f;
                    strokeFrom = 4f;
                }});
                smokeEffect = Fx.shootSmokeTitan;
                hitColor = Pal.surge;

                sprite = "large-orb";
                trailEffect = Fx.missileTrail;
                trailInterval = 3f;
                trailParam = 4f;
                pierceCap = 2;
                buildingDamageMultiplier = 0.5f;
                fragOnHit = false;
                speed = 5f;
                damage = 180f;
                lifetime = 80f;
                width = height = 16f;
                backColor = Pal.surge;
                frontColor = Color.white;
                shrinkX = shrinkY = 0f;
                trailColor = Pal.surge;
                trailLength = 12;
                trailWidth = 2.2f;
                despawnEffect = hitEffect = new ExplosionEffect(){{
                    waveColor = Pal.surge;
                    smokeColor = Color.gray;
                    sparkColor = Pal.surge;
                    waveStroke = 4f;
                    waveRad = 40f;
                }};

                despawnSound = Sounds.explosionAfflict;
                shootSound = Sounds.shootAfflict;

                fragBullet = intervalBullet = new BasicBulletType(3f, 35){{
                    width = 9f;
                    hitSize = 5f;
                    height = 15f;
                    pierceCap = 3;
                    lifetime = 28f;
                    pierceBuilding = true;
                    hitColor = backColor = trailColor = Pal.surge;
                    frontColor = Color.white;
                    trailWidth = 2.1f;
                    trailLength = 5;
                    hitEffect = despawnEffect = new WaveEffect(){{
                        colorFrom = colorTo = Pal.surge;
                        sizeTo = 4f;
                        strokeFrom = 4f;
                        lifetime = 10f;
                    }};
                    buildingDamageMultiplier = 0.3f;
                    homingPower = 0.1f;
                }};

                bulletInterval = 3f;
                intervalRandomSpread = 20f;
                intervalBullets = 2;
                intervalAngle = 180f;
                intervalSpread = 300f;

                fragBullets = 20;
                fragVelocityMin = 0.5f;
                fragVelocityMax = 1.2f;
                fragLifeMin = 0.5f;
            }};

            shake = 1f;
            ammoPerShot = 2;
            drawer = new DrawTurret("frost-resistant-");
            shootY = -2;
            outlineColor = GrPal.outline;
            size = 3;
            reload = 40f;
            recoil = 2f;
            range = 60;
            shootCone = 3f;
            scaledHealth = 180;
            rotateSpeed = 1.5f;
        }};
        // endregion
    }
}