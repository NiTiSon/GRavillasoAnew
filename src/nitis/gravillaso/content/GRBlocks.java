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
import nitis.gravillaso.world.blocks.drone.CargoPort;
import nitis.gravillaso.world.blocks.drone.CargoTerminal;

import static mindustry.type.ItemStack.with;
import static mindustry.content.Items.*;
import static nitis.gravillaso.content.GRItems.*;

public class GRBlocks {
    // storage - gravillo
    public static Block coreFortress, coreTier2, coreTier3;
    // cargo
    public static Block cargoDepot, cargoDepotLarge, cargoPort, cargoTerminal;

    public static void load() {
        coreFortress = new CoreBlock("core-fortress") {{
            requirements(Category.effect, with(cobalt, 1000, lead, 800));
            isFirstTier = true;
            size = 4;

            unitType = UnitTypes.gamma;
            health = 3500;
            itemCapacity = 2000;
            thrusterLength = 34/4f;
            armor = 5f;

            alwaysUnlocked = true;
            incinerateNonBuildable = true;
            buildCostMultiplier = 0.7f;
            requiresCoreZone = true;

            unitCapModifier = 5;
        }};

        coreTier2 = new CoreBlock("core-2") {{
            requirements(Category.effect, with(cobalt, 67, lead, 67));
            size = 5;

            unitType = UnitTypes.gamma;
            health = 9000;
            itemCapacity = 3000;
            thrusterLength = 40/4f;
            armor = 10f;

            incinerateNonBuildable = true;
            buildCostMultiplier = 0.7f;
            requiresCoreZone = true;

            unitCapModifier = 13;
            researchCostMultiplier = 0.1f;
        }};

        coreTier3 = new CoreBlock("core-3") {{
            requirements(Category.effect, with(cobalt, 67, lead, 67));
            size = 6;

            unitType = UnitTypes.gamma;
            health = 21000;
            itemCapacity = 4000;
            thrusterLength = 48/4f;
            armor = 10f;

            incinerateNonBuildable = true;
            buildCostMultiplier = 0.7f;
            requiresCoreZone = true;

            unitCapModifier = 23;
            researchCostMultiplier = 0.1f;
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

        cargoPort = new CargoPort("cargo-port") {{
            requirements(Category.distribution, with(cobalt, 160, silicon, 300));
            size = 3;

            itemCapacity = 320;
        }};

        cargoTerminal = new CargoTerminal("cargo-terminal") {{
            requirements(Category.distribution, with(cobalt, 160, silicon, 300));
            size = 3;

            itemCapacity = 320;
        }};
    }
}
