package nitis.gravillaso.world.blocks.environment;

import arc.graphics.*;
import arc.util.*;
import mindustry.type.*;
import mindustry.world.*;
import nitis.gravillaso.world.reservoir.*;

public interface ReservoirBlock{
    int configIndex();

    /** Reservoir liquid configured on a tile, or null if it has none. */
    default @Nullable Liquid tileLiquid(Tile tile){
        int index = tile.extraData;
        return index >= 0 && index < ReservoirSystem.types.size ? ReservoirSystem.types.get(index) : null;
    }
}