package nitis.gravillaso.world.blocks.distribution;

import mindustry.world.*;
import mindustry.world.blocks.distribution.*;

/** Maglev conveyor, fast speed, items can cross liquid */
public class MaglevConveyor extends Block{
    // for example:
    // w - water, x - ground, = - conveyor
    // xxwwwxx
    // ==www==
    // xxwwwxx
    //
    // if space between two conveyors are less than 6, conveyor can pass items by air
    // first conveyor MUST face to the other conveyor input side
    //
    // When connected phase-bridge(only visual) is appears between

    public MaglevConveyor(String name){
        super(name);
    }

    public class MaglevConveyorBuild extends Build{

    }
}
