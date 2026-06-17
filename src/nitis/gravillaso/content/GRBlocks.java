package nitis.gravillaso.content;

import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.Env;
import nitis.gravillaso.world.blocks.drone.CargoDepot;

import static mindustry.type.ItemStack.with;
import static mindustry.content.Items.*;
import static nitis.gravillaso.content.GRItems.*;

public class GRBlocks {
    public static Block cargoDepot, CargoDepotLarge;

    public static void load() {
        cargoDepot = new CargoDepot("cargo-depot") {{
            requirements(Category.units, with(cobalt, 80, silicon, 120));
            size = 3;
            droneType = GRUnitTypes.phortotis;
            consumePower(1f);
            envEnabled |= Env.any;
        }};

        CargoDepotLarge = new CargoDepot("cargo-depot-large") {{
            requirements(Category.units, with(cobalt, 160, silicon, 300));
            size = 4;
            droneType = GRUnitTypes.skoros;
            consumePower(3f);
            envEnabled |= Env.any;
        }};
    }
}
