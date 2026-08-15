package nitis.gravillaso.content;

import arc.graphics.*;
import mindustry.type.*;

public class GrItems{
    public static Item cobalt;

    public static void load(){
        cobalt = new Item("cobalt", Color.valueOf("f3f3f3")){{
            cost = 1.1f;
        }};
    }
}