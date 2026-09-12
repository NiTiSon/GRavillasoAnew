package nitis.gravillaso.content;

import arc.util.*;
import mindustry.content.*;
import mindustry.type.*;
import nitis.gravillaso.graphics.*;

import static mindustry.content.StatusEffects.*;

public class GrStatusEffects{
    public static StatusEffect froze;

    public static void load(){
        froze = new StatusEffect("froze"){{
            color = GrPal.frost;
            damage = 9f / Time.toSeconds;
            speedMultiplier = 0.5f;
            healthMultiplier = 0.8f;
            effect = Fx.freezing;
            transitionDamage = 25f;

            init(() -> {
                opposite(melting, burning);

                affinity(blasted, (unit, result, time) -> {
                    unit.damagePierce(transitionDamage);
                });
            });
        }};
    }
}
