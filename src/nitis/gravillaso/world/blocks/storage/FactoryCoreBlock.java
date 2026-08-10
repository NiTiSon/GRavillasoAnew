package nitis.gravillaso.world.blocks.storage;

import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.type.UnitType;
import mindustry.world.blocks.storage.CoreBlock;
import nitis.gravillaso.content.GRUnitTypes;

public class FactoryCoreBlock extends CoreBlock {
    public int droneSlots = 1; // amount of available drones per core
    public Seq<UnitType> droneTypes = Seq.with(GRUnitTypes.draugDrone);

    public FactoryCoreBlock(String name) {
        super(name);
    }
}
