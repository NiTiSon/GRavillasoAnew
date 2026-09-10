package nitis.gravillaso.world.blocks.defense;

import mindustry.world.*;

public class BoostConductor extends Block{
    public BoostConductor(String name){
        super(name);
        update = solid = rotate = true;
        rotateDraw = false;
        noUpdateDisabled = true;
    }
}
