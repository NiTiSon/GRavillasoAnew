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
    /** Gaussian kernel radius in pixels. */
    static final int blurPower = 22;
    static final int padding = 6;

    @Override
    public void process(){
        content.blocks().each(b -> b.customShadow && b.minfo != null && b.minfo.mod == Tools.mod, block -> {
            try{
                process(block);
            }catch(Throwable e){
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

            int width = base.width + padding * 2, height = base.height + padding * 2;

            //alpha map padded with empty margin so the blur can spread past the art edges
            float[][] alpha = new float[height][width];
            for(int x = 0; x < base.width; x++){
                for(int y = 0; y < base.height; y++){
                    alpha[y + padding][x + padding] = base.getA(x, y) / 255f;
                }
            }

            blur(alpha);

            Pixmap shadow = new Pixmap(width, height);
            for(int x = 0; x < width; x++){
                for(int y = 0; y < height; y++){
                    if(alpha[y][x] > 0.001f){
                        shadow.setRaw(x, y, Color.rgba8888(0f, 0f, 0f, blurFade(alpha[y][x])));
                    }
                }
            }

            save(shadow, name, SpriteProcessor.propShadowFile(name.startsWith("gr-") ? name.substring("gr-".length()) : name));
        }
    }

    static float blurFade(float value){
        return value;
        //return -Mathf.pow(1 - value, 3) + 1;
    }

    /** In-place separable Gaussian blur with a ±blurPower kernel. */
    private static void blur(float[][] map){
        int width = map[0].length, height = map.length;
        float sigma = Math.max(1f, blurPower / 2f);

        //normalized 1D Gaussian kernel, applied horizontally then vertically
        float[] kernel = new float[blurPower * 2 + 1];
        float ksum = 0;
        for(int k = -blurPower; k <= blurPower; k++){
            kernel[k + blurPower] = (float)Math.exp(-(k * k) / (2f * sigma * sigma));
            ksum += kernel[k + blurPower];
        }
        for(int i = 0; i < kernel.length; i++) kernel[i] /= ksum;

        float[] tmp = new float[Math.max(width, height)];

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                float acc = 0;
                for(int k = -blurPower; k <= blurPower; k++){
                    acc += map[y][Mathf.clamp(x + k, 0, width - 1)] * kernel[k + blurPower];
                }
                tmp[x] = acc;
            }
            System.arraycopy(tmp, 0, map[y], 0, width);
        }

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                float acc = 0;
                for(int k = -blurPower; k <= blurPower; k++){
                    acc += map[Mathf.clamp(y + k, 0, height - 1)][x] * kernel[k + blurPower];
                }
                tmp[y] = acc;
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
