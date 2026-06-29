package nitis.gravillaso;

import arc.Core;
import arc.Events;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import nitis.gravillaso.content.*;
import nitis.gravillaso.graphics.GRShaders;

public class GravillasoMod extends Mod {

    @Override
    public void loadContent() {
        GRItems.load();
        GRLiquids.load();
        GRUnitTypes.load();
        GRBlocks.load();
        GRPlanets.load();
        GRSectorPresets.load();
        GRWeathers.load();
        GravilloTechTree.load();

        if (!Vars.headless) {
            Events.on(EventType.FileTreeInitEvent.class, e -> Core.app.post(GRShaders::init));
            Events.on(EventType.DisposeEvent.class, e -> GRShaders.dispose());
        }
    }
}
