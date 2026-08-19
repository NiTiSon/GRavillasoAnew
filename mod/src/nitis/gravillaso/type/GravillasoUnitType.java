package nitis.gravillaso.type;

import mindustry.type.UnitType;
import mindustry.world.meta.Env;
import nitis.gravillaso.graphics.GrPal;

public class GravillasoUnitType extends UnitType{
    public GravillasoUnitType(String name){
        super(name);
        outlineColor = GrPal.outline;
        envDisabled = Env.scorching;
        researchCostMultiplier = 5f;
    }
}