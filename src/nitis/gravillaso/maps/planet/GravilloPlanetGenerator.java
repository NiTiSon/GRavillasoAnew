package nitis.gravillaso.maps.planet;

import arc.graphics.Color;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;
import arc.struct.FloatSeq;
import arc.struct.IntSeq;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Tmp;
import arc.util.noise.Ridged;
import arc.util.noise.Simplex;
import mindustry.ai.Astar;
import mindustry.ai.BaseRegistry.BasePart;
import mindustry.content.Blocks;
import mindustry.content.Liquids;
import mindustry.content.Weathers;
import mindustry.game.*;
import mindustry.maps.generators.BaseGenerator;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Sector;
import mindustry.type.Weather;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.TileGen;
import mindustry.world.Tiles;
import nitis.gravillaso.content.GRBlocks;
import nitis.gravillaso.content.GRWeathers;

import static mindustry.Vars.*;

public class GravilloPlanetGenerator extends PlanetGenerator {
    float heightYOffset = 42.7f;
    float scl = 5f;
    float waterOffset = 0.04f;
    float heightScl = 1.01f;

    {
        defaultLoadout = new Schematic(
                Seq.with(new Schematic.Stile(GRBlocks.coreFortress, 0, 0, null, (byte)0)),
                new StringMap(), 4, 4
        );
    }

    // ice, snow, calcite, perite?,
    // cryofluid lakes
    // weather: snow, snowstorm
    // buildings?

    Block[][] arr = {
        {Blocks.ice, Blocks.ice, Blocks.ice, Blocks.iceSnow, Blocks.snow, Blocks.snow, Blocks.snow, Blocks.stone, Blocks.stone, Blocks.stone, Blocks.stone, Blocks.stone, Blocks.stone},
        {Blocks.ice, Blocks.ice, Blocks.ice, Blocks.iceSnow, Blocks.snow, Blocks.snow, Blocks.stone, Blocks.stone, Blocks.stone, Blocks.stone, Blocks.stone, Blocks.stone, Blocks.stone},
        {Blocks.ice, Blocks.ice, Blocks.ice, Blocks.iceSnow, Blocks.snow, Blocks.snow, Blocks.stone, GRBlocks.bauxite, Blocks.shale, Blocks.shale, Blocks.stone, Blocks.stone, Blocks.stone},
        {Blocks.ice, Blocks.iceSnow, Blocks.iceSnow, Blocks.snow, Blocks.snow, Blocks.stone, GRBlocks.bauxite, Blocks.shale, Blocks.shale, Blocks.stone, Blocks.stone, Blocks.stone, Blocks.stone},
        {Blocks.iceSnow, Blocks.snow, Blocks.snow, Blocks.snow, Blocks.stone, GRBlocks.bauxite, Blocks.shale, Blocks.shale, Blocks.stone, Blocks.stone, Blocks.redIce, Blocks.redIce, Blocks.redIce},
        {Blocks.snow, Blocks.snow, Blocks.snow, Blocks.stone, GRBlocks.bauxite, Blocks.stone, Blocks.shale, Blocks.stone, Blocks.stone, Blocks.redIce, Blocks.redIce, Blocks.redIce, Blocks.redIce},
        {Blocks.snow, Blocks.snow, Blocks.stone, GRBlocks.bauxite, Blocks.stone, Blocks.shale, Blocks.shale, Blocks.stone, Blocks.stone, Blocks.redIce, Blocks.redIce, Blocks.redIce, Blocks.redIce},
    };

    ObjectMap<Block, Block> dec = ObjectMap.of(
        Blocks.shale, Blocks.shaleBoulder,
        Blocks.redIce, Blocks.redIceBoulder,
        GRBlocks.bauxite, GRBlocks.bauxiteBoulder
    );

    float water = 0.08f;

    BaseGenerator basegen = new BaseGenerator();

    public boolean allowNumberedLaunch(Sector sector) {
        return sector != null && sector.hasBase() && !sector.isAttacked() && (sector.info.bestCoreType.size >= 4 ||
                sector.isBeingPlayed() && state.rules.defaultTeam.cores().contains(b -> b.block.size >= 4));
    }

    @Override
    public boolean allowLanding(Sector sector) {
        return sector != null && sector.planet.allowLaunchToNumbered && (sector.hasBase() || sector.near().contains(this::allowNumberedLaunch));
    }

    @Override
    public void addWeather(Sector sector, Rules rules) {
        rules.weather.clear();

        rules.weather.add(new Weather.WeatherEntry(Weathers.snow));

        float y = (sector.tile.v.y + 1f) / 2;
        if (y > 0.74f) { // poles has endless snowstorm
            rules.weather.add(new Weather.WeatherEntry(GRWeathers.snowstorm) {{
                always = true;
            }});
        } else {
            rules.weather.add(new Weather.WeatherEntry(GRWeathers.snowstorm));
        }
    }

    float rawHeight(Vec3 position) {
        return (Mathf.pow(Simplex.noise3d(seed, 7, 0.5f, 1f/3f, position.x * scl, position.y * scl + heightYOffset, position.z * scl) * heightScl, 2.3f) + waterOffset) / (1f + waterOffset);
    }

    @Override
    public float getSizeScl() {
        return 2250f;
    }

    @Override
    public float getHeight(Vec3 position) {
        float height = rawHeight(position);
        return Math.max(height, water);
    }

    @Override
    public void getColor(Vec3 position, Color out) {
        Block block = getBlock(position, true);
        out.set(block.mapColor).a(1f - block.albedo);
    }

    Block getBlock(Vec3 position, boolean visualOnly) {
        float height = rawHeight(position);
        float px = position.x * scl, py = position.y * scl, pz = position.z * scl;

        float rad = scl;
        float temp = Mathf.clamp(Math.abs(py * 2f) / (rad));
        float tnoise = Simplex.noise3d(seed, 3, 0.82, 1f/3f, px, py + 1200f - 0.1f, pz);
        temp = Mathf.lerp(temp, tnoise, 0.5f);
        height *= 1.2f;
        height = Mathf.clamp(height);

        return arr[Mathf.clamp((int)(temp * arr.length), 0, arr[0].length - 1)][Mathf.clamp((int)(height * arr[0].length), 0, arr[0].length - 1)];
    }

    @Override
    protected float noise(float x, float y, double octaves, double falloff, double scl, double mag){
        Vec3 v = sector.rect.project(x, y);
        return Simplex.noise3d(seed, octaves, falloff, 1f / scl, v.x, v.y, v.z) * (float)mag;
    }

    @Override
    public void genTile(Vec3 position, TileGen tile) {
        tile.floor = getBlock(position, false);
        tile.block = tile.floor.asFloor().wall;

        if(Ridged.noise3d(seed + 1, position.x, position.y, position.z, 2, 22) > 0.31){
            tile.block = Blocks.air;
        }
    }

    @Override
    protected void generate() {
        class Room{
            int x, y, radius;
            ObjectSet<Room> connected = new ObjectSet<>();

            Room(int x, int y, int radius){
                this.x = x;
                this.y = y;
                this.radius = radius;
                connected.add(this);
            }

            void join(int x1, int y1, int x2, int y2){
                float nscl = rand.random(100f, 140f) * 6f;
                int stroke = rand.random(3, 9);
                brush(pathfind(x1, y1, x2, y2, tile -> (tile.solid() ? 50f : 0f) + noise(tile.x, tile.y, 2, 0.4f, 1f / nscl) * 500, Astar.manhattan), stroke);
            }

            void connect(Room to){
                if(!connected.add(to) || to == this) return;

                Vec2 midpoint = Tmp.v1.set(to.x, to.y).add(x, y).scl(0.5f);
                rand.nextFloat();

                midpoint.add(Tmp.v2.setToRandomDirection(rand).scl(Tmp.v1.dst(x, y)));

                midpoint.sub(width/2f, height/2f).limit(width / 2f / Mathf.sqrt3).add(width/2f, height/2f);

                int mx = (int)midpoint.x, my = (int)midpoint.y;

                join(x, y, mx, my);
                join(mx, my, to.x, to.y);
            }

            void joinLiquid(int x1, int y1, int x2, int y2){
                float nscl = rand.random(100f, 140f) * 6f;
                int rad = rand.random(7, 11);
                int avoid = 2 + rad;
                var path = pathfind(x1, y1, x2, y2, tile -> (tile.solid() || !tile.floor().isLiquid ? 70f : 0f) + noise(tile.x, tile.y, 2, 0.4f, 1f / nscl) * 500, Astar.manhattan);
                path.each(t -> {
                    if(Mathf.dst2(t.x, t.y, x2, y2) <= avoid * avoid){
                        return;
                    }

                    for(int x = -rad; x <= rad; x++){
                        for(int y = -rad; y <= rad; y++){
                            int wx = t.x + x, wy = t.y + y;
                            if(wx >= 0 && wx < width && wy >= 0 && wy < height && Mathf.within(x, y, rad)){
                                Tile other = tiles.getn(wx, wy);
                                other.setBlock(Blocks.air);
                                if(Mathf.within(x, y, rad - 1) && !other.floor().isLiquid){
                                    other.setFloor(Blocks.water.asFloor());
                                }
                            }
                        }
                    }
                });
            }

            void connectLiquid(Room to){
                if(to == this) return;

                Vec2 midpoint = Tmp.v1.set(to.x, to.y).add(x, y).scl(0.5f);
                rand.nextFloat();

                midpoint.add(Tmp.v2.setToRandomDirection(rand).scl(Tmp.v1.dst(x, y)));
                midpoint.sub(width/2f, height/2f).limit(width / 2f / Mathf.sqrt3).add(width/2f, height/2f);

                int mx = (int)midpoint.x, my = (int)midpoint.y;

                joinLiquid(x, y, mx, my);
                joinLiquid(mx, my, to.x, to.y);
            }
        }

        cells(4);
        distort(10f, 12f);

        float constraint = 1.3f;
        float radius = width / 2f / Mathf.sqrt3;
        int rooms = rand.random(2, 5);
        Seq<Room> roomseq = new Seq<>();

        for(int i = 0; i < rooms; i++){
            Tmp.v1.trns(rand.random(360f), rand.random(radius / constraint));
            float rx = (width/2f + Tmp.v1.x);
            float ry = (height/2f + Tmp.v1.y);
            float maxrad = radius - Tmp.v1.len();
            float rrad = Math.min(rand.random(9f, maxrad / 2f), 30f);
            roomseq.add(new Room((int)rx, (int)ry, (int)rrad));
        }

        Room spawn = null;
        Seq<Room> enemies = new Seq<>();
        int enemySpawns = rand.random(1, Math.max((int)(sector.threat * 4), 1));
        int offset = rand.nextInt(360);
        float length = width/2.55f - rand.random(13, 23);
        int angleStep = 5;
        int waterCheckRad = 5;
        for(int i = 0; i < 360; i+= angleStep){
            int angle = offset + i;
            int cx = (int)(width / 2f + Angles.trnsx(angle, length));
            int cy = (int)(height / 2f + Angles.trnsy(angle, length));

            int waterTiles = 0;

            for(int rx = -waterCheckRad; rx <= waterCheckRad; rx++){
                for(int ry = -waterCheckRad; ry <= waterCheckRad; ry++){
                    Tile tile = tiles.get(cx + rx, cy + ry);
                    if(tile == null || tile.floor().liquidDrop != null){
                        waterTiles ++;
                    }
                }
            }

            if(waterTiles <= 4 || (i + angleStep >= 360)){
                roomseq.add(spawn = new Room(cx, cy, rand.random(8, 15)));

                for(int j = 0; j < enemySpawns; j++){
                    float enemyOffset = rand.range(60f);
                    Tmp.v1.set(cx - width/2, cy - height/2).rotate(180f + enemyOffset).add(width/2, height/2);
                    Room espawn = new Room((int)Tmp.v1.x, (int)Tmp.v1.y, rand.random(8, 16));
                    roomseq.add(espawn);
                    enemies.add(espawn);
                }

                break;
            }
        }

        for(Room room : roomseq){
            erase(room.x, room.y, room.radius);
        }

        int connections = rand.random(Math.max(rooms - 1, 1), rooms + 3);
        for(int i = 0; i < connections; i++){
            roomseq.random(rand).connect(roomseq.random(rand));
        }

        for(Room room : roomseq){
            spawn.connect(room);
        }

        cells(1);

        int tlen = tiles.width * tiles.height;
        int total = 0, waters = 0;

        for(int i = 0; i < tlen; i++){
            Tile tile = tiles.geti(i);
            if(tile.block() == Blocks.air){
                total ++;
                if(tile.floor().liquidDrop == Liquids.water){
                    waters ++;
                }
            }
        }

        boolean naval = (float)waters / total >= 0.19f;

        if(naval){
            for(Room room : enemies){
                room.connectLiquid(spawn);
            }
        }

        distort(10f, 6f);

        Room fspawn = spawn;

        pass((x, y) -> {
            if(block.solid) return;

            Vec3 v = sector.rect.project(x, y);

            float value = Ridged.noise3d(2, v.x, v.y, v.z, 1, 1f / 55f);

            if(value > 0.17f && !Mathf.within(x, y, fspawn.x, fspawn.y, 12)){
                if(floor != Blocks.ice && floor != Blocks.iceSnow && floor != Blocks.snow && !floor.asFloor().isLiquid){
                    floor = Blocks.ice;
                }
            }
        });

        pass((x, y) -> {
            int deepRadius = 3;

            if(floor.asFloor().isLiquid && floor.asFloor().shallow){

                for(int cx = -deepRadius; cx <= deepRadius; cx++){
                    for(int cy = -deepRadius; cy <= deepRadius; cy++){
                        if((cx) * (cx) + (cy) * (cy) <= deepRadius * deepRadius){
                            int wx = cx + x, wy = cy + y;

                            Tile tile = tiles.get(wx, wy);
                            if(tile != null && (!tile.floor().isLiquid || tile.block() != Blocks.air)){
                                return;
                            }
                        }
                    }
                }

                floor = Blocks.water;
            }
        });

        if(naval){
            int deepRadius = 2;

            pass((x, y) -> {
                if(floor.asFloor().isLiquid && !floor.asFloor().isDeep() && !floor.asFloor().shallow){

                    for(int cx = -deepRadius; cx <= deepRadius; cx++){
                        for(int cy = -deepRadius; cy <= deepRadius; cy++){
                            if((cx) * (cx) + (cy) * (cy) <= deepRadius * deepRadius){
                                int wx = cx + x, wy = cy + y;

                                Tile tile = tiles.get(wx, wy);
                                if(tile != null && (tile.floor().shallow || !tile.floor().isLiquid)){
                                    return;
                                }
                            }
                        }
                    }

                    floor = Blocks.deepwater;
                }
            });
        }

        Seq<Block> ores = Seq.with(GRBlocks.oreCobalt, Blocks.oreLead);
        float poles = Math.abs(sector.tile.v.y);
        float nmag = 0.5f;
        float scl = 1f;
        float addscl = 1.3f;

        if(Simplex.noise3d(seed, 2, 0.5, scl, sector.tile.v.x, sector.tile.v.y, sector.tile.v.z)*nmag + poles > 0.25f*addscl){
            ores.add(Blocks.oreTitanium);
        }

        if(Simplex.noise3d(seed, 2, 0.5, scl, sector.tile.v.x + 2, sector.tile.v.y, sector.tile.v.z)*nmag + poles > 0.7f*addscl){
            ores.add(Blocks.oreCrystalThorium);
        }

        FloatSeq frequencies = new FloatSeq();
        for(int i = 0; i < ores.size; i++){
            frequencies.add(rand.random(-0.1f, 0.01f) - i * 0.01f + poles * 0.04f);
        }

        pass((x, y) -> {
            if(!floor.asFloor().hasSurface()) return;

            int offsetX = x - 4, offsetY = y + 23;
            for(int i = ores.size - 1; i >= 0; i--){
                Block entry = ores.get(i);
                float freq = frequencies.get(i);
                if(Math.abs(0.5f - noise(offsetX, offsetY + i*999, 2, 0.7, (40 + i * 2))) > 0.22f + i*0.01 &&
                    Math.abs(0.5f - noise(offsetX, offsetY - i*999, 1, 1, (30 + i * 4))) > 0.37f + freq){
                    ore = entry;
                    break;
                }
            }
        });

        trimDark();

        median(2);

        inverseFloodFill(tiles.getn(spawn.x, spawn.y));

        pass((x, y) -> {
            if(floor == Blocks.shale){
                if(Math.abs(0.5f - noise(x - 40, y, 2, 0.7, 80)) > 0.25f &&
                Math.abs(0.5f - noise(x, y + sector.id*10, 1, 1, 60)) > 0.41f && !(roomseq.contains(r -> Mathf.within(x, y, r.x, r.y, 30)))){
                    floor = Blocks.darksand;
                }
            }

            if(rand.chance(0.0075)){
                boolean any = false;
                boolean all = true;
                for(Point2 p : Geometry.d4){
                    Tile other = tiles.get(x + p.x, y + p.y);
                    if(other != null && other.block() == Blocks.air){
                        any = true;
                    }else{
                        all = false;
                    }
                }
                if(any && ((block == Blocks.iceWall || block == Blocks.snowWall) || (all && block == Blocks.air && floor == Blocks.snow && rand.chance(0.03)))){
                    block = rand.chance(0.5) ? Blocks.whiteTree : Blocks.whiteTreeDead;
                }
            }

        });

        pass((x, y) -> {
            dec: {
                for(int i = 0; i < 4; i++){
                    Tile near = tiles.get(x + Geometry.d4[i].x, y + Geometry.d4[i].y);
                    if(near != null && near.block() != Blocks.air){
                        break dec;
                    }
                }

                if(rand.chance(0.01) && floor.asFloor().hasSurface() && block == Blocks.air){
                    block = dec.get(floor, floor.asFloor().decoration);
                }
            }
        });

        float difficulty = sector.threat;
        int ruinCount = rand.random(-2, 4);

        if(ruinCount > 0){
            IntSeq ints = new IntSeq(width * height / 4);

            int padding = 25;

            for(int x = padding; x < width - padding; x++){
                for(int y = padding; y < height - padding; y++){
                    Tile tile = tiles.getn(x, y);
                    if(!tile.solid() && (tile.drop() != null || tile.floor().liquidDrop != null)){
                        ints.add(tile.pos());
                    }
                }
            }

            ints.shuffle(rand);

            int placed = 0;
            float diffRange = 0.4f;
            for(int i = 0; i < ints.size && placed < ruinCount; i++){
                int val = ints.items[i];
                int x = Point2.x(val), y = Point2.y(val);

                if(Mathf.within(x, y, spawn.x, spawn.y, 18f)){
                    continue;
                }

                float range = difficulty + rand.random(diffRange);

                Tile tile = tiles.getn(x, y);
                BasePart part = null;
                if(tile.overlay().itemDrop != null){
                    part = bases.forResource(tile.drop()).getFrac(range);
                }else if(tile.floor().liquidDrop != null && rand.chance(0.05)){
                    part = bases.forResource(tile.floor().liquidDrop).getFrac(range);
                }else if(rand.chance(0.05)){
                    part = bases.parts.getFrac(range);
                }

                if(part != null && BaseGenerator.tryPlace(part, x, y, Team.derelict, rand, (cx, cy) -> {
                    Tile other = tiles.getn(cx, cy);
                    if(other.floor().hasSurface()){
                        other.setOverlay(Blocks.oreScrap);
                        for(int j = 1; j <= 2; j++){
                            for(Point2 p : Geometry.d8){
                                Tile t = tiles.get(cx + p.x*j, cy + p.y*j);
                                if(t != null && t.floor().hasSurface() && rand.chance(j == 1 ? 0.4 : 0.2)){
                                    t.setOverlay(Blocks.oreScrap);
                                }
                            }
                        }
                    }
                })){
                    placed ++;

                    int debrisRadius = Math.max(part.schematic.width, part.schematic.height)/2 + 3;
                    Geometry.circle(x, y, tiles.width, tiles.height, debrisRadius, (cx, cy) -> {
                        float dst = Mathf.dst(cx, cy, x, y);
                        float removeChance = Mathf.lerp(0.05f, 0.5f, dst / debrisRadius);

                        Tile other = tiles.getn(cx, cy);
                        if(other.build != null && other.isCenter()){
                            if(other.team() == Team.derelict && rand.chance(removeChance)){
                                other.remove();
                            }else if(rand.chance(0.5)){
                                other.build.health = other.build.health - rand.random(other.build.health * 0.9f);
                            }
                        }
                    });
                }
            }
        }

        for(Tile tile : tiles){
            if(tile.overlay().needsSurface && !tile.floor().hasSurface()){
                tile.setOverlay(Blocks.air);
            }
        }

        Schematics.placeLaunchLoadout(spawn.x, spawn.y);

        for (Room espawn : enemies){
            tiles.getn(espawn.x, espawn.y).setOverlay(Blocks.spawn);
        }

        if (sector.hasEnemyBase()) {
            basegen.generate(tiles, enemies.map(r -> tiles.getn(r.x, r.y)), tiles.get(spawn.x, spawn.y), state.rules.waveTeam, sector, difficulty);

            state.rules.attackMode = sector.info.attack = true;
        } else {
            state.rules.winWave = sector.info.winWave = 10 + 5 * (int)Math.max(difficulty * 10, 1);
        }

        float waveTimeDec = 0.4f;

        state.rules.waveSpacing = Mathf.lerp(60 * 65 * 2, 60f * 60f * 1f, Math.max(difficulty - waveTimeDec, 0f));
        state.rules.waves = true;
        state.rules.env = sector.planet.defaultEnv;
        state.rules.enemyCoreBuildRadius = 600f;

        state.rules.spawns = Waves.generate(difficulty, new Rand(sector.id), state.rules.attackMode, state.rules.attackMode && spawner.countGroundSpawns() == 0, naval);
    }

    @Override
    public void postGenerate(Tiles tiles){
        if(sector.hasEnemyBase()){
            basegen.postGenerate();

            if(spawner.countGroundSpawns() == 0){
                state.rules.spawns = Waves.generate(sector.threat, new Rand(sector.id), state.rules.attackMode, true, false);
            }
        }
    }
}
