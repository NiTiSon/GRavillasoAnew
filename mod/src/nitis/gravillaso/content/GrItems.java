package nitis.gravillaso.content;

import arc.graphics.*;
import mindustry.type.*;
import nitis.gravillaso.graphics.GrPal;

public class GrItems{
    public static Item cobalt, magneturn;

    public static void load(){
        cobalt = new Item("cobalt", GrPal.cobalt){{
            cost = 1.25f;
        }};

        magneturn = new Item("magneturn", Color.valueOf("3121ff")){{
            explosiveness = 0.7f;
            radioactivity = 0.1f;
        }};
    }
}