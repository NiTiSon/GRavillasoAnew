package nitis.gravillaso.tools.processors;

import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;

import mindustry.world.blocks.environment.*;

import nitis.gravillaso.tools.*;
import nitis.gravillaso.tools.GeneratedAtlas.*;

import static mindustry.Vars.*;

/**
 * Mirrors mindustry.tools.Generators#generate("ore-icons"): bakes a semi-transparent shadow
 * below each ore variant and writes the block's '-full' and '-ui' icons.
 * The variant file keeps its prefix-stripped name (cobalt1.png -> region gr-cobalt1, matching
 * Block.load()'s variantRegions lookup); '-full'/'-ui' names already carry the mod name after the
 * type prefix, so Mods.packSprites won't add it again.
 */
public class OreGenerator implements SpriteProcessor{
    @Override
    public void process(){
        content.blocks().<OreBlock>each(b -> b instanceof OreBlock && b.minfo != null && b.minfo.mod == Tools.mod, ore -> {
            try{
                ore.load();
            }catch(Exception e){
                Log.err(e);
                return;
            }

            int shadowColor = Color.rgba8888(0, 0, 0, 0.3f);

            for(int i = 0; i < ore.variants; i++){
                GeneratedRegion baseRegion = Tools.atlas.find(ore.name + i);
                if(!baseRegion.found()) continue;

                Pixmap base = baseRegion.pixmap();
                Pixmap image = base.copy();

                int offset = image.width / (int)tilesize - 1;

                for(int x = 0; x < image.width; x++){
                    for(int y = offset; y < image.height; y++){
                        //draw semi transparent background
                        if(base.getA(x, y - offset) != 0){
                            image.setRaw(x, y, Pixmap.blend(shadowColor, base.getRaw(x, y)));
                        }
                    }
                }

                image.draw(base, true);

                //overwrite the variant sprite and its atlas region with the shadowed image
                new GeneratedRegion(baseRegion.name, image, baseRegion.file).save(true);

                new GeneratedRegion("block-" + ore.name + "-full", image, SpriteProcessor.fullFile("block-" + ore.name + "-full")).save(true);
                new GeneratedRegion("block-" + ore.name + "-ui", image, SpriteProcessor.uiFile("block-" + ore.name + "-ui")).save(true);
            }
        });
    }
}