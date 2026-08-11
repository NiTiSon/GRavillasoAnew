package nitis.gravillaso;

import arc.util.CommandHandler;
import mindustry.gen.Player;
import mindustry.mod.Mod;
import nitis.gravillaso.content.*;
import nitis.gravillaso.world.temperature.TemperatureSystem;

public class GravillasoMod extends Mod{

    @Override
    public void init() {
        TemperatureSystem.init();
    }

    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Object>register("temperatureview", "[mode]", "Toggle the temperature debug overlay (on/off, no arg toggles).", (args, context) -> {
            if(args.length > 0 && !args[0].equals("on") && !args[0].equals("off")){
                if(context instanceof Player player){
                    player.sendMessage("[scarlet]Unknown mode '[lightgray]" + args[0] + "[scarlet]'. Use 'on', 'off' or no argument to toggle.");
                }
                return;
            }

            boolean on = args.length == 0 ? !TemperatureSystem.debugDraw : args[0].equals("on");
            TemperatureSystem.debugDraw = on;

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
