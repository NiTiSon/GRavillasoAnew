package nitis.gravillaso.content;

import arc.graphics.Color;
import arc.util.Time;
import mindustry.content.Planets;
import mindustry.game.Difficulty;
import mindustry.game.Team;
import mindustry.graphics.g3d.HexMesh;
import mindustry.type.Planet;
import mindustry.world.meta.Env;
import nitis.gravillaso.core.GrRules;
import nitis.gravillaso.maps.planet.GravilloPlanetGenerator;

public class GrPlanets {
    public static Planet gravillo;

    public static void load() {
        // Planed:
        // One vulcano with no snow nor ice
        // The VERY cold top and down: the storm is heavy and damaging
        gravillo = new Planet("gravillo", Planets.sun, 1.125f, 2) {{
            loadPlanetData = false;

            rotateTime = 10 * 60f;
            generator = new GravilloPlanetGenerator();
            meshLoader = () -> new HexMesh(this, 5);
            //  cloudMeshLoader = () ->
            //         new PlanetRingMesh(this, radius * 2f, radius * 3.4f, Color.valueOf("#e6dbb3"), 0.5f);
            minZoom = 1f;
            maxZoom = 2.5f;

            accessible = true;
            alwaysUnlocked = true;
            visible = true;

            bloom = false;
            drawOrbit = true;

            sectorSeed = 2;
            startSector = 0;

            iconColor = Color.valueOf("#88ddff");
            atmosphereColor = Color.valueOf("#4488cc");
            landCloudColor = Color.white.cpy().a(0.5f);

            atmosphereRadIn = 0.02f;
            atmosphereRadOut = 0.3f;

            defaultEnv = Env.terrestrial | Env.oxygen;
            defaultCore = GrBlocks.coreBase;

            allowWaves = true;
            allowSectorInvasion = false;
            allowLaunchSchematics = true;
            allowLaunchLoadout = true;
            allowLegacyLaunchPads = false;
            allowSelfSectorLaunch = true;
            enemyCoreSpawnReplace = true;

            enemyFactoryActivationDelay = 120f * Time.toSeconds;
            launchCapacityMultiplier = 0.5f;

            ruleSetter = rules -> {
                GrRules grRules = GrRules.getFrom(rules);
                rules.waveTeam = Team.blue;
                rules.placeRangeCheck = false;
                rules.hideSpawns = false;
                rules.coreDestroyClear = true;
                grRules.appendTo(rules);
            };

            showRtsAIRule = true;

            allowCampaignRules = true;
            campaignRuleDefaults.difficulty = Difficulty.normal;
            campaignRuleDefaults.fog = true;
            campaignRuleDefaults.hideSpawns = false;
            campaignRuleDefaults.rtsAI = true;
        }};
    }
}