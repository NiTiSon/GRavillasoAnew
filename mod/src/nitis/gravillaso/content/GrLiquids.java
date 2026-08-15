package nitis.gravillaso.content;

import arc.graphics.Color;
import mindustry.type.Liquid;

public class GrLiquids{
    public static Liquid brine;

    public static void load(){
        brine = new Liquid("brine", Color.valueOf("f3f3f3")){{
            viscosity = 0.2f;
        }};
    }
}