package nitis.gravillaso.content;

import mindustry.ai.types.AssemblerAI;
import mindustry.ai.types.MinerAI;
import mindustry.content.UnitTypes;
import mindustry.gen.UnitEntity;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.unit.ErekirUnitType;
import mindustry.world.meta.Env;
import nitis.gravillaso.graphics.GRPal;
import nitis.gravillaso.type.GravillasoUnitType;

public class GRUnitTypes {
    // core units
    public static UnitType test;

    // core drones
    public static UnitType draugDrone; // more drones like building helpers and healers

    public static void load() {
        test = new GravillasoUnitType("test"){{
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
        }};

        draugDrone = new GravillasoUnitType("draug-drone"){{
            controller = u -> new MinerAI();

            flying = true;
            drag = 0.06f;
            accel = 0.11f;
            speed = 1.3f;
            health = 90;
            range = 60f;
            engineSize = 2f;
            engineOffset = 5.5f;
            payloadCapacity = 0f;
            targetable = false;
            bounded = false;

            isEnemy = false;
            hidden = false; // TODO: replace with trueddds
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
