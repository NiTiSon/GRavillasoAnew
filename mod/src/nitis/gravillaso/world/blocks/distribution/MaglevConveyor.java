package nitis.gravillaso.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import nitis.gravillaso.annotations.Annotations.*;

import static mindustry.Vars.*;

/**
 * Stack conveyor that can fling items across a gap of unplaceable tiles (e.g. water) to
 * another maglev conveyor facing the same way, up to {@link #maxGap} tiles ahead. A beam
 * is drawn between linked conveyors.
 *
 * <pre>
 * xxwwwxx   w - water, x - ground, = - maglev conveyor
 * ==www==   both conveyors face the same way; the left one flies items
 * xxwwwxx   into the right one's input across the gap.
 * </pre>
 */
public class MaglevConveyor extends StackConveyor{
    public @Load("@name$-bridge") TextureRegion bridgeRegion;

    /** Max tiles between two connected conveyors. */
    public int maxGap = 15;

    public MaglevConveyor(String name){
        super(name);
        outputRouter = false;
    }

    @Override
    public boolean blends(Tile tile, int rotation, int direction){
        //bridge senders tile as through-conveyors so their state (and blending) stays correct
        if(direction == 0 && tile.build instanceof MaglevConveyorBuild b && b.currentTarget() != null) return true;
        return super.blends(tile, rotation, direction);
    }

    public class MaglevConveyorBuild extends StackConveyorBuild{
        /** Position of the far receiver this conveyor flings items to, or -1. Pinned once set, like an ItemBridge link. */
        public int bridgePos = -1;

        /** The live codirectional receiver at {@link #bridgePos}, or null if that position no longer holds one. */
        public MaglevConveyorBuild currentTarget(){
            if(bridgePos == -1) return null;
            Building b = world.build(bridgePos);
            return b instanceof MaglevConveyorBuild m && m.isValid() && m.team == team && m.rotation == rotation ? m : null;
        }

        @Override
        public void onProximityUpdate(){
            recalcBridge();
            super.onProximityUpdate();
            wakeUpstream();
        }

        /** Whether a maglev conveyor cannot sit on this empty tile, i.e. it is a natural gap. */
        protected boolean isBridgeGap(Tile t){
            return t.block() != Blocks.air || t.solid() || t.floor().isLiquid;
        }

        /** Scans ahead once, at placement, for a codirectional maglev conveyor separated by unplaceable tiles. */
        protected void recalcBridge(){
            if(bridgePos != -1) return; //once linked, the link is pinned and never re-hunted
            if(super.front() instanceof StackConveyorBuild) return; //adjacent line, no bridge needed

            int dx = Geometry.d4x(rotation), dy = Geometry.d4y(rotation);
            for(int i = 1; i <= maxGap + 1; i++){
                Tile t = world.tile(tile.x + dx * i, tile.y + dy * i);
                if(t == null) return;

                if(t.build != null){
                    //first building ends the search; only a live codirectional maglev becomes the target
                    if(t.build instanceof MaglevConveyorBuild b && b.isValid() && b.team == team && b.rotation == rotation){
                        bridgePos = b.pos();
                    }
                    return;
                }

                //empty tile in between must be a gap, otherwise a normal line would fit there
                if(i <= maxGap && !isBridgeGap(t)) return;
            }
        }

        /** Reaches backward at placement to pin the sender behind this conveyor (across a gap) to this one. */
        protected void wakeUpstream(){
            if(super.back() instanceof StackConveyorBuild) return; //adjacent line behind, no bridge needed

            int dx = Geometry.d4x(rotation + 2), dy = Geometry.d4y(rotation + 2);
            for(int i = 1; i <= maxGap + 1; i++){
                Tile t = world.tile(tile.x + dx * i, tile.y + dy * i);
                if(t == null) return;

                if(t.build != null){
                    if(t.build instanceof MaglevConveyorBuild b && b.isValid() && b.team == team && b.rotation == rotation && b.bridgePos != pos()){
                        b.bridgePos = pos();
                        b.onProximityUpdate();
                    }
                    return;
                }

                if(i <= maxGap && !isBridgeGap(t)) return;
            }
        }

        @Override
        public Building front(){
            MaglevConveyorBuild target = currentTarget();
            return target != null ? target : super.front();
        }

        @Override
        public void draw(){
            super.draw();

            MaglevConveyorBuild target = currentTarget();
            if(target == null) return;

            int dx = Geometry.d4x(rotation), dy = Geometry.d4y(rotation);
            float x1 = x + dx * tilesize / 2f, y1 = y + dy * tilesize / 2f;
            float x2 = target.x - dx * tilesize / 2f, y2 = target.y - dy * tilesize / 2f;

            Draw.z(Layer.blockUnder);

            if(bridgeRegion.found()){
                Draw.rect(bridgeRegion, (x1 + x2) / 2f, (y1 + y2) / 2f, Mathf.dst(x1, y1, x2, y2), tilesize, Angles.angle(x1, y1, x2, y2));
            }else{
                Draw.color(glowColor, 0.75f);
                Lines.stroke(tilesize);
                Lines.line(x1, y1, x2, y2);
                Draw.reset();
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(bridgePos);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            bridgePos = read.i();
        }
    }
}