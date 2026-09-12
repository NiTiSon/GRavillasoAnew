package nitis.gravillaso.world.blocks.power;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.weather.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;

import static mindustry.Vars.*;
import static mindustry.Vars.tilesize;

public class WindPowerGenerator extends PowerGenerator{
    public static float allowedRange = 16f;
    public float windStrengthForMaximumEfficiency = 4f;

    // this generator is kinda boring, change of the wind power is rough
    // maybe we can add perlin to the wind speed?
    public WindPowerGenerator(String name){
        super(name);
        canPickup = false;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        boolean hasConflicts = getPotentialConflicts(tile, build -> {});

        return !hasConflicts && super.canPlaceOn(tile, team, rotation);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        Tile tile = world.tile(x, y);
        if(tile == null) return;

        Lines.stroke(1f);
        Draw.color(Pal.placing);
        Drawf.circles(x * tilesize + offset, y * tilesize + offset, allowedRange * tilesize);

        getPotentialConflicts(tile, build -> {
            Drawf.square(build.x, build.y, build.block.size * tilesize / 2f, 0f, Pal.remove);
        });

        Draw.reset();
    }

    /** returns true if it has any conflicts */
    protected boolean getPotentialConflicts(Tile tile, Cons<Building> others){
        int count = 0;
        int range = Mathf.ceil(allowedRange);
        float rangeSq = allowedRange * allowedRange;
        for(int dx = -range; dx <= range; dx++){
            for(int dy = -range; dy <= range; dy++){
                Tile other = Vars.world.tile(tile.x + dx, tile.y + dy);

                if(other == null || other.build == null) continue;

                if(other.build.block instanceof WindPowerGenerator && Mathf.dst2(tile.x, tile.y, other.x, other.y) <= rangeSq){
                    others.get(other.build);
                    count++;
                }
            }
        }

        return count != 0;
    }

    public class WindTurbineBuild extends GeneratorBuild{
        @Override
        public void updateTile(){
            super.updateTile();

            float wind = 0f;
            for(WeatherState ws : Groups.weather){
                if(ws.weather instanceof ParticleWeather weather){
                    wind += weather.baseSpeed * ws.intensity;
                }
            }

            productionEfficiency = enabled ? Math.min(wind / windStrengthForMaximumEfficiency, 1f) : 0f;
        }

        @Override
        public float warmup(){
            return productionEfficiency;
        }
    }
}
