package nitis.gravillaso;

import mindustry.mod.Mod;
import nitis.gravillaso.content.*;

public class GravillasoMod extends Mod {

    @Override
    public void loadContent() {
        GRUnitTypes.load();
        GRBlocks.load();
    }
}
