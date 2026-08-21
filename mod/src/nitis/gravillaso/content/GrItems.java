package nitis.gravillaso.content;

import arc.graphics.*;
import mindustry.type.*;
import nitis.gravillaso.graphics.GrPal;

public class GrItems{
    public static Item cobalt, bauxite, aluminium;

    public static void load(){
        cobalt = new Item("cobalt", GrPal.cobalt){{
            cost = 1f;
        }};

        bauxite = new Item("bauxite", GrPal.todoColor){{
            cost = 0.5f;
            buildable = false; // intermediate resource for silicon and aluminium
        }};

        aluminium = new Item("aluminium", GrPal.todoColor){{
            cost = 1.5f;
        }};

        /* to removal
        magneturn = new Item("magneturn", Color.valueOf("3121ff")){{
            explosiveness = 0.7f;
            radioactivity = 0.1f;
        }};*/
    }
}