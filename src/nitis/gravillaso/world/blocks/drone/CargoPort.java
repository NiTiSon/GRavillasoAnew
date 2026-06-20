package nitis.gravillaso.world.blocks.drone;

import arc.struct.EnumSet;
import mindustry.entities.TargetPriority;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class CargoPort extends Block {
    public CargoPort(String name) {
        super(name);
        priority = TargetPriority.transport;
        update = true;
        sync = true;
        hasItems = true;
        separateItemCapacity = true;
        // hasLiquids = true;
        solid = true;
        group = BlockGroup.transportation;
        flags = EnumSet.of(BlockFlag.storage);
        envEnabled = Env.any;
    }

    @Override
    public boolean outputsItems() {
        return true;
    }

    public class PortBuilding extends Building {
        @Override
        public void updateTile() {
            dump();
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return false;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return false;
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            if (items == null) return 0;
            return Math.min(amount, getMaximumAccepted(item) - items.get(item));
        }
    }
}
