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

//modification of SteamVent class
public class FissureBlock extends Floor implements ReservoirBlock{
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

    public FissureBlock(String name){
        super(name);
        variants = 2;
        saveData = true;
        editorConfigurable = true;
        saveConfig = true;
    }

    @Override
    public void buildEditorConfig(Table table){
        super.buildEditorConfig(table);
        showReservoirEdit(this, table);
    }

    public static <T extends Floor & ReservoirBlock> void showReservoirEdit(T floor, Table table){
        table.left().top();
        table.table(Styles.black6, rows -> {
            rows.left().top();
            rebuildReservoirRows(floor, rows);
        }).growX().left().top();
    }

    static <T extends Floor & ReservoirBlock> void rebuildReservoirRows(T floor, Table rows){
        // TODO: add translation keys maybe?
        rows.clear();
        rows.left().top();

        if(ReservoirSystem.types.isEmpty()){
            rows.add("No reservoirs yet, add one.", Color.gray).left().pad(4f).row();
        }

        for(int i = 0; i < ReservoirSystem.types.size; i++){
            int index = i;
            rows.row();
            rows.table(row -> {
                row.left();

                row.check(index + "", index == floor.configIndex(), checked -> {
                    floor.lastConfig = checked ? index : null;
                    rebuildReservoirRows(floor, rows);
                }).padRight(8f);

                row.table(selector -> ItemSelection.buildTable(null, selector, content.liquids(),
                    () -> reservoirLiquid(index),
                    liquid -> { if(liquid != null) ReservoirSystem.types.set(index, liquid); },
                    false, 2, 4
                )).growX();

                row.button(Icon.trash, Styles.flati, () -> {
                    ReservoirSystem.types.remove(index);
                    rebuildReservoirRows(floor, rows);
                }).size(40f).pad(4f);
            }).growX().padBottom(2f);
        }

        rows.row();
        rows.button("Add reservoir", Icon.add, Styles.cleart, () -> {
            ReservoirSystem.types.add(Liquids.oil);
            rebuildReservoirRows(floor, rows);
        }).growX().left().padTop(4f);
    }

    public int configIndex(){
        return lastConfig instanceof Integer c && c >= 0 && c < ReservoirSystem.types.size ? c : -1;
    }

    static Liquid reservoirLiquid(int index){
        return index < ReservoirSystem.types.size ? ReservoirSystem.types.get(index) : null;
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
    public String getDisplayName(Tile tile){
        Liquid liquid = tileLiquid(tile);
        return liquid == null ? super.getDisplayName(tile) : liquid.localizedName;
    }

    @Override
    public TextureRegion getDisplayIcon(Tile tile){
        Liquid liquid = tileLiquid(tile);
        return liquid == null ? super.getDisplayIcon(tile) : liquid.uiIcon;
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