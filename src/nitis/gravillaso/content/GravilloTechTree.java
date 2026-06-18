package nitis.gravillaso.content;

import arc.struct.*;
import mindustry.content.Items;
import mindustry.type.*;

import static mindustry.Vars.*;
import static mindustry.content.Blocks.*;
import static mindustry.content.TechTree.*;
import static nitis.gravillaso.content.GRBlocks.*;

public class GravilloTechTree {
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
        // silicon is acquired from bauxite + something(acid?)
        // alumina is acquired from bauxite
        // aluminium & oxygen is acquired from alumina

        var costMultipliers = new ObjectFloatMap<Item>();
        for (var item : content.items()) {
            costMultipliers.put(item, 0.75f);
        }

        // TODO: add hard-to-make materials in here
        /*
        costMultipliers.put(Items.oxide, 0.5f);
        costMultipliers.put(Items.surgeAlloy, 0.7f);
        costMultipliers.put(Items.carbide, 0.3f);
        costMultipliers.put(Items.phaseFabric, 0.2f);
        */

        GRPlanets.gravillo.techTree = nodeRoot("gravillo", coreFortress, true, () -> {
            context().researchCostMultipliers = costMultipliers;

            // distribution
            node(cargoDepot, () -> {
                node(cargoSupplyPort, () -> {
                    node(cargoReceivingPort, () -> {

                    });
                });

                node(cargoDepotLarge, () -> {

                });
            });

            // items
            nodeProduce(GRItems.cobalt, () -> {
                nodeProduce(Items.lead, () -> {
                    nodeProduce(GRItems.bauxite, () -> {
                        nodeProduce(GRItems.alumina, () -> {
                           nodeProduce(GRItems.aluminium, () -> {});
                        });

                        nodeProduce(Items.silicon, () -> {

                        });
                    });
                });
            });
        });
    }
}
