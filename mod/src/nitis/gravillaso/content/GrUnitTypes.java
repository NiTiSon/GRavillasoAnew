package nitis.gravillaso.content;

import mindustry.ai.types.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.weapons.*;
import mindustry.world.meta.*;
import nitis.gravillaso.type.*;

import static mindustry.Vars.tilesize;

public class GrUnitTypes {
    public static UnitType tantalus;
    public static UnitType draugDrone;

    public static void load(){
        final float coreFleeRange = 400f;

        tantalus = new GravillasoUnitType("tantalus"){{
            coreUnitDock = true;
            controller = u -> new BuilderAI(true, coreFleeRange);
            isEnemy = false;

            range = 60f;
            faceTarget = true;
            targetPriority = -2;
            lowAltitude = false;
            mineWalls = true;
            mineFloor = true;
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

        draugDrone = new GravillasoUnitType("draug-drone"){{
            controller = u -> new MinerAI();

            flying = true;
            drag = 0.06f;
            accel = 0.11f;
            speed = 1.3f;
            health = 90;
            range = 60f;
            engineSize = 1.2f;
            engineOffset = 5.5f;
            payloadCapacity = 0f;
            targetable = false;
            bounded = false;

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
        }};
    }
}
