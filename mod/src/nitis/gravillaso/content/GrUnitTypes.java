package nitis.gravillaso.content;

import mindustry.ai.types.MinerAI;
import mindustry.type.UnitType;
import mindustry.world.meta.Env;
import nitis.gravillaso.type.GravillasoUnitType;

public class GrUnitTypes {
    public static UnitType draugDrone;

    public static void load(){
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
