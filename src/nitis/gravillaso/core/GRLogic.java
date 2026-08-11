package nitis.gravillaso.core;

import arc.ApplicationListener;
import arc.Events;
import arc.util.Log;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.game.EventType.*;
import nitis.gravillaso.GravillasoMod;

import static nitis.gravillaso.GravillasoMod.*;

public class GRLogic implements ApplicationListener {
    @Override
    public void init() {
        Events.on(StateChangeEvent.class, this::reset);
    }

    @Override
    public void update() {
        // to be filled
    }

    public void reset(StateChangeEvent args) {
        grState = new GRGameState();
    }
}
