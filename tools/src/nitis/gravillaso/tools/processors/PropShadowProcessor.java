package nitis.gravillaso.tools.processors;

import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.TextureRegion;
import arc.math.*;
import arc.struct.Seq;
import arc.util.Log;

import mindustry.world.Block;

import nitis.gravillaso.tools.*;

import static mindustry.Vars.*;

/**
 * Generates '-shadow'/'-shadow<i>' silhouettes for props (customShadow blocks): the variant art
 * blurred and turned black, same size as the source sprite.
 */
public class PropShadowProcessor implements SpriteProcessor{
    /** Box blur window size in pixels. */
    static final int blurPower = 10;

    @Override
    public void process(){
        content.blocks().each(b -> b.customShadow && b.minfo != null && b.minfo.mod == Tools.mod, block -> {
            try{
                process(block);
            }catch(Exception e){
                Log.err(e);
                Log.err("Failed to generate shadow for @", block);
            }
        });
    }

    private void process(Block block){
        Seq<TextureRegion> regions = new Seq<>();
        if(block.variants > 0){
            block.load();
            block.loadIcon();
            for(TextureRegion region : block.variantRegions()){
                if(region.found()) regions.add(region);
            }
        }else if(block.region.found()){
            regions.add(block.region);
        }

        for(int i = 0; i < regions.size; i++){
            String name = block.name + "-shadow" + (block.variants > 0 ? (i + 1) : "");
            if(Tools.atlas.has(name)) continue; //hand-made shadow takes precedence
            Pixmap base = pixmap(regions.get(i));
            if(base == null) continue;

            int width = base.width, height = base.height;

            float[][] alpha = new float[height][width];
            for(int x = 0; x < width; x++){
                for(int y = 0; y < height; y++){
                    alpha[y][x] = base.getA(x, y) / 255f;
                }
            }

            blur(alpha);

            //same size as the source sprite, blurred black silhouette
            Pixmap shadow = new Pixmap(width, height);
            for(int x = 0; x < width; x++){
                for(int y = 0; y < height; y++){
                    if(alpha[y][x] > 0.001f){
                        shadow.setRaw(x, y, Color.rgba8888(0f, 0f, 0f, alpha[y][x]));
                    }
                }
            }

            save(shadow, name, SpriteProcessor.propShadowFile(name.startsWith("gr-") ? name.substring("gr-".length()) : name));
        }
    }

    /** In-place separable box blur with a blurPower×blurPower window. */
    private static void blur(float[][] map){
        int width = map[0].length, height = map.length;
        int half = blurPower / 2;
        float[] tmp = new float[Math.max(width, height)];

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                float sum = 0;
                for(int k = -half; k < blurPower - half; k++){
                    sum += map[y][Mathf.clamp(x + k, 0, width - 1)];
                }
                tmp[x] = sum / blurPower;
            }
            System.arraycopy(tmp, 0, map[y], 0, width);
        }

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                float sum = 0;
                for(int k = -half; k < blurPower - half; k++){
                    sum += map[Mathf.clamp(y + k, 0, height - 1)][x];
                }
                tmp[y] = sum / blurPower;
            }
            for(int y = 0; y < height; y++) map[y][x] = tmp[y];
        }
    }

    private static Pixmap pixmap(TextureRegion region){
        return region instanceof GeneratedAtlas.GeneratedRegion gen && gen.found() ? gen.pixmap() : null;
    }

    private static void save(Pixmap pixmap, String name, Fi file){
        new GeneratedAtlas.GeneratedRegion(name, pixmap, file).save(true);
    }
}
