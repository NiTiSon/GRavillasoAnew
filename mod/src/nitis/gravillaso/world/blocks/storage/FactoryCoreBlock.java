package nitis.gravillaso.world.blocks.storage;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Scaling;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.Stat;
import nitis.gravillaso.content.GrStat;
import nitis.gravillaso.content.GrUnitTypes;

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
        stats.add(GrStat.droneType,table -> {
            table.row();
            table.table(Styles.grayPanel, b -> {
                b.image(droneType.uiIcon).size(40).pad(10f).left().scaling(Scaling.fit);
                b.table(info -> {
                    info.add(droneType.localizedName).left();
                    if(Core.settings.getBool("console")){
                        info.row();
                        info.add(droneType.name).left().color(Color.lightGray);
                    }
                });
                b.button("?", Styles.flatBordert, () -> ui.content.show(droneType)).size(40f).pad(10).right().grow().visible(() -> droneType.unlockedNow());
            }).growX().pad(5).row();
        });
    }

    public class FactoryCoreBuild extends CoreBlock.CoreBuild {
        protected IntSeq readUnits = new IntSeq(); // required for saves
        public float droneProgress, totalDroneProgress, droneWarmup;
        public Seq<Unit> units = new Seq<>();

        @Override
        public void updateTile() {
            if(!readUnits.isEmpty()){ // reassign drones after `read`
                units.clear();
                readUnits.each(i -> {
                    var unit = Groups.unit.getByID(i);
                    if(unit != null){
                        units.add(unit);
                    }
                });
                readUnits.clear();
            }

            // TODO: weapons and drones are simulated on the server only; spawned units sync to clients, and cores can't use @Remote
            if(net.client()) return;

            units.removeAll(u -> !u.isAdded() || u.dead);

            float status = enabled ? 1f : 0f;
            droneWarmup = Mathf.lerpDelta(droneWarmup, units.size < droneSlots ? status : 0f, 0.1f);
            totalDroneProgress += droneWarmup * delta();

            if(units.size < droneSlots && (droneProgress += delta() * state.rules.unitBuildSpeed(team) / droneConstructTime) >= 1f){
                var unit = droneType.create(team);
                unit.set(x, y);
                unit.rotation = 90f;
                unit.add();
                units.add(unit);
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