package nitis.gravillaso.content;

import mindustry.ai.types.FlyingAI;
import mindustry.gen.UnitEntity;
import mindustry.type.UnitType;

public class GRUnitTypes {
    public static UnitType cargoDrone, bigCargoDrone;

    public static void load() {
        cargoDrone = new UnitType("cargo-drone") {{
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
            aiController = FlyingAI::new;
            constructor = UnitEntity::create;
        }};

        bigCargoDrone = new UnitType("big-cargo-drone") {{
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
            aiController = FlyingAI::new;
            constructor = UnitEntity::create;
        }};
    }
}
