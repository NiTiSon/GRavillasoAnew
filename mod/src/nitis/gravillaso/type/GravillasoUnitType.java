package nitis.gravillaso.type;

import mindustry.type.*;
import mindustry.world.meta.*;
import nitis.gravillaso.graphics.*;

public class GravillasoUnitType extends UnitType{
    public GravillasoUnitType(String name){
        super(name);
        outlineColor = GrPal.outline;
        envDisabled = Env.scorching;
        researchCostMultiplier = 5f;
    }
}