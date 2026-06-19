package nitis.gravillaso.content;

import mindustry.gen.UnitEntity;
import mindustry.type.UnitType;
import nitis.gravillaso.ai.CargoDroneAI;
import nitis.gravillaso.type.CargoUnitType;

public class GRUnitTypes {
    public static UnitType phortotis, skoros;

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
        }};
    }
}
