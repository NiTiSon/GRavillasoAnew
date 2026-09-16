package nitis.gravillaso.world.blocks.production;

import arc.*;
import arc.graphics.g2d.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import nitis.gravillaso.graphics.*;
import nitis.gravillaso.world.blocks.environment.*;
import nitis.gravillaso.world.blocks.production.PressureBooster.*;

public class WellCollector extends Block{
    public DrawBlock drawer = new DrawDefault();

    public WellCollector(String name){
        super(name);
        update = true;
        solid = true;
        canOverdrive = false;
    }

    @Override
    public void setBars(){
        super.setBars();

        /*addBar("gr-pressure", (WellCollectorBuild build) -> new Bar(
        () -> Core.bundle.format("bar.gr-pressure", build.pressure(), build.maxPressure()),
        () -> GrPal.pressure,
        build::pressureFrac
        ));*/
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        return !(tile.getLinkedTilesAs(this, tempTiles).contains(t -> !(t.floor() instanceof WellBlock)));
    }

    @Override
    protected TextureRegion[] icons(){
        return drawer.icons(this);
    }

    public class WellCollectorBuild extends Building{
        @Override
        public void draw(){
            drawer.draw(this);
        }

        public int reservoir(){
            return tile.extraData;
        }
    }
}
