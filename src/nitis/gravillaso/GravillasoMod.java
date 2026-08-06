package nitis.gravillaso;

import mindustry.mod.Mod;
import nitis.gravillaso.content.*;

public class GravillasoMod extends Mod{

    @Override
    public void loadContent() {
        GRItems.load();
        GRUnitTypes.load();
        GRBlocks.load();
        GRWeathers.load();
        GRSectorPresets.load();
        GRPlanets.load();
        GravilloTechTree.load();
    }
}
