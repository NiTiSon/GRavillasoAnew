package nitis.gravillaso.core;

import arc.ApplicationListener;
import arc.Events;
import arc.util.Log;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.game.EventType.*;
import nitis.gravillaso.GravillasoMod;

import static mindustry.Vars.state;
import static nitis.gravillaso.GravillasoMod.*;

public class GRLogic implements ApplicationListener{

    private boolean wasInit;
    @Override
    public void init() {
        if (wasInit) return;

        wasInit = true;
        Events.on(StateChangeEvent.class, this::reset);
        Events.on(WorldLoadEvent.class, this::worldLoad);
    }

    @Override
    public void update() {
        // to be filled
    }

    public void reset(StateChangeEvent args){
        grState = new GRGameState();
        Log.info("GRLogic::reset");
    }

    public void worldLoad(WorldLoadEvent args){
        grState.rules = GRRules.getFrom(state.rules);
        Log.info("GRLogic::worldLoad");
    }
}
