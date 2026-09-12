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
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.environment.*;
import nitis.gravillaso.world.reservoir.*;

import static mindustry.Vars.*;

// copy of copy of SteamVent
public class WellBlock extends Floor implements ReservoirBlock{
    public static final Point2[] offsets = {
        new Point2(-1, -1),
        new Point2(0, -1),
        new Point2(-1, 0),
        new Point2(0, 0),
    };

    public Block parent = Blocks.air;
    public Effect effect = Fx.ventSteam;
    public Color effectColor = Pal.vent;
    public float effectSpacing = 15f;

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
        FissureBlock.showReservoirEdit(this, table);
    }

    public int configIndex(){
        return lastConfig instanceof Integer c && c >= 0 && c < ReservoirSystem.types.size ? c : -1;
    }

    @Override
    public Object getConfig(Tile tile){
        return tile.extraData;
    }

    @Override
    public void onPicked(Tile tile){
        lastConfig = ReservoirSystem.types.isEmpty() ? null : Math.min(tile.extraData, ReservoirSystem.types.size - 1);
    }

    @Override
    public void editorPicked(Tile tile){
        onPicked(tile);
    }

    @Override
    public void placeEnded(Tile tile, @Nullable Unit builder, int rotation, @Nullable Object config){
        if(config instanceof Integer i){
            tile.extraData = i;
        }
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