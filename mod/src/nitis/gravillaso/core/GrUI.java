package nitis.gravillaso.core;

import mindustry.*;
import nitis.gravillaso.world.temperature.*;

public class GrUI{
    /** Injects new menus into vanilla UI */
    public static void inject(){
        ClientLauncher.runOnClientLoad(() -> {
            Vars.ui.settings.dev.checkPref("gr-showtemperature", false, (value) -> TemperatureSystem.enableDebugView = value);
        });
    }
}
