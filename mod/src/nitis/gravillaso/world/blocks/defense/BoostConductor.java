package nitis.gravillaso.world.blocks.defense;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import nitis.gravillaso.content.*;

import static mindustry.Vars.*;

public class BoostConductor extends Block{
    public float maxBoostThroughput = 2f;
    public DrawBlock drawer = new DrawMulti(new DrawDefault(), new DrawHeatOutput());
    public boolean splitBoost = false;

    public BoostConductor(String name){
        super(name);
        update = solid = rotate = true;
        group = BlockGroup.projectors;
        rotateDraw = false;
        canOverdrive = false;
        noUpdateDisabled = true;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(GrStat.speedThroughput, (int)(maxBoostThroughput * 100f) + "%");
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("boost", (BoostConductorBuild entity) -> new Bar(
        () -> Core.bundle.format("bar.boost", Mathf.round(Math.max((entity.boost() * 100), 0))),
        () -> Pal.accent,
        () -> entity.boost() / maxBoostThroughput));
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

    public class BoostConductorBuild extends Building implements DirectionalBoostBlock{
        public float boost = 0f;
        public float[] sideBoost = new float[4];
        public IntSet cameFrom = new IntSet();
        public long lastBoostUpdate = -1;

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
            updateBoost();
        }

        public void updateBoost(){
            if(lastBoostUpdate == state.updateId) return;
            lastBoostUpdate = state.updateId;
            boost = enabled ? calculateBoost(sideBoost, cameFrom) : 0f;

            for(var build : proximity){
                if(build != null && build.team == team && build.block.canOverdrive){
                    build.applyBoost(1f + boost, 2f);
                }
            }
        }

        public float calculateBoost(float[] sideBoost, @Nullable IntSet cameFrom){
            java.util.Arrays.fill(sideBoost, 0f);
            if(cameFrom != null) cameFrom.clear();

            float total = 0f;

            for(var build : proximity){
                if(build != null && build.team == team && build instanceof DirectionalBoostBlock booster){
                    boolean split = build.block instanceof BoostConductor cond && cond.splitBoost;
                    if(!build.block.rotate || (!split && (relativeTo(build) + 2) % 4 == build.rotation) || (split && relativeTo(build) != build.rotation)){
                        if(!(build instanceof BoostConductorBuild bc && bc.cameFrom.contains(id()))){
                            float diff = (Math.min(Math.abs(build.x - x), Math.abs(build.y - y)) / tilesize);
                            int contactPoints = Math.min((int)(block.size / 2f + build.block.size / 2f - diff), Math.min(build.block.size, block.size));

                            float add = booster.boost() / build.block.size * contactPoints;
                            if(split) add /= 3f;

                            sideBoost[Mathf.mod(relativeTo(build), 4)] += add;
                            total += add;
                        }

                        if(cameFrom != null){
                            cameFrom.add(build.id);
                            if(build instanceof BoostConductorBuild bc){
                                cameFrom.addAll(bc.cameFrom);
                            }
                        }

                        if(booster instanceof BoostConductorBuild cond){
                            cond.updateBoost();
                        }
                    }
                }
            }

            return Math.min(total, maxBoostThroughput);
        }

        @Override
        public float boost(){
            return boost;
        }

        @Override
        public float boostFrac(){
            return (boost / maxBoostThroughput) / (splitBoost ? 3f : 1);
        }
    }
}
