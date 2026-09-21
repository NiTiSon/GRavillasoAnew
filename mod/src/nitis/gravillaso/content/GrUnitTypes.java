package nitis.gravillaso.content;

import arc.graphics.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.weapons.*;
import mindustry.world.meta.*;
import nitis.gravillaso.annotations.Annotations.*;
import nitis.gravillaso.gen.*;
import nitis.gravillaso.type.*;

import static mindustry.Vars.tilesize;

public class GrUnitTypes {
    public static @EntityDef({Unitc.class, Legsc.class}) UnitType offense;
    public static @EntityDef({Unitc.class}) UnitType tantalus;
    public static @EntityDef({Unitc.class}) UnitType draugDrone;

    public static void load(){
        offense = new GravilloUnitType("offense"){{
            constructor = OffenseUnit::new;
            speed = 0.75f;
            drag = 0.15f;
            hitSize = 8f;
            rotateSpeed = 3.5f;
            health = 750;
            armor = 4f;
            itemCapacity = 0;
            legStraightness = 0.3f;
            stepShake = 0f;
            stepSound = Sounds.walkerStepTiny;
            stepSoundVolume = 0.4f;

            legCount = 4;
            legLength = 8f;
            lockLegBase = true;
            legContinuousMove = true;
            legExtension = -2f;
            legBaseOffset = 3f;
            legMaxLength = 1.1f;
            legMinLength = 0.2f;
            legLengthScl = 0.96f;
            legForwardScl = 1.1f;
            legGroupSize = 2;
            rippleScale = 0.2f;

            legMoveSpace = 1f;
            allowLegStep = true;
            hovering = true;
            legPhysicsLayer = false;

            shadowElevation = 0.1f;
            groundLayer = Layer.legUnit - 1f;
            targetAir = true;
            //researchCostMultiplier = 0f;

            weapons.add(new GrWeapon("offense-weapon"){{
                layerOffset = 0.0001f;
                mirror = false;
                x = 0f;
                y = -2f;
                recoil = 1f;
                shootY = 4f;
                reload = 90f;
                rotate = true;
                rotateSpeed = 2.2f;
                cooldownTime = 30f;
                heatColor = Color.valueOf("f9350f");

                bullet = new BasicBulletType(4f, 40){{
                    sprite = "missile-large";
                    smokeEffect = Fx.shootBigSmoke;
                    shootEffect = Fx.shootBigColor;
                    width = 5f;
                    height = 7f;
                    lifetime = 40f;
                    hitSize = 4f;
                    hitColor = backColor = trailColor = Color.valueOf("feb380");
                    frontColor = Color.white;
                    trailWidth = 1.7f;
                    trailLength = 5;
                    despawnEffect = hitEffect = Fx.hitBulletColor;
                }};
            }});
        }};


        final float coreFleeRange = 400f;
        tantalus = new GravilloUnitType("tantalus"){{
            constructor = TantalusUnit::new;
            coreUnitDock = true;
            controller = u -> new BuilderAI(true, coreFleeRange);
            isEnemy = false;

            range = 60f;
            faceTarget = true;
            targetPriority = -2;
            lowAltitude = false;
            mineWalls = true;
            mineFloor = false;
            mineHardnessScaling = false;
            flying = true;
            mineSpeed = 10f;
            mineTier = 1;
            buildSpeed = 1.2f;
            drag = 0.12f;
            speed = 4.5f;
            rotateSpeed = 7f;
            accel = 0.09f;
            itemCapacity = 40;
            health = 300f;
            armor = 1f;
            hitSize = 8f;
            engineSize = 2.5f;
            payloadCapacity = 2f * 2f * tilesize * tilesize;
            pickupUnits = false;
            vulnerableWithPayloads = true;

            fogRadius = 0f;
            targetable = false;
            hittable = false;

            weapons.add(new RepairBeamWeapon(){{ // probably will be replaced with normal weapons
                widthSinMag = 0.11f;
                reload = 20f;
                x = 0f;
                y = 6.5f;
                rotate = false;
                shootY = 0f;
                beamWidth = 0.6f;
                repairSpeed = 1.5f;
                fractionRepairSpeed = 0.06f;
                aimDst = 0f;
                shootCone = 15f;
                mirror = false;

                targetUnits = false;
                targetBuildings = true;
                autoTarget = false;
                controllable = true;
                laserColor = Pal.accent;
                healColor = Pal.accent;

                bullet = new BulletType(){{
                    maxRange = 45f;
                }};
            }});
        }};

        draugDrone = new GravilloUnitType("draug-drone"){{
            constructor = DraugDroneUnit::new;
            controller = u -> new MinerAI();

            flying = true;
            itemCapacity = 80;
            drag = 0.06f;
            accel = 0.11f;
            speed = 1.3f;
            health = 90;
            range = 60f;
            engineSize = 1.2f;
            engineOffset = 5.5f;
            payloadCapacity = 0f;
            targetable = false;
            bounded = true;

            isEnemy = false;
            hidden = false;
            useUnitCap = false;
            logicControllable = false;
            playerControllable = false;
            controlSelectGlobal = false;
            allowedInPayloads = false;
            createWreck = false;
            envEnabled = Env.any;
            envDisabled = Env.none;

            mineTier = 1;
            mineSpeed = 3.75f;
            mineWalls = true;
            mineFloor = true;
            mineHardnessScaling = false;
        }};
    }
}
