package nitis.gravillaso.content;

import arc.graphics.Color;
import mindustry.type.Liquid;

public class GRLiquids {
    public static Liquid brine;

    public static void load() {
        brine = new Liquid("brine", Color.valueOf("f2e1e1")) {{
            coolant = false;
            viscosity = 0.6f;
            heatCapacity = 0.3f;
            boilPoint = -1f;
        }};
    }
}
