package nitis.gravillaso.world.reservoir;

import arc.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.io.*;
import mindustry.world.*;
import nitis.gravillaso.*;

import java.io.*;
import java.util.*;

import static mindustry.Vars.*;

/** Groups connected well tiles into reservoirs. Each reservoir is one "deposit" sharing a single pressure [0..1]. */
public class ReservoirSystem implements SaveFileReader.CustomChunk{
    public static final byte version = 1;

    public ReservoirSystem(){
        // theoretically we should update every time environment is changed
        // in campaign and custom maps static block usually do not change during gameplay, but this is possible
        Events.on(WorldLoadEvent.class, event -> rebuild());
        Events.on(SaveLoadEvent.class, event -> rebuild());
        SaveVersion.addCustomChunk("gr-reservoir", this);
    }

    public void rebuild(){
    }

    @Override
    public void write(DataOutput stream) throws IOException{

    }

    @Override
    public void read(DataInput stream) throws IOException{

    }
}