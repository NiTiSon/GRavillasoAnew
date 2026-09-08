package nitis.gravillaso.content;

import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.type.weather.*;
import mindustry.world.meta.*;

public class GrWeathers {
    public static Weather snowstorm;

    public static void load() {
        snowstorm = new ParticleWeather("snowstorm"){{
            particleRegion = "particle";
            drawNoise = true;
            useWindVector = true;
            sizeMax = 140f;
            sizeMin = 70f;
            minAlpha = 0f;
            maxAlpha = 0.2f;
            density = 1500f;
            baseSpeed = 6.4f;
            attrs.set(Attribute.light, -0.4f);
            opacityMultiplier = 0.55f;
            force = 0.3f;
            sound = Sounds.windHowl;
            soundVol = 0.8f;
            duration = 6f * Time.toMinutes;
        }};
    }
}