package nitis.gravillaso;

import arc.*;
import mindustry.*;
import mindustry.ctype.Content;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import nitis.gravillaso.annotations.Annotations.*;
import nitis.gravillaso.content.*;
import nitis.gravillaso.core.GrLogic;
import nitis.gravillaso.core.GrGameState;
import nitis.gravillaso.gen.*;

@SuppressWarnings("unused")
@EnsureLoad
public class GravillasoMod extends Mod{
    public static GravillasoMod instance;

    public static GrLogic grLogic;
    public static GrGameState grState;

    public GravillasoMod(){
        instance = this;

        Core.app.addListener(grLogic = new GrLogic());
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
        return content.minfo.mod.main == instance;
    }

    private void regionRegistry(Content content){
        if (!isRelated(content)) return;

        // this class below is auto-generated
        GravillasoContentRegionRegistry.load(content);
    }

    @Override
    public void init(){
        Events.on(ContentInitEvent.class, event -> {
            Vars.content.each(this::regionRegistry);
        });

        // TODO: temperatureSystem = new TemperatureSystem();
        grState = new GrGameState();

        grLogic.init();

        // TODO: CustomRulesDialogExtension.inject();
    }
}