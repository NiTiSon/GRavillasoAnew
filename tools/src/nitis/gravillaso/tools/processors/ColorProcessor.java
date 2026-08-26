package nitis.gravillaso.tools.processors;

import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.g2d.TextureAtlas.AtlasRegion;
import arc.graphics.g2d.TextureRegion;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;

import mindustry.world.Block;
import mindustry.world.blocks.*;
import mindustry.world.blocks.environment.*;

import mindustry.world.blocks.legacy.*;
import nitis.gravillaso.tools.*;

import static mindustry.Vars.*;

/**
 * Mirrors the color computation in vanilla Generators#generate("block-icons"): an alpha-weighted
 * average of the block sprite, floors darkened (x0.77), everything else brightened (x1.1), with the
 * squareSprite flag encoded in the alpha channel.
 * Writes sprites/block_colors.png with one pixel per mod-owned block IN REGISTRATION ORDER -
 * GravillasoMod.resolveBlockColors reads them back with the same iteration, so every related
 * block consumes an index even when no color can be computed for it (pixel stays transparent).
 * Runs last so it reads the post-AA/bleed sprites.
 */
public class ColorProcessor implements SpriteProcessor{
    @Override
    public void process(){
        Seq<Block> blocks = content.blocks().select(b ->
            b.minfo != null && b.minfo.mod == Tools.mod
        );

        if(blocks.isEmpty()) return;

        ObjectMap<String, Fi> sprites = new ObjectMap<>();
        Fi.get("sprites").walk(file -> {
            if(file.extEquals("png")) sprites.put(file.nameWithoutExtension(), file);
        });

        Pixmap colors = new Pixmap(blocks.size, 1);
        String prefix = Tools.mod.name + "-";

        try{
            for(int i = 0; i < blocks.size; i++){
                Block block = blocks.get(i);
                if(block instanceof ConstructBlock || block instanceof OreBlock || block instanceof LegacyBlock) continue;

                boolean colored = false;

                //fullIcon is missing for variant-only props, fall back to any real sprite
                TextureRegion icon = block.fullIcon.found() ? block.fullIcon :
                    block.variants > 0 && block.variantRegions != null && block.variantRegions.length > 0 ?
                    block.variantRegions[0] : null;

                do{
                    if(!(icon instanceof AtlasRegion atlasIcon)){ Log.warn("[color] @ icon not an atlas region: @", block.name, icon); break; }

                    String name = atlasIcon.name;
                    //raw sprites drop the mod prefix; generated ones ('block-gr-x-full') keep their full name
                    Fi file = sprites.get(name.startsWith(prefix) ? name.substring(prefix.length()) : name);
                    if(file == null){ Log.warn("[color] @ no file for region @", block.name, name); break; }

                Pixmap image = new Pixmap(file);
                boolean hasEmpty = false;
                Color average = new Color(), c = new Color();
                float asum = 0f;

                for(int x = 0; x < image.width; x++){
                    for(int y = 0; y < image.height; y++){
                        Color color = c.set(image.get(x, y));
                        average.r += color.r * color.a;
                        average.g += color.g * color.a;
                        average.b += color.b * color.a;
                        asum += color.a;
                        if(color.a < 0.9f){
                            hasEmpty = true;
                        }
                    }
                }
                image.dispose();

                if(asum <= 0f){ Log.warn("[color] @ sprite is fully transparent", block.name); break; }

                colored = true;

                average.mul(1f / asum);

                if(block instanceof Floor floor && !floor.wallOre){
                    average.mul(0.77f);
                }else{
                    average.mul(1.1f);
                }

                //encode square sprite in alpha channel
                average.a = hasEmpty ? 0.1f : 1f;
                colors.setRaw(i, 0, average.rgba());
                }while(false);

                if(!colored) Log.warn("[color] No source sprite for @", block.name);
            }
        }finally{
            Fi.get("sprites/block_colors.png").writePng(colors);
            colors.dispose();
        }

        Log.info("Wrote block colors for @ blocks.", blocks.size);
    }
}