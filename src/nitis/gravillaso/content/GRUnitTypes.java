package nitis.gravillaso.content;

import mindustry.content.UnitTypes;
import mindustry.gen.UnitEntity;
import mindustry.type.UnitType;
import nitis.gravillaso.ai.CargoDroneAI;
import nitis.gravillaso.type.CargoUnitType;

public class GRUnitTypes {
    // cargo units
    public static UnitType phortotis, skoros;

    // core units
    public static UnitType releaseChan;

    public static void load() {
        phortotis = new CargoUnitType("phortotis") {{
            flying = true;
            health = 120f;
            speed = 2.5f;
            accel = 0.08f;
            drag = 0.04f;
            rotateSpeed = 5f;
            engineOffset = 5f;
            engineSize = 2.5f;
            hitSize = 8f;
            buildSpeed = 0f;
            mineSpeed = 0f;
            mineTier = 0;
            playerControllable = false;
            createWreck = false;
            itemCapacity = 50;
            lowAltitude = true;
            aiController = CargoDroneAI::new;
            constructor = UnitEntity::create;
            powerCapacity = 2400f;
        }};

        skoros = new CargoUnitType("skoros") {{
            flying = true;
            health = 240f;
            speed = 3.0f;
            accel = 0.06f;
            drag = 0.04f;
            rotateSpeed = 4f;
            engineOffset = 7f;
            engineSize = 3.5f;
            hitSize = 12f;
            buildSpeed = 0f;
            mineSpeed = 0f;
            mineTier = 0;
            playerControllable = false;
            createWreck = false;
            itemCapacity = 120;
            lowAltitude = true;
            aiController = CargoDroneAI::new;
            constructor = UnitEntity::create;
            powerCapacity = 4200f;
            powerUsePerTick = 0.7f;
            chargePerTick = 6;
        }};

        releaseChan = new UnitType("release") {{
            constructor = UnitTypes.gamma.constructor;
            health = 320;
            flying = true;
            rotateSpeed = 8.6f;

            buildSpeed = 1.35f;

            mineRange *= 1.5f;
            mineSpeed = 9.0f;

            itemCapacity = 40;

            hitSize = 11.5f;

            fallSpeed = 0.025f;
            engineOffset = 9;
            engineSize = 3.2f;

            speed = 4.5f;
            drag = 0.1f;
            range = 30;
            accel = 0.35f;
//            weapons.add(new GRWeapon("south") {{
//                rotate = true;
//                rotateSpeed = 60f;
//                x = 6.9f;
//                y = -4.5f;
//                inaccuracy = 2.5f;
//                reload = 20;
//                shots = 1;
//                mirror = true;
//                shootSound = Sounds.shoot;
//                bullet = new MissileBulletType() {{
//                    lifetime = 45;
//                    speed = 3.5f;
//                    damage = 27;
//                    width = 6;
//                    height = 11.7f;
//                    hitSound = Sounds.explosion;
//                    smokeEffect = Fx.shootSmallSmoke;
//                    homingPower = 0.25f;
//                    buildingDamageMultiplier = 0.10f;
//                }};
//            }});
        }};
    }
}
