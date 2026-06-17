package nitis.gravillaso.content;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import nitis.gravillaso.world.blocks.drone.CargoDepot;

import static mindustry.type.ItemStack.with;

public class GRBlocks {
    public static Block cargoDepot, bigCargoDepot;

    public static void load() {
        cargoDepot = new CargoDepot("cargo-depot") {{
            requirements(Category.units, with(Items.copper, 80, Items.lead, 60, Items.silicon, 50, Items.titanium, 40));
            size = 3;
            droneType = GRUnitTypes.cargoDrone;
            buildDuration = 120f * 60f;
            consumePower(1f);
            envEnabled |= Env.any;
        }};

        bigCargoDepot = new CargoDepot("big-cargo-depot") {{
            requirements(Category.units, with(Items.copper, 160, Items.lead, 120, Items.silicon, 100, Items.titanium, 80, Items.thorium, 50));
            size = 4;
            droneType = GRUnitTypes.bigCargoDrone;
            buildDuration = 120f * 60f;
            consumePower(3f);
            envEnabled |= Env.any;
        }};
    }
}
