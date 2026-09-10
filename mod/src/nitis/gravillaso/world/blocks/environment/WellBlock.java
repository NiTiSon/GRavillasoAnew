package nitis.gravillaso.world.blocks.environment;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import nitis.gravillaso.world.reservoir.*;

import static mindustry.Vars.*;

//can't use an overlay for this because it spans multiple tiles
public class WellBlock extends Floor{
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
    public Effect effect = Fx.ventSteam;
    public Color effectColor = Pal.vent;
    public float effectSpacing = 15f;

    static{
        for(var p : offsets){
            p.sub(1, 1);
        }
    }

    public WellBlock(String name){
        super(name);
        variants = 2;
        saveData = true;
        editorConfigurable = true;
        saveConfig = true;
    }

    @Override
    public void buildEditorConfig(Table table){
        super.buildEditorConfig(table);
        showReservoirEdit(table);
    }

    public static void showReservoirEdit(Table table){
        // save-wide editor:
        // items:
        // * selector_box, index, liquid_selector
        for(int i = 0; i < ReservoirSystem.types.size; i++){
            Liquid liquid = ReservoirSystem.types.get(i);
            table.image(liquid.uiIcon);
            table.labelWrap(liquid.localizedName);
        }
        table.button(Icon.add, () -> {
            ReservoirSystem.types.add(Liquids.oil);
        });
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
        return isCenterWell(tile);
    }

    public boolean isCenterWell(Tile tile){
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