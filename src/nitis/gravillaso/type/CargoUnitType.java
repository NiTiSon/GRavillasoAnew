package nitis.gravillaso.type;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.scene.ui.layout.Table;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import nitis.gravillaso.ai.CargoDroneAI;

public class CargoUnitType extends UnitType {
    public float powerCapacity = 2400f;
    public float powerUsePerTick = 0.5f;
    public float lowPowerRatio = 0.075f;
    public float chargeSpeed = 5f;

    public CargoUnitType(String name) {
        super(name);
    }

    @Override
    public void display(Unit unit, Table table) {
        table.table(bars -> {
            bars.defaults().growX().height(20f).pad(4);
            bars.add(new Bar("stat.health", Pal.health, unit::healthf).blink(Color.white));
            bars.row();
            if (unit.controller() instanceof CargoDroneAI ai) {
                bars.add(new Bar(
                    () -> Core.bundle.get("stat.drone-power"),
                    () -> Pal.accent,
                    () -> ai.power / powerCapacity
                ));
                bars.row();
            }
        }).growX();
    }
}
