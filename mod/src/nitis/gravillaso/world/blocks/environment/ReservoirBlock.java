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
import arc.scene.ui.*;

import static mindustry.Vars.*;

public abstract class ReservoirBlock extends Floor{
    public final Point2[] offsets;

    public Block parent = Blocks.air;
    public Effect effect = Fx.ventSteam;
    public Color effectColor = Pal.vent;
    public float effectSpacing = 15f;
    public float spriteSpacing = tilesize;

    protected ReservoirBlock(String name, Point2[] offsets){
        super(name);
        this.offsets = offsets;
        variants = 2;
        saveData = true;
        editorConfigurable = true;
        saveConfig = true;
    }

    @Override
    public void buildEditorConfig(Table table){
        super.buildEditorConfig(table);

        table.left().top();
        table.table(Styles.black6, cont -> {
            Table rows = new Table().left().top();
            rebuildReservoirRows(rows);

            ScrollPane pane = new ScrollPane(rows, Styles.smallPane);
            pane.setScrollingDisabled(true, false);
            pane.setOverscroll(false, false);
            cont.add(pane).growX().maxHeight(Math.min(Math.max(ReservoirSystem.types.size, 2), 8) * 46f);
        }).growX().left().top();
    }

    void rebuildReservoirRows(Table rows){
        //TODO: add translation keys maybe?
        rows.clear();
        rows.left().top();

        if(ReservoirSystem.types.isEmpty()){
            rows.add("No reservoirs yet.", Color.gray).left().pad(4f).row();
        }

        for(int i = 0; i < ReservoirSystem.types.size; i++){
            int index = i;
            rows.row();
            rows.table(row -> {
                row.left();

                row.check(index + "", index == this.configIndex(), checked -> {
                    this.lastConfig = checked ? index : null;
                    rebuildReservoirRows(rows);
                }).padRight(8f);

                row.table(selector -> ItemSelection.buildTable(null, selector, content.liquids(),
                    () -> ReservoirSystem.getReserviourLiquid(index),
                    liquid -> { if(liquid != null) ReservoirSystem.types.set(index, liquid); },
                    false, 2, 4
                )).growX();

                row.button(Icon.trash, Styles.flati, () -> {
                    ReservoirSystem.types.remove(index);
                    rebuildReservoirRows(rows);
                }).size(40f).pad(4f);
            }).growX().padBottom(2f);
        }

        rows.row();
        rows.button("Add reservoir", Icon.add, Styles.cleart, () -> {
            ReservoirSystem.types.add(Liquids.oil);
            rebuildReservoirRows(rows);
        }).growX().left().padTop(4f);
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
            Draw.rect(variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, variantRegions.length - 1))], tile.worldx() - spriteSpacing, tile.worldy() - spriteSpacing);
        }
    }

    @Override
    public boolean updateRender(Tile tile){
        return checkAdjacent(tile);
    }

    @Override
    public boolean shouldIndex(Tile tile){
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

    public Liquid tileLiquid(Tile tile){
        return ReservoirSystem.getReserviourLiquid(tile.extraData);
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