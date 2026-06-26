package nitis.gravillaso;

import mindustry.ctype.Content;
import mindustry.mod.Mod;
import nitis.gravillaso.content.*;

public class GravillasoMod extends Mod {
    @Override
    public void loadContent() {
        GRItems.load();
        GRUnitTypes.load();
        GRBlocks.load();
        GRPlanets.load();
        GravilloTechTree.load();
    }

    public static boolean isRelated(Content content) {
        return content.minfo.mod != null && content.minfo.mod.name.equals("gr-anew");
    }
}
