package nitis.gravillaso;

import arc.*;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.util.Log;
import mindustry.*;
import mindustry.ctype.Content;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.world.Block;
import nitis.gravillaso.annotations.Annotations.*;
import nitis.gravillaso.content.*;
import nitis.gravillaso.core.GrLogic;
import nitis.gravillaso.core.GrGameState;
import nitis.gravillaso.gen.*;
import nitis.gravillaso.world.temperature.TemperatureSystem;

@SuppressWarnings("unused")
@EnsureLoad
public class GravillasoMod extends Mod{
    public static GravillasoMod instance;

    public static TemperatureSystem temperatureSystem;
    public static GrLogic grLogic;
    public static GrGameState grState;

    public GravillasoMod(){
        instance = this;

        Core.app.addListener(grLogic = new GrLogic());

        Events.on(ContentInitEvent.class, event -> {
            Vars.content.each(this::regionRegistry);
            resolveBlockColors();
        });
    }

    @Override
    public void loadContent(){
        GrItems.load();
        GrLiquids.load();
        GrUnitTypes.load();
        GrBlocks.load();
        GrWeathers.load();
        GrPlanets.load();
        GravilloTechTree.load();
    }

    public static boolean isRelated(Content content){
        return content.minfo.mod != null && content.minfo.mod.main == instance;
    }

    private void regionRegistry(Content content){
        if (!isRelated(content)) return;

        // this class below is auto-generated
        GravillasoContentRegionRegistry.load(content);
    }

    @Override
    public void init(){
        temperatureSystem = new TemperatureSystem();
        grState = new GrGameState();

        grLogic.init();

        // TODO: CustomRulesDialogExtension.inject();
    }

    /**
     * Reads block_colors.png in mod sprites
     * Since the pregenerated set to true, we must set block color explicitly
     * Should improve startup time
     */
    private void resolveBlockColors(){
        Log.debug("GravillasoMod::resolveBlockColors - init");
        var mod = Vars.mods.getMod(getClass());
        if(mod == null) return;

        Fi file = mod.root.child("sprites").child("block_colors.png");
        if(!file.exists()){
            Log.errTag("Gr", "block_colors was not found in sprites directory");
            return;
        }

        Pixmap pixmap;
        try{
            pixmap = new Pixmap(file);
        }catch(Throwable t){
            Log.errTag("Gr", "Failed to read block_colors: " + t);
            return;
        }

        int colorMapped = 0;
        try{
            int index = 0;
            for(Block block : Vars.content.blocks()){
                if(!isRelated(block)) continue;
                if(index >= pixmap.width){
                    Log.warn("GravillasoMod::resolveBlockColors - index out of bounds for @", block.name);
                }

                int color = pixmap.get(index++, 0);
                if(color == 0 || color == 255) continue;

                block.mapColor.rgba8888(color);
                block.squareSprite = block.mapColor.a > 0.5f;
                block.mapColor.a = 1f;
                block.hasColor = true;
                colorMapped++;
            }
        }finally{
            pixmap.dispose();
            Log.debug("GravillasoMod::resolveBlockColors - mapped @ block colors", colorMapped);
        }
    }
}