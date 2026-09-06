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
import nitis.gravillaso.content.*;
import nitis.gravillaso.core.*;

import static mindustry.Vars.*;

public class GravilloPlanetGenerator extends PlanetGenerator {
    public float heightScl = 0.7f, octaves = 10, persistence = 0.5f, heightPow = 3f, heightMult = 1.6f;

    //Block[] terrain = {Blocks.regolith, Blocks.regolith, Blocks.regolith, Blocks.regolith, Blocks.yellowStone, Blocks.rhyolite, Blocks.rhyolite, Blocks.carbonStone};
    Block[] terrain = {GrBlocks.corundum, GrBlocks.corundum, GrBlocks.corundum, GrBlocks.corundum, GrBlocks.purpleStone};

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
}