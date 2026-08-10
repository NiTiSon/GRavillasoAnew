package nitis.gravillaso.type;

import mindustry.type.UnitType;
import mindustry.world.meta.Env;
import nitis.gravillaso.graphics.GRPal;

public class GravillasoUnitType extends UnitType {
    public GravillasoUnitType(String name) {
        super(name);
        outlineColor = GRPal.outline;
        envDisabled = Env.scorching;
        researchCostMultiplier = 5f;
    }
}
