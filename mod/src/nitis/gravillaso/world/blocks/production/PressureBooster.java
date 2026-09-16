package nitis.gravillaso.world.blocks.production;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
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
    public float maxProducedPressure = 80f;
    public float pressureSpeed = 0.15f;

    public DrawBlock drawer = new DrawDefault();

    public PressureBooster(String name){
        super(name);
        update = true;
        solid = true;
        // TODO: boost cause raise of maxPressure?
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("gr-pressure", (PressureBoosterBuild build) -> new Bar(
        () -> Core.bundle.format("bar.gr-pressure", build.pressure(), build.maxPressure()),
        () -> GrPal.pressure,
        build::pressureFrac
        ));
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.output, maxProducedPressure, GrStatUnit.pressure);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        return !(tile.getLinkedTilesAs(this, tempTiles).contains(t -> !(t.floor() instanceof FissureBlock)));
    }

    @Override
    protected TextureRegion[] icons(){
        return drawer.icons(this);
    }

    public class PressureBoosterBuild extends Building{
        public float pressure;

        public float pressure(){
            return pressure;
        }

        public float pressureFrac(){
            return pressure / Math.max(maxProducedPressure, maxPressure());
        }

        public float maxPressure(){
            // TODO: Implement
            return ReservoirSystem.getPressureCapacity(reservoir());
        }

        public int reservoir(){
            return tile.extraData;
        }

        @Override
        public void updateTile(){
            if(efficiency > 0){
                pressure = Mathf.approachDelta(maxProducedPressure, 0f, pressureSpeed);
            }else{
                pressure = Mathf.approachDelta(pressure, 0f, pressureSpeed);
            }
        }
    }
}
