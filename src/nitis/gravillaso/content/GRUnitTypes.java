package nitis.gravillaso.content;

import mindustry.content.UnitTypes;
import mindustry.gen.UnitEntity;
import mindustry.type.UnitType;

public class GRUnitTypes {
    // core units
    public static UnitType test;

    public static void load() {
        test = new UnitType("test"){{
            alwaysCreateOutline = true;
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
    }
}
