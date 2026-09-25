package nitis.gravillaso.world.blocks.storage;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.storage.*;
import nitis.gravillaso.annotations.Annotations.*;
import nitis.gravillaso.content.*;
import nitis.gravillaso.gen.*;
import nitis.gravillaso.world.meta.*;

import static mindustry.Vars.*;

public class FactoryCoreBlock extends CoreBlock {
    public int droneSlots = 1;
    // DISCUSSION:
    // How handle new cores?
    // Do they should make new units or use Draug
    // What about building helper drones?
    // What if user wants both helpers and miners?
    public UnitType droneType = GrUnitTypes.draugDrone;
    public float droneConstructTime = 10f * 60f;

    public FactoryCoreBlock(String name) {
        super(name);
    }

    @Override
    public void setStats() {
        super.setStats();

        // code from CoreBlock.setStas
        stats.add(GrStat.droneType, table -> {
            table.row();
            table.table(Styles.grayPanel, b -> {
                if(droneType.unlockedNow()){
                    b.image(droneType.uiIcon).size(40).pad(10f).left().scaling(Scaling.fit);
                    b.table(info -> {
                        info.add(droneType.localizedName).left();
                        if(Core.settings.getBool("console")){
                            info.row();
                            info.add(droneType.name).left().color(Color.lightGray);
                        }
                    });
                }else{
                    b.image(Icon.lock).color(Pal.darkerGray).size(40).pad(10f).left();
                    b.table(info -> info.add(droneType.localizedName).color(Pal.darkerGray).left());
                }
                b.button("?", Styles.flatBordert, () -> ui.content.show(droneType)).size(40f).pad(10).right().grow().visible(() -> droneType.unlockedNow());
            }).growX().pad(5).row();
        });
    }

    /** Tells clients which building a spawned drone is tethered to; the tether field isn't part of the unit snapshot. */
    @Remote(targets = Loc.server)
    public static void unitTetheredToCore(Building building, int unitId){
        var unit = Groups.unit.getByID(unitId);
        if(unit == null || unit.dead || !(unit instanceof BuildingTetherc tether)) return;
        tether.building(building);
    }

    public class FactoryCoreBuild extends CoreBlock.CoreBuild {
        protected IntSeq readUnits = new IntSeq(); // required for saves
        public float droneProgress, totalDroneProgress, droneWarmup;
        public Seq<Unit> units = new Seq<>();

        @Override
        public void updateTile() {
            super.updateTile();

            if(!readUnits.isEmpty()){ // reassign drones after `read`
                units.clear();
                readUnits.each(i -> {
                    var unit = Groups.unit.getByID(i);
                    if(unit != null){
                        units.add(unit);
                        if(unit instanceof BuildingTetherc tether){
                            tether.building(this);
                            //sync the reassigned tether to clients
                            GravillasoCall.unitTetheredToCore(this, unit.id);
                        }
                    }
                });
                readUnits.clear();
            }

            // weapons and drones are simulated on the server only; spawned units sync to clients via @Remote
            if(net.client()) return;

            units.removeAll(u -> !u.isAdded() || u.dead);

            float status = enabled ? 1f : 0f;
            droneWarmup = Mathf.lerpDelta(droneWarmup, units.size < droneSlots && droneType.unlockedNow() ? status : 0f, 0.1f);
            totalDroneProgress += droneWarmup * delta();

            if(units.size < droneSlots && droneType.unlockedNow() && (droneProgress += delta() * state.rules.unitBuildSpeed(team) / droneConstructTime) >= 1f){
                var unit = droneType.create(team);
                if(unit instanceof BuildingTetherc tether){
                    tether.building(this);
                }else{
                    Log.err("Drone is not implements BuildingTetherc");
                }
                unit.set(x, y);
                unit.rotation = 90f;
                unit.add();
                units.add(unit);
                GravillasoCall.unitTetheredToCore(this, unit.id);
                droneProgress = 0f;
                Fx.spawn.at(x, y);
            }
        }

        @Override
        public void draw() {
            super.draw();

            if(net.client() || droneWarmup <= 0.001f) return;
            Draw.draw(Layer.blockOver + 0.2f, () -> Drawf.construct(this, droneType.fullIcon, Pal.accent, 0f, droneProgress, droneWarmup, totalDroneProgress, 14f));
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.b(units.size);
            for(var unit : units){
                write.i(unit.id);
            }
        }

        @Override
        public byte version(){
            return 1;
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            int count = read.b();
            readUnits.clear();
            for(int i = 0; i < count; i++){
                readUnits.add(read.i());
            }
        }
    }
}