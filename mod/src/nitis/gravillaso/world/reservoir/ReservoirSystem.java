package nitis.gravillaso.world.reservoir;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.world.*;
import nitis.gravillaso.world.blocks.production.*;

import java.io.*;

import static mindustry.Vars.*;

public class ReservoirSystem implements SaveFileReader.CustomChunk{
    public static Liquid[] types = {}; // maybe add information like maximum pressure/efficiency
    public static final IntMap<PressureProducer> producers = new IntMap<>(8);

    private static @Nullable Tiles syncTiles; //used for sync new maps
    public ReservoirSystem(){
        Events.on(ResetEvent.class, e -> {
            if(syncTiles != world.tiles){
                reset();
                syncTiles = world.tiles;
            }
        });

        SaveVersion.addCustomChunk("gr-reservoir", this);
    }

    public static void reset(){
        types = new Liquid[]{};
    }

    public static void addProducer(PressureProducer p){
        producers.put(p.reservoir(), p);
    }
    public static void removeProducer(PressureProducer p){
        producers.remove(p.reservoir());
    }

    public static int addReservoir(Liquid liquid){
        int id = types.length;

        Liquid[] newTypes = new Liquid[types.length + 1];
        System.arraycopy(types, 0, newTypes, 0, types.length);
        types = newTypes;
        newTypes[id] = liquid;

        return id;
    }

    public static void removeReservoir(int reservoir){
        if(reservoir >= types.length){
            Log.err("Tried to remove reservoir with invalid id");
            return;
        }

        Liquid[] newTypes = new Liquid[types.length - 1];
        System.arraycopy(types, 0, newTypes, 0, reservoir);
        System.arraycopy(types, reservoir + 1, newTypes, reservoir, newTypes.length - reservoir);
        types = newTypes;
    }

    /** get current efficiency of {@code reservoir}. */
    public static float getPressure(int reservoir){
        if(reservoir >= types.length){
            return 0f; // invalid reservoir id, no logging, since this method is called very often
        }

        PressureProducer producer = producers.get(reservoir);

        if(producer != null){
            return producer.pressure();
        }

        return 0f;
    }

    public static float getPressureCapacity(int reservoir){
        if(reservoir >= types.length){
            return 0f; // invalid reservoir id, no logging, since this method is called very often
        }

        return 240f;
    }

    public static Liquid getReserviourLiquid(int reservoir){
        if(reservoir >= types.length){
            return Liquids.oil;
        }

        return types[reservoir];
    }

    @Override
    public boolean shouldWrite(){
        return types.length > 0;
    }

    @Override
    public void write(DataOutput stream) throws IOException{
        stream.writeByte(1);
        stream.writeInt(types.length);

        for(Liquid type : types){
            stream.writeUTF(type.name); //item id is unstable between different mod versions, I suppose
        }
    }

    @Override
    public void read(DataInput stream) throws IOException{
        byte version = stream.readByte();
        int size = stream.readInt();

        types = new Liquid[size];
        for(int i = 0; i < size; i++){
            String name = stream.readUTF();
            Liquid liquid = Vars.content.liquid(name); // not annotated as nullable, but in reality does
            if(liquid == null) liquid = Liquids.oil;
            types[i] = liquid;
        }
    }
}