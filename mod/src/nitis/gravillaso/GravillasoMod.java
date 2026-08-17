package nitis.gravillaso;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.ctype.Content;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import nitis.gravillaso.annotations.Annotations.*;
import nitis.gravillaso.content.*;
import nitis.gravillaso.gen.*;

@SuppressWarnings("unused")
@EnsureLoad
public class GravillasoMod extends Mod{
    public static GravillasoMod instance;

    public GravillasoMod(){
        instance = this;
    }

    @Override
    public void loadContent(){
        GrItems.load();
        GrLiquids.load();
        GrBlocks.load();
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
    }
}