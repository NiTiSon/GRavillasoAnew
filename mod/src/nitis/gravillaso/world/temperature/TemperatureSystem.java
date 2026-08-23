package nitis.gravillaso.world.temperature;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import mindustry.io.SaveFileReader.*;
import mindustry.io.*;

import java.io.*;
import java.util.*;

import static mindustry.Vars.*;
import static nitis.gravillaso.GravillasoMod.grState;
import static nitis.gravillaso.graphics.GrPal.*;

/**
 * Per-tile temperature grid, one value per 1x1 tile, normalized to [-1, 1]:
 * -1 very cold, 0 neutral, +1 very hot. Rebuilt from the floor layout on every world load.
 */
public final class TemperatureSystem implements CustomChunk{
    // Whenever draw the temperature debug view.
    public static boolean enableDebugView = false;
    private static volatile int ww, wh;

    // PERF: Maybe reconsider using bytes/short instead
    // Does it worth it? This array is not exceed 1MB most of the time
    /** Array of normalized[-1..1] thermal state of tile */
    private static volatile float[] data;

    public TemperatureSystem() {
        Events.on(WorldLoadEvent.class, e -> {
            ww = world.width();
            wh = world.height();
            data = new float[ww * wh];

            Arrays.fill(data, grState.rules.baseTemperature);

            Log.debug("Initialized thermal grid: @x@; base temperature: @", ww, wh, grState.rules.baseTemperature);
        });
        Events.run(Trigger.update, TemperatureSystem::update);
        Events.run(Trigger.draw, TemperatureSystem::drawDebug);

        enableDebugView = Core.settings.getBool("gr-showtemperature");
        SaveVersion.addCustomChunk("gr-anew-temperature-system" ,this);
    }

    static void drawDebug(){
        if (!enableDebugView) return;

        var whiteRect = Core.atlas.find("white");
        Core.camera.bounds(Tmp.r1);
        int minx = Math.max(0, Mathf.floor(Tmp.r1.x / tilesize));
        int miny = Math.max(0, Mathf.floor(Tmp.r1.y / tilesize));
        int maxx = Math.min(ww, Mathf.ceil((Tmp.r1.x + Tmp.r1.width) / tilesize));
        int maxy = Math.min(wh, Mathf.ceil((Tmp.r1.y + Tmp.r1.height) / tilesize));

        // too zoomed out to see per-tile data, skip to avoid a lag spike
        if((maxx - minx) * (maxy - miny) > 120_000) return;

        Draw.z(Layer.blockOver);
        for(int y = miny; y < maxy; y++){
            for(int x = minx; x < maxx; x++){
                float t = data[x + y * ww];
                if(t == 0f) continue;
                Draw.color(t < 0f ? debugColdColor : debugHotColor, Math.abs(t));
                Draw.rect(whiteRect, x * tilesize, y * tilesize, tilesize, tilesize);
            }
        }
        Draw.color();
    }

    static int tmp;
    static void update() {
        if (!grState.rules.frostEnabled) return;

        Log.debug("Base temp: @", grState.rules.baseTemperature);
        world.tiles.each(TemperatureSystem::tileUpdate);
    }

    static void tileUpdate(int x, int y){

    }

    @Override
    public void write(DataOutput stream) throws IOException {
        stream.writeShort(world.width());
        stream.writeShort(world.height());

        int size = ww * wh;
        for (int i = 0; i < size; i++){
            stream.writeShort(encodeNormalizedFloat(data[i])); // leave for forward-compatibility
        }
    }

    @Override
    public void read(DataInput stream) throws IOException {
        int w = stream.readShort(), h = stream.readShort();

        ww = w; wh = h;
        int size = w * h;
        for (int i = 0; i < size; i++){
            data[i] = decodeNormalizedFloat(stream.readShort());
        }
        Log.debug("world read");
    }

    public static short encodeNormalizedFloat(float value) {
        if (value < 0){
            return (short)(value * Short.MIN_VALUE);
        }else{
            return (short)(value * Short.MAX_VALUE);
        }
    }

    public static float decodeNormalizedFloat(short value) {
        if (value < 0){
            return (float)value / Short.MIN_VALUE;
        } else{
            return (float)value / Short.MAX_VALUE;
        }
    }
}

/*
public class TemperatureSystem {
    private static float[] temperature = new float[0];
    private static int width = 1;

    // temperatures below this freeze liquid carriers (pipes crack)
    private static final float crackTemp = -0.4f;
    // temperatures below this damage every building
    private static final float damageTemp = -0.7f;
    // hp per second while cracking, and max cold damage per second (at -1)
    private static final float crackRate = 80f;
    private static final float coldRate = 40f;

    public static final ObjectSet<Block> insulated = new ObjectSet<>();

    public static boolean debugDraw;

    public static void init() {
        Events.on(WorldLoadEvent.class, e -> build());
        Events.run(Trigger.update, TemperatureSystem::updateDamage);
        Events.run(Trigger.draw, TemperatureSystem::drawDebug);
        registerChunk();
    }

    static float lastCheck;

    static void updateDamage() {
        // host/singleplayer only, on the gravillo planet, no point before the grid exists
        if(net.client() || state.rules.planet != GRPlanets.gravillo || temperature.length == 0) return;

        float dt = 0.5f;
        if(Time.time - lastCheck < dt * 60f) return;
        lastCheck = Time.time;

        for(Building b : Groups.build){
            float t = temperature(b.tileX(), b.tileY());
            float dmg = 0f;

            if(t <= damageTemp){
                dmg += coldRate * Mathf.clamp((t - damageTemp) / (1f + damageTemp), 0f, 1f) * dt;
            }

            if(t <= crackTemp && canCrack(b)){
                dmg += crackRate * dt;
                if(Mathf.chance(0.4)) Sounds.blockBreak1.at(b.x, b.y, 0.4f, Mathf.random(0.9f, 1.2f));
            }

            if(dmg > 0f){
                b.damage(dmg);
                // same full-block flash mechanics a mender uses for heal, only blue
                Fx.healBlockFull.at(b.x, b.y, b.block.size, coldFlash, b.block);
            }
        }
    }

    static boolean canCrack(Building b){
        // anything carrying liquid freezes; insulated blocks are exempt
        return !insulated.contains(b.block)
            && b.block.hasLiquids && b.liquids != null
            && b.liquids.currentAmount() > 0.01f
            && b.liquids.current().temperature <= 0.5f;
    }

    private static void registerChunk() {
        SaveVersion.addCustomChunk("gr-anew-temperature", new CustomChunk() {
            static final int version = 1;

            @Override
            public boolean shouldWrite() {
                return temperature.length > 0;
            }

            @Override
            public void write(DataOutput stream) throws IOException {
                stream.write(version);
                stream.writeInt(width);
                stream.writeInt(temperature.length / width);
                for(float t : temperature) stream.writeFloat(t);
            }

            @Override
            public void read(DataInput stream) throws IOException {
                int readVersion = stream.readInt();
                int w = stream.readInt();
                int h = stream.readInt();
                // stale or mismatched save: keep the floor-derived build
                if(w <= 0 || h <= 0 || w != world.width() || h != world.height()) return;
                width = w;
                temperature = new float[w * h];
                for(int i = 0; i < temperature.length; i++) temperature[i] = stream.readFloat();
            }
        });
    }

    public static void build() {
        if(state.rules.planet != GRPlanets.gravillo) return;

        width = world.width();
        int height = world.height();
        temperature = new float[width * height];

        world.tiles.each((x, y) -> temperature[x + y * width] = floorTemp(world.tiles.get(x, y).floor()));
    }

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
        var whiteRect = Core.atlas.find("white");
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
                Draw.color(t < 0f ? debugColdColor : debugHotColor, Math.abs(t));
                Draw.rect(whiteRect, x * tilesize, y * tilesize, tilesize, tilesize);
            }
        }
        Draw.color();
    }
}
*/