package nitis.gravillaso.world.blocks.defense;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import nitis.gravillaso.world.draw.*;

import static mindustry.Vars.*;

public class Booster extends Block{
    public float additiveBoost = 0.5f;
    public DrawBlock drawer = new DrawMulti(
    new DrawDefault(),
    new DrawHeatOutput(),
    new DrawOverdriveTop(),
    new DrawDirectionalPulse(Pal.redLight)
    );
    public float warmupRate = 0.05f;

    public Booster(String name){
        super(name);
        solid = update = true;
        rotateDraw = false;
        rotate = true;
        drawArrow = true;
        canOverdrive = false;
        hasPower = true;
        group = BlockGroup.projectors;
        envEnabled |= Env.space;
        ambientSound = Sounds.loopCircuit;
        ambientSoundVolume = 0.7f;
    }

    @Override
    public boolean outputsItems(){
        return false;
    }

    @Override
    public void setStats(){
        //stats.timePeriod = useTime;
        super.setStats();

        stats.add(Stat.speedIncrease, "+" + (int)(additiveBoost * 100f) + "%");
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("boost", (BoosterBuild entity) -> new Bar(
        () -> Core.bundle.format("bar.boost", Mathf.round(Math.max((entity.boost() * 100), 0))),
        () -> Pal.accent,
        () -> entity.boost() / additiveBoost));
    }

    @Override
    public void load(){
        super.load();

        drawer.load(this);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons(){
        return drawer.finalIcons(this);
    }

    public class BoosterBuild extends Building implements DirectionalBoostBlock{
        public float boost;

        @Override
        public void draw(){
            drawer.draw(this);
        }

        @Override
        public void drawLight(){
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public void updateTile(){
            boost = Mathf.approachDelta(boost, additiveBoost * efficiency, warmupRate * delta());

            for(var build : proximity){
                if(build != null && build.team == team && build.block.canOverdrive){
                    int contact = DirectionalBoostBlock.contactPoints(self(), build);
                    if(contact > 0 && relativeTo(build) == rotation){
                        float ratio = (float)contact / Math.min(build.block.size, block.size);
                        build.applyBoost(1f + (boost * ratio), 2f);
                    }
                }
            }
        }

        @Override
        public float warmup(){
            return boostFrac();
        }

        @Override
        public float boost(){
            return boost;
        }

        @Override
        public float boostFrac(){
            return boost / additiveBoost;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(boost);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            boost = read.f();
        }
    }
}
