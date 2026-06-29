package nitis.gravillaso.maps.planet;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.struct.FloatSeq;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import arc.util.noise.Ridged;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
import mindustry.content.Planets;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.maps.planet.SerpuloPlanetGenerator;
import mindustry.type.Sector;
import mindustry.world.Block;
import mindustry.world.TileGen;
import nitis.gravillaso.content.GRBlocks;

import static mindustry.Vars.state;

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

    Block[][] arr =
    {
        {Blocks.water, Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.darksand, Blocks.darksand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.darksandTaintedWater, Blocks.stone, Blocks.stone},
        {Blocks.water, Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.darksandTaintedWater, Blocks.stone, Blocks.stone, Blocks.stone},
        {Blocks.water, Blocks.darksandWater, Blocks.darksand, Blocks.sand, Blocks.salt, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.darksandTaintedWater, Blocks.stone, Blocks.stone, Blocks.stone},
        {Blocks.water, Blocks.sandWater, Blocks.sand, Blocks.salt, Blocks.salt, Blocks.salt, Blocks.sand, Blocks.stone, Blocks.stone, Blocks.stone, Blocks.snow, Blocks.iceSnow, Blocks.ice},
        {Blocks.deepwater, Blocks.water, Blocks.sandWater, Blocks.sand, Blocks.salt, Blocks.sand, Blocks.sand, Blocks.basalt, Blocks.snow, Blocks.snow, Blocks.snow, Blocks.snow, Blocks.ice},
        {Blocks.deepwater, Blocks.water, Blocks.sandWater, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.moss, Blocks.iceSnow, Blocks.snow, Blocks.snow, Blocks.ice, Blocks.snow, Blocks.ice},
        {Blocks.deepwater, Blocks.sandWater, Blocks.sand, Blocks.sand, Blocks.moss, Blocks.moss, Blocks.snow, Blocks.basalt, Blocks.basalt, Blocks.basalt, Blocks.ice, Blocks.snow, Blocks.ice},
        {Blocks.deepTaintedWater, Blocks.darksandTaintedWater, Blocks.darksand, Blocks.darksand, Blocks.basalt, Blocks.moss, Blocks.basalt, Blocks.hotrock, Blocks.basalt, Blocks.ice, Blocks.snow, Blocks.ice, Blocks.ice},
        {Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.darksand, Blocks.moss, Blocks.sporeMoss, Blocks.snow, Blocks.basalt, Blocks.basalt, Blocks.ice, Blocks.snow, Blocks.ice, Blocks.ice},
        {Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.sporeMoss, Blocks.ice, Blocks.ice, Blocks.snow, Blocks.snow, Blocks.snow, Blocks.snow, Blocks.ice, Blocks.ice, Blocks.ice},
        {Blocks.deepTaintedWater, Blocks.darksandTaintedWater, Blocks.darksand, Blocks.sporeMoss, Blocks.sporeMoss, Blocks.ice, Blocks.ice, Blocks.snow, Blocks.snow, Blocks.ice, Blocks.ice, Blocks.ice, Blocks.ice},
        {Blocks.taintedWater, Blocks.darksandTaintedWater, Blocks.darksand, Blocks.sporeMoss, Blocks.moss, Blocks.sporeMoss, Blocks.iceSnow, Blocks.snow, Blocks.ice, Blocks.ice, Blocks.ice, Blocks.ice, Blocks.ice},
        {Blocks.darksandWater, Blocks.darksand, Blocks.snow, Blocks.ice, Blocks.iceSnow, Blocks.snow, Blocks.snow, Blocks.snow, Blocks.ice, Blocks.ice, Blocks.ice, Blocks.ice, Blocks.ice}
    };

    public boolean allowNumberedLaunch(Sector sector) {
        return sector != null && sector.hasBase() && !sector.isAttacked() && (sector.info.bestCoreType.size >= 4 ||
                sector.isBeingPlayed() && state.rules.defaultTeam.cores().contains(b -> b.block.size >= 4));
    }

    @Override
    public boolean allowLanding(Sector sector) {
        return sector != null && sector.planet.allowLaunchToNumbered && (sector.hasBase() || sector.near().contains(this::allowNumberedLaunch));
    }

    float rawHeight(Vec3 position) {
        return (Mathf.pow(Simplex.noise3d(seed, 7, 0.5f, 1f/3f, position.x * scl, position.y * scl + heightYOffset, position.z * scl) * heightScl, 2.3f) + waterOffset) / (1f + waterOffset);
    }

    @Override
    public float getSizeScl() {
        return 2100f;
    }

    @Override
    public int getSectorSize(Sector sector) {
        int size = super.getSectorSize(sector);

        // TODO: add randomization to sector size?

        return size;
    }

    @Override
    public float getHeight(Vec3 position) {
        float height = rawHeight(position);
        return Math.max(height, 0.15f);
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

        //float tar = Simplex.noise3d(seed, 4, 0.55f, 1f/2f, px, py + 999f, pz) * 0.3f + position.dst(0, 0, 1f) * 0.2f;

        Block res = arr[Mathf.clamp((int)(temp * arr.length), 0, arr[0].length - 1)][Mathf.clamp((int)(height * arr[0].length), 0, arr[0].length - 1)];

        /*if(visualOnly && position.within(basePos, 0.65f)){

            float dst = 999f;

            Object[] sectors = Planets.serpulo.sectors.items;
            int size = Planets.serpulo.sectors.size;

            for(int i = 0; i < size; i ++){
                var sector = (Sector)sectors[i];

                if(sector.hasEnemyBase()){
                    dst = Math.min(dst, position.dst(sector.tile.v));
                }
            }

            float freq = 0.05f, freq2 = 0.07f;

            if(dst * 0.85f + Simplex.noise3d(seed, 3, 0.4, 5.5f, position.x, position.y + 200f, position.z)*0.015f + ((basePos.dst(position) + 0.00f) % freq < freq/2f ? 1f : 0f) * 0.07f < 0.15f){
                return ((basePos.dst(position) + 0.01f) % freq2 < freq2*0.65f) ? Blocks.metalFloor : Blocks.darkPanel6;
            }
        }*/
        return res;
    }

    @Override
    public void genTile(Vec3 position, TileGen tile){
        tile.floor = getBlock(position, false);
        if(tile.floor == Blocks.darkPanel6) tile.floor = Blocks.darkPanel3;
        tile.block = tile.floor.asFloor().wall;

        if(Ridged.noise3d(seed + 1, position.x, position.y, position.z, 2, 22) > 0.31){
            tile.block = Blocks.air;
        }
    }

    @Override
    public void getColor(Vec3 position, Color out){
        Block block = getBlock(position, true);

        out.set(block.mapColor).a(1f - block.albedo);
    }

    @Override
    protected void generate() {
        cells(4);
        distort(6f, 12f);

        median(3);

        blend(Blocks.snow, Blocks.iceSnow, 0.008f);

        decoration(0.06f);

        Schematics.placeLaunchLoadout(width / 2, height / 2);

        Seq<Block> ores = Seq.with(Blocks.oreLead);
        float poles = Math.abs(sector.tile.v.y);

        FloatSeq oreFrequencies = new FloatSeq();
        for(int i = 0; i < ores.size; i++){
            oreFrequencies.add(rand.random(-0.1f, 0.01f) - i * 0.01f + poles * 0.04f);
        }

        pass((x, y) -> {
            if(!floor.asFloor().hasSurface()) return;

            int offsetX = x - 4, offsetY = y + 23;
            for(int i = ores.size - 1; i >= 0; i--){
                Block entry = ores.get(i);
                float freq = oreFrequencies.get(i);
                if(Math.abs(0.5f - noise(offsetX, offsetY + i * 999, 2, 0.7, (40 + i * 2))) > 0.22f + i*0.01 &&
                        Math.abs(0.5f - noise(offsetX, offsetY - i * 999, 1, 1, (30 + i * 4))) > 0.37f + freq){
                    ore = entry;
                    break;
                }
            }
        });
    }
}
