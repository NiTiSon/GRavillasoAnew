package nitis.gravillaso.world.blocks.distribution;

import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.graphics.*;
import mindustry.world.blocks.distribution.*;
import nitis.gravillaso.annotations.Annotations.*;

public class GrStackRouter extends StackRouter{
    @Load("@name$-center") public TextureRegion centerRegion;

    public GrStackRouter(String name){
        super(name);
    }

    public class GrStackRouterBuild extends StackRouterBuild{
        @Override
        public void draw(){
            Draw.rect(region, x, y);
            if(sortItem != null){
                Draw.color(sortItem.color);
                Draw.rect(centerRegion, x, y);
                Draw.color();
            }else{
                Draw.rect(topRegion, x, y, rotdeg());
            }

            if(glowRegion.found() && power != null && power.status > 0){
                Draw.z(Layer.blockAdditive);
                Draw.color(glowColor, glowAlpha * power.status);
                Draw.blend(Blending.additive);
                Draw.rect(glowRegion, x, y, rotation * 90);
                Draw.blend();
                Draw.color();
            }
        }
    }
}
