package nitis.gravillaso.core;

import arc.ApplicationListener;
import arc.Events;
import arc.util.Log;
import mindustry.game.EventType.*;

import static mindustry.Vars.state;
import static nitis.gravillaso.GravillasoMod.*;

public class GrLogic implements ApplicationListener{

    public GrLogic(){
        Events.on(StateChangeEvent.class, this::reset);
        Events.on(WorldLoadEvent.class, this::worldLoad);
    }

    @Override
    public void update() {
        // to be filled
    }

    public void reset(StateChangeEvent args){
        grState = new GrGameState();
        Log.info("GRLogic::reset");
    }

    public void worldLoad(WorldLoadEvent args){
        grState.rules = GrRules.getFrom(state.rules);
        Log.info("GRLogic::worldLoad");
    }
}