package nitis.gravillaso.tools.processors;

import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;

import mindustry.graphics.*;

import nitis.gravillaso.tools.*;
import nitis.gravillaso.tools.GeneratedAtlas.*;

import static mindustry.Vars.*;

/**
 * Generates the '-ui' icons used by the tech tree/database, mirroring
 * mindustry.tools.Generators#generate("item-icons"). Regions must be named
 * '<contentType>-<name>-ui' (e.g. 'item-gr-cobalt-ui') to match
 * UnlockableContent.loadIcon(), and files must carry that same name so
 * Mods.packSprites doesn't add the mod prefix (it already contains it after the first hyphen).
 */
public class ItemProcessor implements SpriteProcessor{
    @Override
    public void process(){
        // Items
        content.items().each(u -> u.minfo != null && u.minfo.mod == Tools.mod, item -> {
            String uiIconName = "item-" + item.name + "-ui";
            generateUiIcon(uiIconName, item.name);
        });

        // Liquids
        content.liquids().each(u -> u.minfo != null && u.minfo.mod == Tools.mod, liquid -> {
            String uiIconName = "liquid-" + liquid.name + "-ui";
            generateUiIcon(uiIconName, liquid.name);
        });

        // Status Effects (tinted + outlined base is regenerated too)
        content.statusEffects().each(u -> u.minfo != null && u.minfo.mod == Tools.mod, effect -> {
            GeneratedRegion baseRegion = Tools.atlas.find(effect.name);
            if(!baseRegion.found()){
                Log.warn("Base region not found for status effect '@'. Skipping.", effect.name);
                return;
            }

            Pixmap tinted = baseRegion.pixmap().copy();
            tinted.each((x, y) -> tinted.setRaw(x, y, Color.muli(tinted.getRaw(x, y), effect.color.rgba())));

            Pixmap container = new Pixmap(tinted.width + 6, tinted.height + 6);
            container.draw(tinted, 3, 3, true);

            Pixmap finalImage = Pixmaps.outline(new PixmapRegion(container), Pal.gray, 3);

            // Overwrite original region with tinted and outlined version
            new GeneratedRegion(baseRegion.name, finalImage, baseRegion.file).save(false);

            new GeneratedRegion("status-" + effect.name + "-ui", finalImage.copy(), SpriteProcessor.uiFile("status-" + effect.name + "-ui")).save(true);

            tinted.dispose();
            container.dispose();
            finalImage.dispose();
        });
    }

    private static void generateUiIcon(String uiIconName, String baseRegionName){
        GeneratedRegion baseRegion = Tools.atlas.find(baseRegionName);
        if(!baseRegion.found()) return;
        if(Tools.atlas.has(uiIconName)) return;

        new GeneratedRegion(uiIconName, baseRegion.pixmap().copy(), SpriteProcessor.uiFile(uiIconName)).save(true);
    }
}
