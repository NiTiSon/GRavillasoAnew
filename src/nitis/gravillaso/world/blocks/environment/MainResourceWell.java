package nitis.gravillaso.world.blocks.environment;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.Liquids;
import mindustry.entities.Effect;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import static mindustry.Vars.tilesize;

// 3x3 resource well, which WellPressuriser placed on.
public class MainResourceWell extends Floor{
    public static final Point2[] offsets = {
        new Point2(0, 0),
        new Point2(1, 0),
        new Point2(1, 1),
        new Point2(0, 1),
        new Point2(-1, 1),
        new Point2(-1, 0),
        new Point2(-1, -1),
        new Point2(0, -1),
        new Point2(1, -1),
    };

    public Block parent = Blocks.air;
    public Liquid resource = Liquids.oil;
    public Effect effect = Fx.ventSteam;
    public Color effectColor = Pal.vent;
    public float effectSpacing = 15f;

    static{
        for(var p : offsets){
            p.sub(1, 1);
        }
    }

    public MainResourceWell(String name) {
        super(name);
        variants = 2;
    }

    @Override
    public void drawMain(Tile tile){
        if(parent instanceof Floor floor){
            floor.drawMain(tile);
        }

        if(checkAdjacent(tile)){
            Draw.rect(variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, variantRegions.length - 1))], tile.worldx() - tilesize, tile.worldy() - tilesize);
        }
    }

    @Override
    public boolean updateRender(Tile tile){
        return checkAdjacent(tile);
    }

    @Override
    public boolean shouldIndex(Tile tile){
        return isCenterVent(tile);
    }

    public boolean isCenterVent(Tile tile){
        Tile topRight = tile.nearby(1, 1);
        return topRight != null && topRight.floor() == tile.floor() && checkAdjacent(topRight);
    }

    @Override
    public void renderUpdate(UpdateRenderState state){
        if(state.tile.nearby(-1, -1) != null && state.tile.nearby(-1, -1).block() == Blocks.air && (state.data += Time.delta) >= effectSpacing){
            effect.at(state.tile.x * tilesize - tilesize, state.tile.y * tilesize - tilesize, effectColor);
            state.data = 0f;
        }
    }

    //note that only the top right tile works for this; render order reasons.
    public boolean checkAdjacent(Tile tile){
        for(var point : offsets){
            Tile other = Vars.world.tile(tile.x + point.x, tile.y + point.y);
            if(other == null || other.floor() != this){
                return false;
            }
        }
        return true;
    }
}
