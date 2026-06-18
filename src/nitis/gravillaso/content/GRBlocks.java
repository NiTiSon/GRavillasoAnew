package nitis.gravillaso.content;

import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.Env;
import nitis.gravillaso.world.blocks.drone.CargoDepot;
import nitis.gravillaso.world.blocks.drone.CargoReceivingPort;
import nitis.gravillaso.world.blocks.drone.CargoSupplyPort;

import static mindustry.type.ItemStack.with;
import static mindustry.content.Items.*;
import static nitis.gravillaso.content.GRItems.*;

public class GRBlocks {
    // storage - gravillo
    public static Block coreFortress;
    // cargo
    public static Block cargoDepot, cargoDepotLarge, cargoSupplyPort, cargoReceivingPort;

    public static void load() {
        coreFortress = new CoreBlock("core-fortress") {{
            requirements(Category.effect, with(cobalt, 1000, lead, 800));

            isFirstTier = true;
            unitType = UnitTypes.gamma;
            health = 3500;
            itemCapacity = 2000;
            size = 4;
            thrusterLength = 34/4f;
            armor = 5f;
            alwaysUnlocked = true;
            incinerateNonBuildable = true;
            requiresCoreZone = true;

            buildCostMultiplier = 0.7f;

            unitCapModifier = 7;
            researchCostMultiplier = 0.07f;
        }};

        cargoDepot = new CargoDepot("cargo-depot") {{
            requirements(Category.distribution, with(cobalt, 80, silicon, 120));
            size = 3;
            droneType = GRUnitTypes.phortotis;
            consumePower(1f);
            envEnabled |= Env.any;
        }};

        cargoDepotLarge = new CargoDepot("cargo-depot-large") {{
            requirements(Category.distribution, with(cobalt, 160, silicon, 300));
            size = 4;
            droneType = GRUnitTypes.skoros;
            consumePower(3f);
            envEnabled |= Env.any;
        }};

        cargoSupplyPort = new CargoSupplyPort("cargo-supply-port") {{
            requirements(Category.distribution, with(cobalt, 160, silicon, 300));

            size = 3;
        }};

        cargoReceivingPort = new CargoReceivingPort("cargo-receiving-port") {{
            requirements(Category.distribution, with(cobalt, 160, silicon, 300));

            size = 3;
        }};
    }
}
