package nitis.gravillaso.tools.processors;

import arc.graphics.*;
import arc.graphics.g2d.TextureRegion;
import arc.util.Log;

import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OverlayFloor;

import nitis.gravillaso.tools.*;

import static mindustry.Vars.*;

/**
 * Mirrors mindustry.tools.Generators#generate("edges"): multiplies the floor's sprite with
 * 'edge-stencil' to produce '<floor>-edge' blend masks. Floors with a hand-made '-edge' sprite,
 * a foreign blendGroup or drawEdgeOut == false are skipped. Requires an 'edge-stencil' sprite
 * in assets-raw (copied from vanilla).
 */
public class EdgeProcessor implements SpriteProcessor{
    @Override
    public void process(){
        Pixmap stencil = find("edge-stencil");
        if(stencil == null){
            Log.warn("[edge] No 'edge-stencil' sprite found; skipping edge generation.");
            return;
        }

        content.blocks().each(b -> b instanceof Floor && !(b instanceof OverlayFloor)
            && !b.isAir() && b.minfo != null && b.minfo.mod == Tools.mod, b -> {
            Floor floor = (Floor)b;
            if(Tools.atlas.has(floor.name + "-edge") || floor.blendGroup != floor || !floor.drawEdgeOut) return;

            try{
                floor.load();
                floor.loadIcon();

                Pixmap image = pixmap(floor);
                if(image == null){
                    Log.warn("[edge] No source sprite for @", floor.name);
                    return;
                }

                Pixmap result = new Pixmap(stencil.width, stencil.height);
                for(int x = 0; x < result.width; x++){
                    for(int y = 0; y < result.height; y++){
                        result.setRaw(x, y, Color.muli(stencil.getRaw(x, y), image.get(x % image.width, y % image.height)));
                    }
                }

                String name = floor.name + "-edge";
                new GeneratedAtlas.GeneratedRegion(name, result,
                    SpriteProcessor.edgeFile(name.startsWith("gr-") ? name.substring("gr-".length()) : name)).save(true);
            }catch(Exception e){
                Log.err("Failed to generate edge for " + floor, e);
            }
        });
    }

    /** Floor icon: first generated icon, falling back to fullIcon/first variant. */
    private static Pixmap pixmap(Floor floor){
        TextureRegion[] icons = floor.getGeneratedIcons();
        if(icons.length > 0 && icons[0].found()) return raw(icons[0]);
        if(floor.fullIcon.found()) return raw(floor.fullIcon);
        return null;
    }

    private static Pixmap raw(TextureRegion region){
        return region instanceof GeneratedAtlas.GeneratedRegion gen && gen.found() ? gen.pixmap() : null;
    }

    /** Looks up a region by bare or mod-prefixed name. */
    private static Pixmap find(String name){
        GeneratedAtlas.GeneratedRegion region = Tools.atlas.find(Tools.mod.name + "-" + name);
        if(!region.found()) region = Tools.atlas.find(name);
        return region.found() ? region.pixmap() : null;
    }
}
