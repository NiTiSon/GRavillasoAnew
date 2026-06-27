package nitis.gravillaso.content;

import mindustry.type.SectorPreset;

import static nitis.gravillaso.content.GRPlanets.gravillo;

public class GRSectorPresets {
    public static SectorPreset negativeOnCelsius;
    public static void load() {
        negativeOnCelsius = new SectorPreset("negative-on-celsius", gravillo, 0) {{
            alwaysUnlocked = true;
            addStartingItems = true;
            captureWave = 10;
            difficulty = 1;
            overrideLaunchDefaults = true;
            noLighting = true;
            startWaveTimeMultiplier = 3f;
        }};
    }
}
