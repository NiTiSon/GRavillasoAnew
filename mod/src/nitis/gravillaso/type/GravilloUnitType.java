package nitis.gravillaso.type;

import mindustry.type.*;
import mindustry.world.meta.*;
import nitis.gravillaso.graphics.*;

public class GravilloUnitType extends UnitType{
    public GravilloUnitType(String name){
        super(name);
        outlineColor = GrPal.outline;
        envDisabled = Env.scorching;
        researchCostMultiplier = 5f;
    }
}