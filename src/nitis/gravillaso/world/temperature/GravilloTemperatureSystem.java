package nitis.gravillaso.world.temperature;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.content.Blocks;
import mindustry.game.EventType.Trigger;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import nitis.gravillaso.content.GRBlocks;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

/**
 * Per-tile temperature grid, one value per 1x1 tile, normalized to [-1, 1]:
 * -1 very cold, 0 neutral, +1 very hot. Rebuilt from the floor layout on every world load.
 */
public class GravilloTemperatureSystem {
    private static float[] temperature = new float[0];
    private static int width = 1;

    private static final Color coldColor = Color.valueOf("4d9fff");
    private static final Color hotColor = Color.valueOf("ff5f3c");

    public static boolean debugDraw;

    public static void init() {
        Events.on(WorldLoadEvent.class, e -> build());
        Events.run(Trigger.draw, GravilloTemperatureSystem::drawDebug);
    }

    public static void build() {
        width = world.width();
        int height = world.height();
        temperature = new float[width * height];

        world.tiles.each((x, y) -> temperature[x + y * width] = floorTemp(world.tiles.get(x, y).floor()));
    }

    /** Base temperature contributed by a floor, before emitters/weather. */
    // ponytail: hardcoded per-floor values, move to content when the system grows
    static float floorTemp(Block floor) {
        if(floor == Blocks.ice) return -0.6f;
        if(floor == Blocks.iceSnow) return -0.5f;
        if(floor == Blocks.snow) return -0.35f;
        if(floor == Blocks.redIce) return -0.25f;
        if(floor == Blocks.shale) return 0.05f;
        if(floor == Blocks.darksand) return 0.1f;
        if(floor == Blocks.water) return 0.05f;
        if(floor == Blocks.deepwater) return 0.1f;
        if(floor == GRBlocks.bauxite) return 0.05f;
        return 0f;
    }

    public static int size() {
        return temperature.length;
    }

    public static float temperature(int x, int y) {
        return temperature.length == 0 ? 0f : temperature[x + y * width];
    }

    public static float temperature(Building b) {
        return temperature(b.tileX(), b.tileY());
    }

    public static void setTemperature(int x, int y, float t) {
        temperature[x + y * width] = Mathf.clamp(t, -1f, 1f);
    }

    static void drawDebug() {
        if(!debugDraw || temperature.length == 0) return;

        Core.camera.bounds(Tmp.r1);
        int minx = Math.max(0, Mathf.floor(Tmp.r1.x / tilesize));
        int miny = Math.max(0, Mathf.floor(Tmp.r1.y / tilesize));
        int maxx = Math.min(width, Mathf.ceil((Tmp.r1.x + Tmp.r1.width) / tilesize));
        int maxy = Math.min(world.height(), Mathf.ceil((Tmp.r1.y + Tmp.r1.height) / tilesize));

        // too zoomed out to see per-tile data, skip to avoid a lag spike
        if((maxx - minx) * (maxy - miny) > 120_000) return;

        Draw.z(Layer.blockOver);
        for(int y = miny; y < maxy; y++){
            for(int x = minx; x < maxx; x++){
                float t = temperature[x + y * width];
                if(t == 0f) continue;
                Draw.color(t < 0f ? coldColor : hotColor, Math.abs(t));
                Draw.rect(Core.atlas.find("white"), x * tilesize, y * tilesize, tilesize, tilesize);
            }
        }
        Draw.color();
    }
}