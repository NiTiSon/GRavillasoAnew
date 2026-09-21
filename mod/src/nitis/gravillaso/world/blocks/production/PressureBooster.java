package nitis.gravillaso.world.blocks.production;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import nitis.gravillaso.graphics.*;
import nitis.gravillaso.world.blocks.environment.*;
import nitis.gravillaso.world.meta.*;
import nitis.gravillaso.world.reservoir.*;

public class PressureBooster extends Block{
    public float maxPressureProduction = 80f;
    public float pressureSpeed = 0.1f;

    public DrawBlock drawer = new DrawDefault();

    public PressureBooster(String name){
        super(name);
        update = true;
        solid = true;
        // TODO: boost cause raise of maxPressure?
    }

    @Override
    public void load(){
        super.load();
        drawer.load(this);
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("gr-pressure", (PressureBoosterBuild build) -> new Bar(
        () -> Core.bundle.format("bar.gr-pressure", (int)build.pressure(), build.maxPressure()),
        () -> GrPal.pressure,
        build::pressureFrac
        ));
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.output, maxPressureProduction, GrStatUnit.pressure);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        return !(tile.getLinkedTilesAs(this, tempTiles).contains(t -> !(t.floor() instanceof FissureBlock)));
    }

    @Override
    protected TextureRegion[] icons(){
        return drawer.icons(this);
    }

    public class PressureBoosterBuild extends Building implements PressureProducer{
        public float pressure;

        @Override
        public void draw(){
            drawer.draw(this);
        }

        @Override
        public void updateTile(){
            if(efficiency > 0){
                pressure = Mathf.approachDelta(pressure, maxPressureProduction(), pressureSpeed * edelta());
            }else{
                pressure = Mathf.approachDelta(pressure, 0f, pressureSpeed * Time.delta);
            }
        }

        @Override
        public void onProximityAdded(){
            super.onProximityAdded();

            ReservoirSystem.addProducer(this);
        }

        @Override
        public void onProximityRemoved(){
            super.onProximityRemoved();

            ReservoirSystem.removeProducer(this);
        }

        public float maxPressureProduction(){
            return maxPressureProduction * this.timeScale;
        }

        public float pressure(){
            return pressure;
        }

        public float pressureFrac(){
            return pressure / Math.max(maxPressureProduction(), maxPressure());
        }

        public float maxPressure(){
            // TODO: Implement
            return ReservoirSystem.getPressureCapacity(reservoir());
        }

        public int reservoir(){
            return tile.extraData;
        }

        @Override
        public byte version(){
            return 1;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(pressure);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            pressure = read.f();
        }
    }
}
