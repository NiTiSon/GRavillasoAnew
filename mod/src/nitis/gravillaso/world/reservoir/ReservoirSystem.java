package nitis.gravillaso.world.reservoir;

import arc.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.io.*;
import mindustry.io.SaveFileReader.*;
import mindustry.type.*;
import mindustry.world.*;
import nitis.gravillaso.*;

import java.io.*;
import java.util.*;

import static mindustry.Vars.*;

public class ReservoirSystem implements SaveFileReader.CustomChunk{
    public static final Seq<Liquid> types = Seq.with(); // maybe add information like maximum pressure/efficiency

    private static @Nullable Tiles syncTiles; //used for sync new maps
    public ReservoirSystem(){
        /* ResetEvent is enough, I suppose
        Events.on(WorldLoadEvent.class, e -> {
            reset();
            syncTiles = world.tiles; //clear previous map reservoir
        });
        */
        Events.on(ResetEvent.class, e -> {
            if(syncTiles != world.tiles){
                reset();
                syncTiles = world.tiles;
            }
        });

        SaveVersion.addCustomChunk("gr-reservoir", this);
    }

    public static void reset(){
        types.clear();
    }

    /** get current efficiency of {@code reservoir}. */
    public static float getPressure(int reservoir){
        if(reservoir >= types.size){
            return 0f; // invalid reservoir id, no logging, since this method is called very often
        }

        // TODO: track current pressure
        return 1f;
    }

    public static Liquid getReserviourLiquid(int reservoir){
        if(reservoir >= types.size){
            return Liquids.oil;
        }

        return types.get(reservoir);
    }

    @Override
    public boolean shouldWrite(){
        return types.size > 0;
    }

    @Override
    public void write(DataOutput stream) throws IOException{
        stream.writeByte(1);
        stream.writeInt(types.size);
        for(int i = 0; i < types.size; i++){
            stream.writeUTF(types.get(i).name); //item id is unstable between different mod versions, I suppose
        }
    }

    @Override
    public void read(DataInput stream) throws IOException{
        byte version = stream.readByte();
        int size = stream.readInt();
        types.clear();
        for(int i = 0; i < size; i++){
            String name = stream.readUTF();
            Liquid liquid = Vars.content.liquid(name); // not annotated as nullable, but in reality does
            if(liquid == null) liquid = Liquids.oil;
            types.add(liquid);
        }

        Log.debug("gr-reservoir read: @", types);
    }
}