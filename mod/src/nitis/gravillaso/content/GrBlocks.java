package nitis.gravillaso.content;

import arc.graphics.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import nitis.gravillaso.entities.bullet.*;
import nitis.gravillaso.graphics.*;
import nitis.gravillaso.world.blocks.distribution.*;
import nitis.gravillaso.world.blocks.environment.*;
import nitis.gravillaso.world.blocks.liquid.*;
import nitis.gravillaso.world.blocks.power.*;
import nitis.gravillaso.world.blocks.storage.*;
import nitis.gravillaso.world.reservoir.*;

import static mindustry.content.Items.*;
import static mindustry.type.ItemStack.*;
import static nitis.gravillaso.content.GrItems.*;
import static nitis.gravillaso.content.GrLiquids.*;

public class GrBlocks{
    // environment
    public static Block
    corundum, corundumWall,
    galena, galenaWall,
    cryogenFloor, cryogenWall,
    purpleStone, purpleStoneCrater, purpleStoneWall;
    // wells
    public static Floor corundumWell;
    // boulders
    public static Block corundumBoulder, corundumCluster, purpleStoneBoulder, cryogenBoulder;
    // ores
    // wall ores
    public static Block wallOreLead, wallOreCobalt;
    // crafting
    public static Block siliconFurnace, aluminiumFurnace;
    // sandbox
    // walls
    public static Block cobaltWall, cobaltWallLarge;
    // defense
    // transport
    public static Block cobaltConveyor, maglevConveyor;
    // liquid
    public static Block screenConduit, radiantConduit, screenLiquidRouter;
    // power
    public static Block powerSection;
    // production
    public static Block bauxiteCrusher;
    // wells building
    public static Block wellCollector, pressureBooster;
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
        corundum = new Floor("corundum-floor", 3){{
            attributes.set(Attribute.sand, 3f);
        }};

        corundumWall = new StaticWall("corundum-wall"){{
            corundum.asFloor().wall = this;
            attributes.set(Attribute.sand, 3f);
        }};

        galena = new Floor("galena", 4);

        galenaWall = new StaticWall("galena-wall"){{
            galena.asFloor().wall = this;
        }};

        cryogenFloor = new Floor("cryogen-floor", 3);

        cryogenWall = new StaticWall("cryogen-wall"){{
            cryogenFloor.asFloor().wall = this;
        }};

        purpleStone = new Floor("purple-stone", 4){{
            attributes.set(Attribute.sand, 1.25f);
        }};

        purpleStoneCrater = new Floor("purple-stone-crater", 3){{
            attributes.set(Attribute.sand, 1.25f);
            blendGroup = purpleStone;
        }};

        purpleStoneWall = new StaticWall("purple-stone-wall"){{
            purpleStone.asFloor().wall = purpleStoneCrater.asFloor().wall = this;
            attributes.set(Attribute.sand, 1.25f);
        }};
        // endregion

        // region well well well
        corundumWell = new WellBlock("corundum-well"){{
            parent = blendGroup = corundum;
        }};
        // endregion

        // region boulders
        corundumBoulder = new Prop("corundum-boulder"){{
            variants = 3;
            customShadow = true;
            corundum.asFloor().decoration = this;
            obstructsLight = false;
        }};
        corundumCluster = new TallBlock("corundum-cluster"){{
            variants = 3;
            clipSize = 128f;
        }};

        purpleStoneBoulder = new Prop("purple-stone-boulder"){{
            variants = 2;
            customShadow = true;
            purpleStone.asFloor().decoration = this;
            obstructsLight = false;
        }};

        cryogenBoulder = new Prop("cryogen-boulder"){{
            variants = 2;
            cryogenFloor.asFloor().decoration = this;
            obstructsLight = false;
        }};
        // endregion

        // region ores
        wallOreLead = new OreBlock("ore-wall-lead", lead){{
            wallOre = true;
        }};

        wallOreCobalt = new OreBlock("ore-wall-cobalt", cobalt){{
            wallOre = true;
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
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt(), new DrawDefault());
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

        maglevConveyor = new MaglevConveyor("maglev-conveyor"){{
            requirements(Category.distribution, with(tungsten, 2, silicon, 2, phaseFabric, 1));
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

        // region power
        powerSection = new SquarePowerNode("power-section"){{
            requirements(Category.power, with(cobalt, 5, lead, 20));
            consumesPower = outputsPower = true;
            size = 2;
            health = 250;
            fogRadius = 3;
            laserRange = 6;
            maxNodes = 3;

            consumePowerBuffered(5000f);
        }};
        // endregion

        // region production
        bauxiteCrusher = new WallCrafter("bauxite-crusher"){{
            requirements(Category.production, with(cobalt, 30, lead, 25));
            consumePower(24 / Time.toSeconds);

            drillTime = 120f;
            size = 2;
            attribute = Attribute.sand;
            output = bauxite;
            fogRadius = 2;
            researchCost = with(cobalt, 100, lead, 80);
            ambientSound = Sounds.loopDrill;
            ambientSoundVolume = 0.04f;
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

        /*testTurret = new PayloadAmmoTurret("test-turret"){{
            requirements(Category.turret, with(cobalt, 1));
            buildVisibility = BuildVisibility.hidden; // curse stuff
            range = 45.5f * Vars.tilesize;
            size = 3;

            ammo(
            Blocks.router, new PayloadBulletType(Blocks.router, 20f),
            GrBlocks.cobaltWallLarge, new PayloadBulletType(GrBlocks.cobaltWallLarge, 10f)
            );

            limitRange();
        }};*/
        // endregion
    }
}