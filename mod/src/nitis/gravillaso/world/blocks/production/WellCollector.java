package nitis.gravillaso.world.blocks.production;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.draw.*;
import nitis.gravillaso.graphics.*;
import nitis.gravillaso.world.blocks.environment.*;
import nitis.gravillaso.world.blocks.production.PressureBooster.*;
import nitis.gravillaso.world.reservoir.*;

public class WellCollector extends LiquidBlock{
    public float pumpEfficiency = 0.2f;

    public WellCollector(String name){
        super(name);
        canOverdrive = false;
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("gr-pressure", (WellCollectorBuild build) -> new Bar(
        () -> Core.bundle.format("bar.gr-pressure", (int)build.pressure(), build.maxPressure()),
        () -> GrPal.pressure,
        build::pressureFrac
        ));
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        return !(tile.getLinkedTilesAs(this, tempTiles).contains(t -> !(t.floor() instanceof WellBlock)));
    }

    public class WellCollectorBuild extends LiquidBuild{
        public int reservoir(){
            return tile.extraData;
        }

        @Override
        public void updateTile(){
            liquids.add(liquid(), Math.min(liquidCapacity - liquids.get(liquid()), pumpEfficiency * pressureFrac() * delta()));
            dumpLiquid(liquid());
        }

        public Liquid liquid(){
            return ReservoirSystem.getReserviourLiquid(reservoir());
        }

        public float pressure(){
            return ReservoirSystem.getPressure(reservoir());
        }

        public float maxPressure(){
            return ReservoirSystem.getPressureCapacity(reservoir());
        }

        public float pressureFrac() {
            return Mathf.clamp(pressure() / maxPressure());
        }
    }
}
