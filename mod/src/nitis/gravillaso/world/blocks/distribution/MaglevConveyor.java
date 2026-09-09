package nitis.gravillaso.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.io.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import nitis.gravillaso.annotations.Annotations.*;

import static mindustry.Vars.*;

public class MaglevConveyor extends StackConveyor{
    public @Load("@name$-phase") TextureRegion phaseRegion;
    public @Load("@name$-phase-edge") TextureRegion phaseEdgeRegion;
    public @Load("@name$-connect") TextureRegion connectRegion;
    public TextureRegion uiPhaseIcon;

    public MaglevConveyor(String name){
        super(name);
        placeableLiquid = true;
        outputRouter = false;
    }

    protected boolean isPhaseValidTile(Tile t){
        return t.floor().isLiquid || !t.floor().placeableOn;
    }

    @Override
    public boolean blends(Tile tile, int rotation, int direction){
        //phase only forms a straight line, never side connections
        if(tile.build instanceof MaglevConveyorBuild b && b.phase) return direction == 0 || direction == 2;
        return super.blends(tile, rotation, direction);
    }

    @Override
    public void loadIcon(){
        super.loadIcon();

        uiPhaseIcon =
        Core.atlas.find(ContentType.block.name() + "-" + name + "-phase" + "-ui",
        Core.atlas.find(ContentType.block.name() + "-" + name + "-ui"));
    }

    public TextureRegion phaseIcon(){
        return phaseRegion != null ? phaseRegion : Core.atlas.find(name + "-phase");
    }

    public class MaglevConveyorBuild extends StackConveyorBuild{
        public boolean phase;

        @Override
        public TextureRegion getDisplayIcon(){
            if(!phase){
                return super.getDisplayIcon();
            }else{
                return uiPhaseIcon != null ? uiPhaseIcon : super.getDisplayIcon();
            }
        }

        @Override
        public void onProximityUpdate(){
            phase = isPhaseValidTile(tile);
            super.onProximityUpdate();
        }

        @Override
        public void updateTile(){
            super.updateTile();
        }

        @Override
        public void drawCached(){
            if(!phase){
                super.drawCached();
                return;
            }

            Draw.rect(phaseRegion, x, y, rotdeg());

            if(!(back() instanceof MaglevConveyorBuild back)){
                Draw.rect(phaseEdgeRegion, x, y, (rotation + 2) * 90);
            }else if(!back.phase){ // connection to the previous not-phase conveyor
                Draw.rect(connectRegion, x, y, (rotation + 2) * 90);
            }

            if(!(front() instanceof  MaglevConveyorBuild front)){
                Draw.rect(phaseEdgeRegion, x, y, rotation * 90);
            }else if(!front.phase){ // connection to the previous not-phase conveyor
                Draw.rect(connectRegion, x, y, rotation * 90);
            }

            /*for(int i = 0; i < 4; i++){
                if((blendprox & (1 << i)) == 0){
                    Draw.rect(phaseEdgeRegion, x, y, (rotation - i) * 90);
                }
            }*/
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.bool(phase);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            phase = read.bool();
        }
    }
}