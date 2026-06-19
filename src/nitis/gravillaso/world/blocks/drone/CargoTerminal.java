package nitis.gravillaso.world.blocks.drone;

import arc.struct.EnumSet;
import arc.struct.Seq;
import mindustry.ctype.*;
import mindustry.entities.TargetPriority;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class CargoTerminal extends Block {
    public int maxFilterSize = 4;

    public CargoTerminal(String name) {
        super(name);
        priority = TargetPriority.transport;
        update = true;
        sync = true;
        destructible = true;
        hasItems = true;
        separateItemCapacity = true;
        hasLiquids = true;
        solid = true;
        group = BlockGroup.transportation;
        flags = EnumSet.of(BlockFlag.storage);
        envEnabled = Env.any;
    }

    public class TerminalBuilding extends Building {
        public UnlockableContent[] filter = new UnlockableContent[maxFilterSize];

        public boolean isFilterEmpty() {
            return filter[0] == null;
        }

        public Seq<UnlockableContent> filter() {
            Seq<UnlockableContent> ret = new Seq<>(maxFilterSize);
            for (UnlockableContent content : filter) {
                if (content == null) break;
                ret.add(content);
            }
            return ret;
        }

        boolean hasFilter(UnlockableContent content) {
            for (UnlockableContent c : filter) {
                if (c == content) return true;
                if (c == null) break;
            }
            return false;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return isFilterEmpty() || hasFilter(item);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return isFilterEmpty() || hasFilter(liquid);
        }
    }
}
