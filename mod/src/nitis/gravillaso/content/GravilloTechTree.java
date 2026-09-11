package nitis.gravillaso.content;

import arc.struct.*;
import mindustry.type.*;

import static mindustry.content.Items.*;
import static mindustry.content.Liquids.cryofluid;
import static mindustry.content.TechTree.*;
import static nitis.gravillaso.content.GrBlocks.*;
import static nitis.gravillaso.content.GrItems.*;
import static nitis.gravillaso.content.GrLiquids.*;
import static nitis.gravillaso.content.GrUnitTypes.*;

public class GravilloTechTree{
    public static void load() {
        // planned resources:
        // T1: cobalt, lead - 1st map
        // T2: bauxite, silicon - 2nd map
        // T3: gold - 3rd/4th map
        // T4: tungsten
        // T5: aluminium
        // T6: some kind of replacement for surge-alloy and phase-fabric
        //
        //   oxygen?
        //   hydrogen?
        //
        // silicon is acquired from bauxite + brine
        // aluminium & oxygen is acquired from bauxite

        var costMultipliers = new ObjectFloatMap<Item>();
        /*
        costMultipliers.put(Items.oxide, 0.5f);
        costMultipliers.put(Items.surgeAlloy, 0.7f);
        costMultipliers.put(Items.carbide, 0.3f);
        costMultipliers.put(Items.phaseFabric, 0.2f);
        */

        GrPlanets.gravillo.techTree = nodeRoot("gravillo", coreBase, true, () -> {
            context().researchCostMultipliers = costMultipliers;

            // distribution
            node(cobaltConveyor, () -> {
                // TODO: junction
                //node(cobaltRouter, () -> {

                //});
                node(maglevConveyor, () -> {

                });
            });

            // production
            node(bauxiteCrusher, () -> {

            });

            // power
            node(powerSection, () -> {
                node(booster, () -> {
                    node(boostRedirector, () -> {
                        node(largeBoostRedirector);
                        node(boostRouter, () -> {
                            node(largeBoostRouter);
                        });
                    });
                });
            });

            // cores
            node(draugDrone, ItemStack.with(cobalt, 1500), () -> {
//            node(coreTier2, () -> {
//                node(coreTier3, () -> {
//
//                });
//            });
            });

            // turrets
            node(sight, () -> {
                node(cobaltWall, () -> {
                   node(cobaltWallLarge, () -> {

                   });
                });
            });

            // units

            // sectors
//            node(negativeOnCelsius, () -> {
//
//            });

            // items
            // THE ORDER WILL CHANGE 100%
            nodeProduce(cobalt, () -> {
                nodeProduce(lead, () -> {
                    nodeProduce(brine, () -> {
                        nodeProduce(aluminium, () -> {

                        });
                    });
                });

                nodeProduce(bauxite, () -> {
                    nodeProduce(silicon, () -> {

                    });

                    nodeProduce(oxygen, () -> {
                        nodeProduce(oxide, () -> {

                        });
                    });
                });

                nodeProduce(cryofluid, () -> { // Use rockets for phaseFabric, (maybe replace with phaseFluid/phaseAlloy
                    nodeProduce(phaseFabric, () -> {

                    });
                });
            });
        });
    }
}