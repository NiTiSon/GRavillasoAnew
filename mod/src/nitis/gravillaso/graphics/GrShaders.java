package nitis.gravillaso.graphics;

import arc.*;
import arc.files.*;
import arc.graphics.gl.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.graphics.*;
import mindustry.mod.*;
import nitis.gravillaso.*;

import static mindustry.Vars.*;

public class GrShaders{
    public static LineGlossShader lineGloss;

    public static void init(){
        lineGloss = new LineGlossShader(vanilla("default.vert"), mod("linegloss.frag"));
    }

    public static class LineGlossShader extends Shader{
        public LineGlossShader(Fi vertexShader, Fi fragmentShader){
            super(vertexShader, fragmentShader);
        }

        @Override
        public void apply(){
            setUniformf("u_time", Time.time);
        }
    }

    public static Fi mod(String name){
        Fi root = mods.getMod(GravillasoMod.class).root;
        return root.child("shaders").child(name);
    }

    public static Fi vanilla(String name){
        return Shaders.getShaderFi(name);
    }
}
