package nitis.gravillaso.ai;

import arc.struct.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import nitis.gravillaso.type.*;
import nitis.gravillaso.world.blocks.drone.*;

import static mindustry.Vars.*;

public class CargoDroneAI extends AIController {
    static ObjectMap<Building, Unit> locks = new ObjectMap<>();

    public enum State {
        idle, await, receive, supply, charging
    }
    public State state = State.idle;
    public @Nullable Building target;

    public float homeX, homeY;
    public float loadTimer, unloadTimer;
    public float power;

    public static float transferRange = 20f;
    public static float moveRange = 6f;
    public static float smooth = 20f;
    public static float retryInterval = 60f;
    public static float homeRange = 8f;
    public static float loadTime = 60f;
    public static float unloadTime = 60f;

    @Override
    public void init() {
        if (unit.type instanceof CargoUnitType t) {
            power = t.powerCapacity;
        }
    }

    @Override
    public void updateMovement() {
        switch (state) {
            case idle -> updateIdle();
            case await -> updateAwaiting();
            case receive -> updateReceive();
            case supply -> updateSupply();
            case charging -> updateCharging();
        }

        if (target != null && !unit.within(target, moveRange)) {
            moveTo(target, moveRange, smooth);
            if (unit.isFlying()) unit.lookAt(target);
        }

        if ((state == State.idle || state == State.charging) && target == null && !unit.within(homeX, homeY, homeRange)) {
            moveTo(Tmp.v1.set(homeX, homeY), homeRange, smooth);
            if (unit.isFlying()) unit.lookAt(homeX, homeY);
        }
    }

    void updateIdle() {
        target = null;

        if (unit.type instanceof CargoUnitType t && power < t.powerCapacity * t.lowPowerRatio) {
            state = State.charging;
            return;
        }

        if (unit.hasItem()) {
            Building dest = findSupplyTarget(unit.item());
            if (dest != null) {
                target = dest;
                unloadTimer = 0f;
                state = State.supply;
            }
            return;
        }

        Building src = findReceiveTarget();
        if (src != null) {
            target = src;
            loadTimer = 0f;
            state = State.receive;
        }
    }

    void updateAwaiting() {
        if (timer.get(timerTarget, retryInterval)) {
            state = State.idle;
        }
    }

    void updateReceive() {
        if (target == null || !target.isValid() || target.team != unit.team) {
            release();
            state = State.idle;
            return;
        }

        if (!tryLock()) return;

        if (!unit.within(target, transferRange)) return;

        if (unit.hasItem() || target.items == null || !target.items.any()) {
            release();
            state = State.idle;
            return;
        }

        if (loadTimer < loadTime) {
            loadTimer += Time.delta;
            return;
        }

        Item best = null;
        int bestAmount = 0;
        for (Item item : content.items()) {
            int amount = target.items.get(item);
            if (amount > bestAmount) {
                bestAmount = amount;
                best = item;
            }
        }

        if (best != null && bestAmount > 0) {
            int max = unit.maxAccepted(best);
            if (max > 0) {
                Call.takeItems(target, best, Math.min(bestAmount, max), unit);
            }
        }

        release();
        state = State.idle;
    }

    void updateSupply() {
        float use = unit.type instanceof CargoUnitType t ? t.powerUsePerTick : 0f;
        power = Math.max(0, power - use * Time.delta);

        if (target == null || !target.isValid() || target.team != unit.team) {
            release();
            state = State.idle;
            return;
        }

        if (!tryLock()) return;

        if (!unit.within(target, transferRange)) return;

        if (power <= 0f) {
            release();
            state = State.charging;
            return;
        }

        if (!unit.hasItem()) {
            release();
            state = State.idle;
            return;
        }

        if (unloadTimer < unloadTime) {
            unloadTimer += Time.delta;
            return;
        }

        int max = target.acceptStack(unit.item(), unit.stack.amount, unit);
        if (max > 0) {
            Call.transferItemTo(unit, unit.item(), max, unit.x, unit.y, target);
        }

        if (!unit.hasItem()) {
            release();
            state = State.idle;
        }
    }

    void updateCharging() {
        target = null;
        release();

        if (unit.type instanceof CargoUnitType t && unit.within(homeX, homeY, homeRange)) {
            power = Math.min(t.powerCapacity, power + t.chargePerTick * Time.delta);
            if (power >= t.powerCapacity) {
                state = State.idle;
            }
        }
    }

    Building findReceiveTarget() {
        Building best = null, bestOccupied = null;
        int bestItems = -1, bestOccItems = -1;

        for (Building b : Groups.build) {
            if (b.team != unit.team) continue;
            if (!(b instanceof CargoTerminal.TerminalBuilding)) continue;
            if (b.items == null || !b.items.any()) continue;

            int total = b.items.total();
            if (locks.containsKey(b)) {
                if (total > bestOccItems) {
                    bestOccItems = total;
                    bestOccupied = b;
                }
            } else {
                if (total > bestItems) {
                    bestItems = total;
                    best = b;
                }
            }
        }

        return best != null ? best : bestOccupied;
    }

    Building findSupplyTarget(Item carried) {
        Building best = null, bestOccupied = null;

        for (Building b : Groups.build) {
            if (b.team != unit.team) continue;
            if (!(b instanceof CargoPort.PortBuilding)) continue;
            if (b.items == null) continue;

            if (locks.containsKey(b)) {
                if (bestOccupied == null) bestOccupied = b;
            } else {
                if (best == null) best = b;
            }
        }

        return best != null ? best : bestOccupied;
    }

    boolean tryLock() {
        if (target == null) {
            return false;
        }

        Unit owner = locks.get(target);
        if (owner == unit) {
            return true;
        }

        if (owner != null && owner.isValid()) {
            target = null;
            state = State.await;
            return false;
        }

        locks.put(target, unit);
        return true;
    }

    void release() {
        if (target != null) {
            locks.remove(target);
            target = null;
        }
    }
}
