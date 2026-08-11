package nitis.gravillaso;

import arc.Core;
import arc.Events;
import arc.util.CommandHandler;
import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Player;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.CustomRulesDialog;
import nitis.gravillaso.content.*;
import nitis.gravillaso.core.GRGameState;
import nitis.gravillaso.core.GRLogic;
import nitis.gravillaso.ui.dialog.CustomRulesDialogExtension;
import nitis.gravillaso.world.temperature.TemperatureSystem;

public class GravillasoMod extends Mod{
    public static TemperatureSystem temperatureSystem;
    public static GRLogic grLogic;
    public static GRGameState grState;

    public GravillasoMod() {
        Core.app.addListener(grLogic = new GRLogic());
    }

    @Override
    public void init() {
        temperatureSystem = new TemperatureSystem();
        grState = new GRGameState();

        grLogic.init();

        CustomRulesDialogExtension.inject();
    }

    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.register("temperatureview", "[mode]", "Toggle the temperature debug overlay (on/off, no arg toggles).", (args, context) -> {
            if(args.length > 0 && !args[0].equals("on") && !args[0].equals("off")){
                if(context instanceof Player player){
                    player.sendMessage("[scarlet]Unknown mode '[lightgray]" + args[0] + "[scarlet]'. Use 'on', 'off' or no argument to toggle.");
                }
                return;
            }

            boolean on = args.length == 0 ? !TemperatureSystem.enableDebugView : args[0].equals("on");
            TemperatureSystem.enableDebugView = on;

            if(context instanceof Player player){
                player.sendMessage("[accent]Temperature overlay [lightgray]" + (on ? "enabled." : "disabled."));
            }
        });
    }

    @Override
    public void loadContent() {
        GRItems.load();
        GRLiquids.load();
        GRUnitTypes.load();
        GRBlocks.load();
        GRWeathers.load();
        GRSectorPresets.load();
        GRPlanets.load();
        GravilloTechTree.load();
    }
}