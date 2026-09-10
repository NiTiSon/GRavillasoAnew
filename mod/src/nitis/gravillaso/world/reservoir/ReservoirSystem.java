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
import mindustry.type.*;
import mindustry.world.*;
import nitis.gravillaso.*;

import java.io.*;
import java.util.*;

import static mindustry.Vars.*;

public class ReservoirSystem implements SaveFileReader.CustomChunk{
    public static final Seq<Liquid> types = Seq.with(); // maybe add information like maximum pressure/efficiency

    public ReservoirSystem(){
        SaveVersion.addCustomChunk("gr-reservoir", this);
    }

    /** get current efficiency of {@code reserviour}. */
    public float getPressure(int reserviour){
        if(reserviour >= types.size){
            return 0f; // invalid reserviour id, no logging, since this method is called very often
        }

        // TODO: track current pressure
        return 1f;
    }

    public Liquid getReserviourLiquid(int reserviour){
        if(reserviour >= types.size){
            return Liquids.oil;
        }

        return types.get(reserviour);
    }

    @Override
    public void write(DataOutput stream) throws IOException{
        stream.writeByte(1);
        stream.writeInt(types.size);
        for(int i = 0; i < types.size; i++){
            stream.writeUTF(types.get(i).name); // id is unstable between saves , I suppose
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
    }
}