package nitis.gravillaso.content;

import arc.graphics.Color;
import mindustry.content.Liquids;
import mindustry.content.StatusEffects;
import mindustry.type.Liquid;

public class GrLiquids{
    public static Liquid brine, oxygen;

    public static void load(){
        brine = new Liquid("brine", Color.valueOf("f7bfa6")){{
            viscosity = 0.2f;
            effect = StatusEffects.wet;
            boilPoint = 0.4f;
            gasColor = Color.grays(0.9f);
        }};

        oxygen = new Liquid("oxygen", Color.valueOf("e6f8ff")){{
           gas = true;
        }};
    }
}