package nitis.gravillaso.content;

import arc.graphics.Color;
import mindustry.content.Blocks;
import mindustry.content.Planets;
import mindustry.content.SerpuloTechTree;
import mindustry.game.Difficulty;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.graphics.g3d.HexMesh;
import mindustry.graphics.g3d.HexSkyMesh;
import mindustry.graphics.g3d.MultiMesh;
import mindustry.maps.planet.SerpuloPlanetGenerator;
import mindustry.type.Planet;
import mindustry.world.meta.Env;
import nitis.gravillaso.maps.planet.GravilloPlanetGenerator;

public class GRPlanets {
    public static Planet gravillo;

    public static void load() {
        gravillo = new Planet("gravillo", Planets.sun, 1.125f, 2) {{
            loadPlanetData = false;

            generator = new GravilloPlanetGenerator();
            meshLoader = () -> new HexMesh(this, 5);
            cloudMeshLoader = () -> new MultiMesh(
                new HexSkyMesh(this, 2, 0.15f, 0.13f, 5,
                    Color.white.cpy().mul(0.9f).a(0.75f), 2, 0.45f, 0.9f, 0.38f),
                new HexSkyMesh(this, 1, 0.6f, 0.16f, 5,
                    Color.white.cpy().lerp(Color.valueOf("#cceeff"), 0.55f).a(0.75f), 2, 0.45f, 1f, 0.41f)
            );
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
            defaultCore = GRBlocks.coreFortress;

            allowWaves = true;
            allowSectorInvasion = true;
            allowLaunchSchematics = true;
            allowLaunchLoadout = true;
            allowLegacyLaunchPads = true;
            allowSelfSectorLaunch = true;
            enemyCoreSpawnReplace = true;

            enemyFactoryActivationDelay = 7200f;
            launchCapacityMultiplier = 0.5f;

            ruleSetter = rules -> {
                rules.waveTeam = Team.blue;
                rules.placeRangeCheck = false;
                rules.showSpawns = false;
                rules.coreDestroyClear = true;
            };

            showRtsAIRule = true;

            allowCampaignRules = true;
            campaignRuleDefaults.difficulty = Difficulty.normal;
            campaignRuleDefaults.fog = true;
            campaignRuleDefaults.showSpawns = false;
            campaignRuleDefaults.rtsAI = true;
        }};
    }
}
