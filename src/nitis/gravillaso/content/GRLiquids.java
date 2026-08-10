package nitis.gravillaso.content;

import arc.graphics.Color;
import mindustry.type.Liquid;

public class GRLiquids{
    public static Liquid brine;
    public static void load(){
        brine = new Liquid("brine", Color.valueOf("f3f3f3")) {{
            coolant = false;
        }};
    }
}
