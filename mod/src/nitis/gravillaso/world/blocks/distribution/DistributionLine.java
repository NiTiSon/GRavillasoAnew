package nitis.gravillaso.world.blocks.distribution;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.distribution.Conveyor.*;
import mindustry.world.meta.*;
import nitis.gravillaso.annotations.Annotations.*;

import static mindustry.Vars.*;

public class DistributionLine extends Block implements Autotiler{
    protected static final int
    stateMove = 0, // move items forward (move forward)
    stateLoad = 1, // load items (input from building)
    stateFork = 2, // when several lines come from current block, this block become a fork a.k.a. router (1+ input, 2+ output)
    stateJunction = 3, // works like move, but alternately changing direction (has 2 accept sides, and 2 output; only left ↔ right, top ↔ bottom)
    stateUnload = 4;

    public @Load(value = "@name$-bottom") TextureRegion bottomRegion;
    public @Load("@name$-edge") TextureRegion edgeRegion;
    public @Load("@name$-stack") TextureRegion stackRegion;

    public float speed = 0f;
    public float recharge = 3f;
    public boolean outputRouter = false;

    public Color lineColor = Pal.accent;
    public Effect loadEffect = Fx.conveyorPoof;
    public Effect unloadEffect = Fx.conveyorPoof;

    public DistributionLine(String name){
        super(name);

        rotate = true;
        update = true;
        group = BlockGroup.transportation;
        hasItems = true;
        itemCapacity = 10;
        conveyorPlacement = true;
        underBullets = true;
        priority = TargetPriority.transport;
        drawCached = true;
        buildingCacheLayer = BuildingCacheLayer.under;

        ambientSound = Sounds.loopConveyor;
        ambientSoundVolume = 0.004f;
    }

    @Override
    public String getDisplayName(Tile tile){
        if(tile.build instanceof DistributionLineBuild build){
            return super.getDisplayName(tile) + " " + build.state + " " + build.blendprox;
        }

        return super.getDisplayName(tile);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.itemsMoved, Mathf.round(itemCapacity * speed * 60), StatUnit.itemsSecond);
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
        if(tile.build instanceof DistributionLineBuild b){
            int state = b.state;
            if(state == stateLoad){ //standard conveyor mode
                return otherblock.outputsItems() && lookingAtEither(tile, rotation, otherx, othery, otherrot, otherblock);
            }else if(state == stateUnload && !outputRouter){ //router mode
                return otherblock.acceptsItems &&
                (!otherblock.noSideBlend || lookingAtEither(tile, rotation, otherx, othery, otherrot, otherblock)) &&
                (notLookingAt(tile, rotation, otherx, othery, otherrot, otherblock) ||
                (otherblock instanceof DistributionLine && facing(otherx, othery, otherrot, tile.x, tile.y))) &&
                !(world.build(otherx, othery) instanceof DistributionLineBuild s && s.state == stateUnload) &&
                !(world.build(otherx, othery) instanceof DistributionLineBuild s2 && s2.state == stateMove &&
                !facing(otherx, othery, otherrot, tile.x, tile.y));
            }else if(state == stateFork || state == stateJunction){
                return otherblock.outputsItems() && (otherblock instanceof DistributionLine || lookingAtEither(tile, rotation, otherx, othery, otherrot, otherblock));
            }
        }

        if(otherblock instanceof DistributionLine && world.build(otherx, othery) instanceof DistributionLineBuild other && (other.state == stateFork || other.state == stateJunction)){
            return otherblock.outputsItems();
        }

        return otherblock.outputsItems() && blendsArmored(tile, rotation, otherx, othery, otherrot, otherblock) && otherblock instanceof DistributionLine;
    }

    @Override
    public boolean rotatedOutput(int x, int y){
        Building tile = world.build(x, y);
        if(tile instanceof DistributionLineBuild s){
            return s.state != stateUnload;
        }
        return super.rotatedOutput(x, y);
    }

    public class DistributionLineBuild extends Building{
        public int state, blendprox;

        public int link = -1;
        public float cooldown;
        public Item lastItem;

        boolean proxUpdating = false;

        @Override
        public void drawCached(){
            Draw.rect(bottomRegion, x, y, rotdeg());

            for(int i = 0; i < 4; i++){
                if((blendprox & (1 << i)) == 0){
                    Draw.rect(edgeRegion, x, y, (rotation - i) * 90);
                }
            }

            //draw inputs
            if(state == stateLoad){
                for(int i = 0; i < 4; i++){
                    int dir = Mathf.mod(rotation - i, 4);
                    var near = nearby(dir);
                    if((blendprox & (1 << i)) != 0 && i != 0 && near != null && !near.block.squareSprite){
                        Draw.rect(sliced(bottomRegion, SliceMode.bottom), x + Geometry.d4x(dir) * tilesize*0.75f, y + Geometry.d4y(dir) * tilesize*0.75f, (float)(dir*90));
                    }
                }
            }else if(state == stateUnload){ //front unload
                //TOOD hacky front check
                if((blendprox & (1)) != 0 && front() != null && !front().block.squareSprite){
                    Draw.rect(sliced(bottomRegion, SliceMode.top), x + Geometry.d4x(rotation) * tilesize*0.75f, y + Geometry.d4y(rotation) * tilesize*0.75f, rotation * 90f);
                }
            }

            Lines.stroke(1f, lineColor);

            final float halfSize = tilesize / 2f;
            final float revPadding = tilesize / 4f;
            if(state != stateFork){
                for(int i = 0; i < 4; i++){
                    int dir = Mathf.mod(rotation - i, 4);
                    float xOffset = Geometry.d4x(dir) * halfSize;
                    float yOffset = Geometry.d4y(dir) * halfSize;
                    if((blendprox & (1 << i)) != 0){
                        Lines.line(x, y, x + xOffset, y + yOffset);
                    }
                }
            }else{
                for(int i = 0; i < 4; i++){
                    int dir = Mathf.mod(rotation - i, 4);
                    float xOffset = Geometry.d4x(dir) * halfSize;
                    float yOffset = Geometry.d4y(dir) * halfSize;
                    float padX = Geometry.d4x(dir) * (revPadding + 0.2f);
                    float padY = Geometry.d4y(dir) * (revPadding + 0.2f);
                    if((blendprox & (1 << i)) != 0){
                        Lines.line(x + padX, y + padY, x + xOffset, y + yOffset);
                    }
                }
            }

            switch(state){
                case stateFork -> {
                    Lines.beginLine();
                    int lines = 0;
                    for(int i = 0; i < 4; i++){
                        int dir = Mathf.mod(rotation - i, 4);
                        float xOffset = Geometry.d4x(dir) * revPadding;
                        float yOffset = Geometry.d4y(dir) * revPadding;
                        if((blendprox & (1 << i)) != 0){
                            Lines.linePoint(x + xOffset, y + yOffset);
                            lines++;
                        }
                    }
                    Lines.endLine(lines == 4);
                    break;
                }
                case stateJunction -> {
                    Lines.line(x - revPadding, y - revPadding, x + revPadding, y + revPadding);
                    Lines.line(x + revPadding, y - revPadding, x - revPadding, y + revPadding);
                    break;
                }
                case stateLoad, stateUnload -> {
                    Draw.rect("white", x, y, 2f, 2f, 45f);
                }
            }
            Draw.color();
        }

        @Override
        public void draw(){
            Draw.z(Layer.block - 0.1f);

            // TODO: draw lines
            // TODO: draw item stack
        }

        @Override
        public void dropped(){
            super.dropped();
            var prev = Geometry.d4[(rotation + 2) % 4];
            if(items.any()){
                link = Point2.pack(tile.x + prev.x, tile.y + prev.y);
                cooldown = 0f;
            }else{
                link = -1;
            }
        }

        @Override
        public void drawCracks(){
            Draw.z(Layer.block - 0.15f);
            super.drawCracks();
        }

        @Override
        public void payloadDraw(){
            Draw.rect(block.fullIcon, x, y);
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            recache();

            int lastState = state;

            state = stateMove;

            int[] bits = buildBlending(tile, rotation, null, true);
            if(bits[0] == 0 && blends(tile, rotation, 0) && (!blends(tile, rotation, 2) || back() instanceof DistributionLineBuild b && b.state == stateUnload)) state = stateLoad;  // a 0 that faces into a conveyor with none behind it
            if(outputRouter && bits[0] == 0 && !blends(tile, rotation, 0) && blends(tile, rotation, 2)) state = stateUnload; // a 0 that faces into none with a conveyor behind it
            if(!outputRouter && !(front() instanceof DistributionLineBuild)) state = stateUnload; // a 0 that faces into none with a conveyor behind it

            int lines = 0, inputs = 0, inputMask = 0;
            for(int i = 0; i < 4; i++){
                if(nearby(i) instanceof DistributionLineBuild line && line.team == team){
                    lines++;
                    if(line.front() == this){
                        inputs++;
                        inputMask |= 1 << i;
                    }
                }
            }

            int outputs = lines - inputs;
            boolean oppositeInputs = (inputMask & 0b0101) == 0b0101 || (inputMask & 0b1010) == 0b1010;
            if(inputs >= 2 && outputs >= 2 && !oppositeInputs){
                state = stateJunction;
            }else if(inputs >= 1 && outputs >= 2){
                state = stateFork;
            }

            if(!headless){
                blendprox = 0;

                for(int i = 0; i < 4; i++){
                    if(blends(tile, rotation, i) && (state != stateUnload || outputRouter || i == 0 || nearby(Mathf.mod(rotation - i, 4)) instanceof DistributionLineBuild)){
                        blendprox |= (1 << i);
                    }
                }
            }

            if(state == stateLoad){
                for(Building near : proximity){
                    if(near instanceof DistributionLineBuild && near.front() == this){
                        state = stateMove;
                        break;
                    }
                }
            }

            if(state != lastState){
                proxUpdating = true;
                for(Building near : proximity){
                    if(!(near instanceof DistributionLineBuild b && b.proxUpdating && b.state != stateUnload)){
                        near.onProximityUpdate();
                    }
                }
                proxUpdating = false;
            }
        }

        @Override
        public boolean canUnload(){
            return state != stateLoad;
        }

        @Override
        public void overwrote(Seq<Building> builds){
            if(builds.first() instanceof ConveyorBuild build){
                Item item = build.items.first();
                if(item != null){
                    handleStack(item, build.items.get(item), null);
                }
            }
        }

        @Override
        public boolean shouldAmbientSound(){
            return false; //has no moving parts;
        }

        protected void poofIn(){
            link = tile.pos();
            loadEffect.at(this);
        }

        protected void poofOut(){
            unloadEffect.at(this);
            link = -1;
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            if(items.any() && !items.has(item)) return 0;
            return super.acceptStack(item, amount, source);
        }

        @Override
        public void handleItem(Building source, Item item){
            if(items.empty() && tile != null){
                poofIn();
            }

            super.handleItem(source, item);
            lastItem = item;
        }

        @Override
        public void handleStack(Item item, int amount, @Nullable Teamc source){
            if(amount <= 0) return;
            if(items.empty() && tile != null){
                poofIn();
            }

            super.handleStack(item, amount, source);
            lastItem = item;
        }

        @Override
        public int removeStack(Item item, int amount){
            try{
                return super.removeStack(item, amount);
            }finally{
                if(items.empty()){
                    poofOut();
                }
            }
        }

        @Override
        public void itemTaken(Item item){
            if(items.empty()){
                poofOut();
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            if (this == source) {
                return items.total() < itemCapacity && (!items.any() || items.has(item));
            }

            return cooldown <= recharge - 1f
            && state == stateLoad
            && (!items.any() || items.has(item))
            && items.total() < getMaximumAccepted(item)
            && source != front();
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(link);
            write.f(cooldown);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            link = read.i();
            cooldown = read.f();
            lastItem = items.first();
        }
    }
}
