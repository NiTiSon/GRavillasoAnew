package nitis.gravillaso.world.blocks.storage;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.content.Fx;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.world.blocks.storage.CoreBlock;
import nitis.gravillaso.content.GRUnitTypes;
import nitis.gravillaso.type.BlockWeapon;
import nitis.gravillaso.world.blocks.defense.MultiWeaponBuild;

import static mindustry.Vars.net;
import static mindustry.Vars.state;

public class FactoryCoreBlock extends CoreBlock {
    public int droneSlots = 1;
    public UnitType droneType = GRUnitTypes.draugDrone;
    public float droneConstructTime = 10f * 60f;
    public Seq<BlockWeapon> weapons = new Seq<>();

    public FactoryCoreBlock(String name) {
        super(name);
        ambientSound = Sounds.loopUnitBuilding;
        ambientSoundVolume = 0.13f;
    }

    @Override
    public void load() {
        super.load();
        for(var weapon : weapons){
            weapon.load();
        }
    }

    public class FactoryCoreBuild extends CoreBlock.CoreBuild implements MultiWeaponBuild {
        public float droneProgress, totalDroneProgress, droneWarmup;
        public Seq<Unit> units = new Seq<>();
        public Seq<WeaponMount> mounts = new Seq<>();

        public void setupWeapons(){
            mounts.clear();
            for(var weapon : weapons){
                mounts.add(new WeaponMount(weapon));
            }
            Log.info("FactoryCore '@': set up @ weapon mounts, client=@", block.name, mounts.size, net.client());
        }

        @Override
        public float rotation(){ return rotation; }

        @Override
        public float efficiency(){ return efficiency; }

        @Override
        public boolean isControlled(){ return false; }

        @Override
        public boolean logicControlled(){ return false; }

        @Override
        public boolean canShoot(){ return true; }

        @Override
        public Seq<WeaponMount> mounts(){ return mounts; }

        @Override
        public void update() {
            super.update();

            if(mounts.size != weapons.size){
                setupWeapons();
            }

            // TODO: weapons and drones are simulated on the server only; spawned units sync to clients, and cores can't use @Remote
            if(net.client()) return;

            for(int i = 0; i < mounts.size; i++){
                weapons.get(i).update(this, mounts.get(i));
            }

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

            for(int i = 0; i < mounts.size; i++){
                weapons.get(i).draw(this, mounts.get(i));
            }

            if(net.client() || droneWarmup <= 0.001f) return;
            Draw.draw(Layer.blockOver + 0.2f, () -> Drawf.construct(this, droneType.fullIcon, Pal.accent, 0f, droneProgress, droneWarmup, totalDroneProgress, 14f));
        }
    }
}
