package nitis.gravillaso.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.meta.*;

public class DistributionLine extends Block implements Autotiler{
    protected static final int
    stateMove = 0, // move items forward
    stateLoad = 1, // load items
    stateFork = 2, // when connected to several lines, works like router
    stateJunction = 3, // works like junction on interchange
    stateUnload = 4; // when last element

    public float speed = 0f;
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
    public void setStats(){
        super.setStats();

        stats.add(Stat.itemsMoved, Mathf.round(itemCapacity * speed * 60), StatUnit.itemsSecond);
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
        return false;
    }

    public class DistributionLineBuild extends Building{
        @Override
        public void drawCached(){
        }

        @Override
        public void draw(){
            Draw.z(Layer.block - 0.1f);

            // TODO: draw lines
            // TODO: draw item stack
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
    }
}
