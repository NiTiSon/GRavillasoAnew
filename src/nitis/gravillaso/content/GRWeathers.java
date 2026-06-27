package nitis.gravillaso.content;

import arc.graphics.Color;
import arc.util.Time;
import mindustry.gen.Sounds;
import mindustry.type.Weather;
import mindustry.type.weather.ParticleWeather;
import mindustry.world.meta.Attribute;

public class GRWeathers {
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
            baseSpeed = 3.4f;
            attrs.set(Attribute.light, -0.4f);
            opacityMultiplier = 0.55f;
            force = 0.2f;
            sound = Sounds.windHowl;
            soundVol = 0.8f;
            duration = 6f * Time.toMinutes;
        }};
    }
}
