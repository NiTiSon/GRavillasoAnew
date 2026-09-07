package nitis.gravillaso.maps.planet;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.noise.*;
import mindustry.ai.*;
import mindustry.ai.BaseRegistry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.maps.generators.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import nitis.gravillaso.content.*;
import nitis.gravillaso.core.*;

import static mindustry.Vars.*;

public class GravilloPlanetGenerator extends PlanetGenerator {
    public float heightScl = 0.7f, octaves = 6, persistence = 0.5f, heightPow = 3f, heightMult = 1.3f;

    public static float liqThresh = 0.90f, liqScl = 70f;
    public static float airThresh = 0.15f, airScl = 11;

    //Block[] terrain = {Blocks.regolith, Blocks.regolith, Blocks.regolith, Blocks.regolith, Blocks.yellowStone, Blocks.rhyolite, Blocks.rhyolite, Blocks.carbonStone};
    Block[] terrain = {GrBlocks.corundum, GrBlocks.corundum, GrBlocks.corundum, GrBlocks.corundum, GrBlocks.purpleStone, Blocks.shale, Blocks.shale};

    {
        baseSeed = 2;
        defaultLoadout = GrLoadouts.basicBase;
    }

    @Override
    public float getHeight(Vec3 position){
        return Mathf.pow(rawHeight(position), heightPow) * heightMult;
    }

    @Override
    public void getColor(Vec3 position, Color out){
        Block block = getBlock(position);

        out.set(block.mapColor).a(1f - block.albedo);
    }

    @Override
    public float getSizeScl(){
        return 2000 * 1.015f * 12f / 11f;
    }

    float rawHeight(Vec3 position){
        return Simplex.noise3d(seed, octaves, persistence, 1f/heightScl, 15f + position.x, 15f + position.y, 15f + position.z);
    }

    Block getBlock(Vec3 position){
        float px = position.x, py = position.y, pz = position.z;

        //float ice = rawTemp(position);
        float height = rawHeight(position);

        height *= 1.2f;
        height = Mathf.clamp(height);

        Block result = terrain[Mathf.clamp((int)(height * terrain.length), 0, terrain.length - 1)];

        //if(ice < 0.3 + Math.abs(Ridged.noise3d(seed + crystalSeed, px + 4f, py + 8f, pz + 1f, crystalOct, crystalScl)) * crystalMag){
        //    return Blocks.crystallineStone;
        //}

//        if(ice < 0.6){
//            if(result == Blocks.rhyolite || result == Blocks.yellowStone || result == Blocks.regolith){
//                //TODO bio(?) luminescent stuff? ice?
//                return Blocks.carbonStone; //TODO perhaps something else.
//            }
//        }

//        //TODO tweak this to make it more natural
//        //TODO edge distortion?
//        if(ice < redThresh - noArkThresh && Ridged.noise3d(seed + arkSeed, px + 2f, py + 8f, pz + 1f, arkOct, arkScl) > arkThresh){
//            //TODO arkyic in middle
//            result = Blocks.beryllicStone;
//        }

//        if(ice > redThresh){
//            result = Blocks.redStone;
//        }else if(ice > redThresh - 0.4f){
//            //TODO this may increase the amount of regolith, but it's too obvious a transition.
//            result = Blocks.regolith;
//        }

        return result;
    }

    public float getTemperature(Vec3 position){
        return getTemperature(position.y, position.z);
    }

    public float getTemperature(float longitude, float latitude){
        return Mathf.map(Math.abs(longitude), 0f, 1f, 0.1f, -0.9f);
    }

    @Override
    public void addWeather(Sector sector, Rules rules) {
        GrRules gr = GrRules.getFrom(rules);
        gr.baseTemperature = getTemperature(sector.tile.v.y, sector.tile.v.x);
        gr.appendTo(rules);
        rules.weather.clear();

        float y = (sector.tile.v.y + 1f) / 2;
        if (y > 0.74f) { // poles has endless snowstorm
            rules.weather.add(new Weather.WeatherEntry(GrWeathers.snowstorm) {{
                always = true;
            }});
        } else {
            rules.weather.add(new Weather.WeatherEntry(GrWeathers.snowstorm));
        }

        rules.lighting = true;

        float normalizedLatitude = Mathf.clamp(Math.abs(sector.tile.v.y)); // 0..1

        Color ambient = new Color();
        float r = Mathf.lerp(0.75f, 0.35f, normalizedLatitude);
        float g = Mathf.lerp(0.82f, 0.42f, normalizedLatitude);
        float b = Mathf.lerp(0.88f, 0.55f, normalizedLatitude);
        ambient.set(r, g, b, 1f);
        rules.ambientLight.set(ambient);
    }

    @Override
    public void genTile(Vec3 position, TileGen tile){
        tile.floor = getBlock(position);

        if(tile.floor == GrBlocks.purpleStone && rand.chance(0.003)){
            tile.floor = GrBlocks.purpleStoneCrater;
        }

        tile.block = tile.floor.asFloor().wall;

        if(Ridged.noise3d(seed + 1, position.x, position.y, position.z, 2, airScl) > airThresh){
            tile.block = Blocks.air;
        }

        // TODO: replace with other block
        if(Ridged.noise3d(seed + 2, position.x, position.y + 4f, position.z, 3, 6f) > 0.6){
            tile.floor = Blocks.carbonStone;
        }
    }

    @Override
    protected void generate(){
        float temperature = getTemperature(sector.tile.v);

        cells(4);

//        pass((x, y) -> {
//            if(floor == Blocks.regolith && noise(x, y, 3, 0.4f, 13f, 1f) > 0.59f){
//                block = Blocks.regolithWall;
//            }
//        });

        float length = width/3f;
        Vec2 trns = Tmp.v1.trns(rand.random(360f), length);
        int
        spawnX = (int)(trns.x + width/2f), spawnY = (int)(trns.y + height/2f),
        endX = (int)(-trns.x + width/2f), endY = (int)(-trns.y + height/2f);
        float maxd = Mathf.dst(width/2f, height/2f);

        erase(spawnX, spawnY, 15);
        brush(pathfind(spawnX, spawnY, endX, endY, tile -> (tile.solid() ? 300f : 0f) + maxd - tile.dst(width/2f, height/2f)/10f, Astar.manhattan), 9);
        erase(endX, endY, 15);

        pass((x, y) -> { // cryogen
            if(floor != Blocks.shale) return;

            if(Math.abs(noise(x, y + 500f, 5, 0.6f, 40f, 1f) - 0.5f) < 0.09f){
                floor = GrBlocks.cryogenFloor;
            }

            if(nearWall(x, y)) return;

            float noise = noise(x + 300, y - x*1.6f + 100, 4, 0.8f, liqScl, 1f);

            if(noise > liqThresh){
                floor = Blocks.cryofluid;
            }
        });

        median(2, 0.6, Blocks.cryofluid);

        blend(Blocks.cryofluid, GrBlocks.cryogenFloor, 4);

        distort(10f, 12f);
        distort(5f, 7f);

        pass((x, y) -> {
            // TODO: floor alternates
            /*
            //rough rhyolite
            if(noise(x, y + 600 + x, 5, 0.86f, 60f, 1f) < 0.41f && floor == Blocks.rhyolite){
                floor = Blocks.roughRhyolite;
            }

            if(floor == Blocks.slag && Mathf.within(x, y, spawnX, spawnY, 30f + noise(x, y, 2, 0.8f, 9f, 15f))){
                floor = Blocks.yellowStonePlates;
            }

            if((floor == Blocks.arkyciteFloor || floor == Blocks.arkyicStone) && block.isStatic()){
                block = Blocks.arkyicWall;
            }

            float max = 0;
            for(Point2 p : Geometry.d8){
                //TODO I think this is the cause of lag
                max = Math.max(max, world.getDarkness(x + p.x, y + p.y));
            }
            if(max > 0){
                block = floor.asFloor().wall;
                if(block == Blocks.air) block = Blocks.yellowStoneWall;
            }

            if(floor == Blocks.yellowStonePlates && noise(x + 78 + y, y, 3, 0.8f, 6f, 1f) > 0.44f){
                floor = Blocks.yellowStone;
            }

            if(floor == Blocks.redStone && noise(x + 78 - y, y, 4, 0.73f, 19f, 1f) > 0.63f){
                floor = Blocks.denseRedStone;
            }*/
        });

        inverseFloodFill(tiles.getn(spawnX, spawnY));

        // blend(Blocks.redStoneWall, Blocks.denseRedStone, 4);

        // make sure enemies have room
        erase(endX, endY, 6);


        tiles.getn(endX, endY).setOverlay(Blocks.spawn);

        pass((x, y) -> { // ores
            if(block != Blocks.air){
                // TODO: rework this into cobalt and lead
                /* if(nearAir(x, y)){
                    if(block == Blocks.carbonWall && noise(x + 78, y, 4, 0.7f, 33f, 1f) > 0.52f){
                        block = Blocks.graphiticWall;
                    }else if(block != Blocks.carbonWall && noise(x + 782, y, 4, 0.8f, 38f, 1f) > 0.665f){
                        ore = Blocks.wallOreBeryllium;
                    }

                }*/
            }
        });

        pass((x, y) -> { // remove large props near resources
            if(ore.asFloor().wallOre || block.itemDrop != null || (block == Blocks.air && ore != Blocks.air)){
                removeWall(x, y, 3, b -> b instanceof TallBlock);
            }
        });

        trimDark();

        for(Tile tile : tiles){
            if(tile.overlay().needsSurface && !tile.floor().hasSurface()){
                tile.setOverlay(Blocks.air);
            }
        }

        decoration(0.017f);

        Schematics.placeLaunchLoadout(spawnX, spawnY);
    }
}