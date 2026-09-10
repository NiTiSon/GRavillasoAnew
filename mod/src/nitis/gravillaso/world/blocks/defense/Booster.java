package nitis.gravillaso.world.blocks.defense;

import mindustry.world.*;

public class Booster extends Block{
    public Booster(String name){
        super(name);
        rotateDraw = false;
        rotate = true;
        canOverdrive = false;
        drawArrow = true;
    }
}