package nitis.gravillaso.world.blocks.drone;

import arc.Core;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import nitis.gravillaso.ai.CargoDroneAI;
import nitis.gravillaso.type.CargoUnitType;

public class CargoDepot extends Block {
    public UnitType droneType;
    public float buildDuration = 90f * Time.toSeconds;
    public float powerCapacity = 8000f;
    public float powerUseConstruction = 1f;
    public float droneChargeSpeed = 5f;

    public CargoDepot(String name) {
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        hasPower = true;
        configurable = false;
        group = BlockGroup.units;
        consumePowerBuffered(powerCapacity);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("drone-status", (DepotBuilding build) -> {
            if (build.drone != null && build.drone.isValid() && !build.drone.dead()) {
                return new Bar(
                    () -> Core.bundle.get("bar.drone-health"),
                    () -> Pal.heal,
                    () -> build.drone.health / droneType.health
                );
            } else {
                return new Bar(
                    () -> Core.bundle.get("bar.progress"),
                    () -> Pal.ammo,
                    () -> build.progress / buildDuration
                );
            }
        });
        addBar("drone-power", (DepotBuilding build) -> {
            if (build.drone != null && build.drone.isValid() && !build.drone.dead()
                && build.drone.controller() instanceof CargoDroneAI ai
                && build.drone.type instanceof CargoUnitType t) {
                return new Bar(
                    () -> Core.bundle.get("bar.drone-power"),
                    () -> Pal.accent,
                    () -> ai.power / t.powerCapacity
                );
            }
            return null;
        });
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    public class DepotBuilding extends Building {
        public Unit drone;
        public float progress;
        public boolean isConstructing;
        private int pendingDroneId = -1;

        @Override
        public void updateTile() {
            if (droneType == null) return;

            if (pendingDroneId != -1) {
                drone = Groups.unit.find(u -> u.id == pendingDroneId);
                if (drone != null && (drone.dead() || !drone.isValid())) {
                    drone = null;
                }
                if (drone != null && drone.controller() instanceof CargoDroneAI ai) {
                    ai.homeX = x;
                    ai.homeY = y;
                }
                pendingDroneId = -1;
            }

            if (drone == null || !drone.isValid() || drone.dead()) {
                if (!isConstructing) {
                    isConstructing = true;
                    progress = 0f;
                }

                progress += delta() * efficiency;
                if (power != null) {
                    float drain = Math.min(powerUseConstruction * delta(), power.status * powerCapacity);
                    power.status -= drain / powerCapacity;
                }
                if (progress >= buildDuration) {
                    spawnDrone();
                }
            } else if (isConstructing) {
                isConstructing = false;
                progress = 0f;
            }

            if (drone != null && drone.isValid() &&
                    drone.controller() instanceof CargoDroneAI ai && ai.state == CargoDroneAI.State.charging &&
                    power != null && power.status > 0) {
                float drain = Math.min(droneChargeSpeed * delta(), power.status * powerCapacity);
                power.status -= drain / powerCapacity;
            }
        }

        private void spawnDrone() {
            if (droneType == null) return;

            drone = droneType.spawn(team, x, y, 90f);
            if (drone != null) {
                drone.vel().add(0f, 1.5f);
                if (drone.controller() instanceof CargoDroneAI ai) {
                    ai.homeX = x;
                    ai.homeY = y;
                }
            }
            isConstructing = false;
            progress = 0f;
        }

        @Override
        public void onDestroyed() {
            super.onDestroyed();
            killDrone();
        }

        @Override
        public void onRemoved() {
            super.onRemoved();
            killDrone();
        }

        private void killDrone() {
            if (drone != null && drone.isValid() && !drone.dead()) {
                drone.kill();
            }
            drone = null;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            write.bool(isConstructing);
            write.i(drone != null && drone.isValid() && !drone.dead() ? drone.id : -1);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            progress = read.f();
            isConstructing = read.bool();
            pendingDroneId = read.i();
        }
    }
}
