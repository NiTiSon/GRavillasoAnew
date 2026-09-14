package nitis.gravillaso.core;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import nitis.gravillaso.graphics.*;
import nitis.gravillaso.world.reservoir.*;
import nitis.gravillaso.world.temperature.*;

import static mindustry.Vars.*;
import static nitis.gravillaso.GravillasoMod.*;

public class GrLogic implements ApplicationListener{
    public ThermalRenderer thermalRenderer;

    public GrLogic(){
        Events.on(StateChangeEvent.class, this::reset);
        Events.on(WorldLoadEvent.class, this::worldLoad);
    }

    @Override
    public void init(){
        temperatureSystem = new TemperatureSystem();
        reservoirSystem = new ReservoirSystem();
        grState = new GrGameState();
        thermalRenderer = new ThermalRenderer();
    }

    @Override
    public void update() {
        // to be filled
    }

    public void reset(StateChangeEvent args){
        grState = new GrGameState();
        Log.debug("GrLogic::reset");
    }

    public void worldLoad(WorldLoadEvent args){
        grState.rules = GrRules.getFrom(state.rules);
        Log.debug("GrLogic::worldLoad");
    }
}