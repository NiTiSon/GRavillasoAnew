package nitis.gravillaso.world.draw;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.draw.*;

import static mindustry.Vars.tilesize;

public class DrawDirectionalPulse extends DrawBlock{
    public Color color = Color.white;

    public DrawDirectionalPulse(){
    }

    public DrawDirectionalPulse(Color color){
        this.color = color;
    }

    @Override
    public void draw(Building build){
        int rot = build.rotation;
        // TODO: implement
        // draw line from back to front (+rotation)
    }
}
