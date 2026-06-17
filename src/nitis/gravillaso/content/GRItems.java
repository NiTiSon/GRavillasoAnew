package nitis.gravillaso.content;

import arc.graphics.Color;
import mindustry.content.Items;
import mindustry.type.Item;

public class GRItems {
    public static Item cobalt, bauxite, alumina, aluminum;

    public static void load() {
        cobalt = new Item("cobalt", Color.valueOf("#0047ab")) {{
            hardness = 1;
            cost = 0.5f;
        }};
        bauxite = new Item("bauxite") {{
            color = Color.valueOf("#c97a5e");
            cost = 0.3f;
            buildable = false;
        }};

        alumina = new Item("alumina") {{
            color = Color.valueOf("#e8e0d8");
            cost = 2f;
        }};

        aluminum = new Item("aluminum") {{
            color = Color.valueOf("#c0c8d8");
            cost = 1.3f;
        }};
    }
}
