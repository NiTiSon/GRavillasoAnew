package nitis.gravillaso.world.blocks.drone;

import arc.struct.EnumSet;
import mindustry.entities.TargetPriority;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.meta.BlockFlag;

public class CargoSupplyPort extends Block {
    public CargoSupplyPort(String name) {
        super(name);
        priority = TargetPriority.transport;
    }

    public class CargoSupplyPortBuilding extends Build {
    }
}
