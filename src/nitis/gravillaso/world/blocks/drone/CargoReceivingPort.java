package nitis.gravillaso.world.blocks.drone;

import mindustry.entities.TargetPriority;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.blocks.logic.CanvasBlock;
import mindustry.world.blocks.logic.MessageBlock;

public class CargoReceivingPort extends Block {
    public CargoReceivingPort(String name) {
        super(name);
        priority = TargetPriority.transport;
    }

    public class CargoReceivingPortBuilding extends Build {
    }
}
