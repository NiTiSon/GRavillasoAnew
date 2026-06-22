package nitis.gravillaso.type;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import nitis.gravillaso.ai.CargoDroneAI;

import static mindustry.Vars.iconMed;

public class CargoUnitType extends UnitType {
    public float powerCapacity = 2400f;
    public float powerUsePerTick = 0.5f;
    public float lowPowerRatio = 0.075f;
    public float chargePerTick = 5f;

    public CargoUnitType(String name) {
        super(name);
    }

    @Override
    public void display(Unit unit, Table table) {
        table.table(t -> {
            t.left();
            t.add(new Image(uiIcon)).size(iconMed).scaling(Scaling.fit);
            t.labelWrap(localizedName).left().width(190f).padLeft(5);
        }).growX().left();
        table.row();

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
