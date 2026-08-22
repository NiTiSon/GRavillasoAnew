package nitis.gravillaso.tools.processors;

import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.TextureAtlas.AtlasRegion;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.noise.*;

import mindustry.gen.*;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

import nitis.gravillaso.tools.*;
import nitis.gravillaso.tools.GeneratedAtlas.GeneratedRegion;

import static mindustry.Vars.*;

/**
 * Mirrors mindustry.tools.Generators#generate("unit-icons"): outlines leg/tread/joint/weapon
 * regions, composites the full unit sprite, generates random wrecks, and writes '-full',
 * '-icon-logic' and '-ui' icons. Runs before AA/bleed.
 */
public class UnitIconProcessor implements SpriteProcessor{
    static final int maxUiIcon = 128, logicIconSize = 64;

    @Override
    public void process(){
        content.units().each(type -> type.minfo != null && type.minfo.mod == Tools.mod
            && !(type.internal && !type.internalGenerateSprites), type -> {
            try{
                process(type);
            }catch(Exception e){
                Log.err(e);
                Log.err("Failed to generate icons for @", type);
            }
        });
    }

    private void process(UnitType type){
        ObjectSet<String> outlined = new ObjectSet<>();

        type.load();
        if(!type.previewRegion.found()) return;

        Unit sample = type.constructor.get();

        Func<Pixmap, Pixmap> outline = i -> i.outline(type.outlineColor, 3);
        Cons<TextureRegion> outliner = t -> {
            Pixmap pix = get(t);
            if(pix != null){
                save(outline.get(pix), name(t));
            }
        };

        Seq<TextureRegion> toOutline = new Seq<>();
        type.getRegionsToOutline(toOutline);

        for(TextureRegion region : toOutline){
            save(get(region).outline(type.outlineColor, type.outlineRadius), name(region) + "-outline", SpriteProcessor.outlineFile(name(region) + "-outline"));
        }

        Seq<Weapon> weapons = type.weapons;
        weapons.each(Weapon::load);
        weapons.removeAll(w -> !w.region.found());

        for(Weapon weapon : weapons){
            if(outlined.add(weapon.name) && Tools.atlas.has(weapon.name)){
                //only non-top weapons need separate outline sprites (this is mostly just mechs)
                if(!weapon.top || weapon.parts.contains(p -> p.under)){
                    save(outline.get(find(weapon.name)), weapon.name + "-outline", SpriteProcessor.outlineFile(weapon.name + "-outline"));
                }else{
                    //replace weapon with outlined version, no use keeping standard around
                    outliner.get(weapon.region);
                }
            }
        }

        //generate tank animation
        if(sample instanceof Tankc){
            Pixmap pix = get(type.treadRegion);

            for(int r = 0; r < type.treadRects.length; r++){
                Rect treadRect = type.treadRects[r];
                //slice is always 1 pixel wide
                Pixmap slice = pix.crop((int)(treadRect.x + pix.width/2f), (int)(treadRect.y + pix.height/2f), 1, (int)treadRect.height);
                int frames = type.treadFrames;
                for(int i = 0; i < frames; i++){
                    int pullOffset = type.treadPullOffset;
                    Pixmap frame = new Pixmap(slice.width, slice.height);
                    for(int y = 0; y < slice.height; y++){
                        int idx = y + i;
                        if(idx >= slice.height){
                            idx -= slice.height;
                            idx += pullOffset;
                            idx = Mathf.mod(idx, slice.height);
                        }

                        frame.setRaw(0, y, slice.getRaw(0, idx));
                    }
                    save(frame, type.name + "-treads" + r + "-" + i, Fi.get("sprites").child(type.name + "-treads" + r + "-" + i + ".png"));
                }
            }
        }

        outliner.get(type.jointRegion);
        outliner.get(type.footRegion);
        outliner.get(type.legBaseRegion);
        outliner.get(type.baseJointRegion);
        if(sample instanceof Legsc) outliner.get(type.legRegion);
        if(sample instanceof Tankc) outliner.get(type.treadRegion);

        Pixmap image = type.segments > 0 ? get(type.segmentRegions[0]) : outline.get(get(type.previewRegion));

        Func<Weapon, Pixmap> weaponRegion = weapon -> Tools.atlas.has(weapon.name + "-preview") ? find(weapon.name + "-preview") : get(weapon.region);
        Cons2<Weapon, Pixmap> drawWeapon = (weapon, pixmap) ->
        image.draw(weapon.flipSprite ? pixmap.flipX() : pixmap,
        (int)(weapon.x / Draw.scl + image.width / 2f - weapon.region.width / 2f),
        (int)(-weapon.y / Draw.scl + image.height / 2f - weapon.region.height / 2f),
        true
        );

        boolean anyUnder = false;

        //draw each extra segment on top before it is saved as outline
        if(sample instanceof Crawlc){
            for(int i = 0; i < type.segments; i++){
                save(outline.get(get(type.segmentRegions[i])), type.name + "-segment-outline" + i, SpriteProcessor.outlineFile(type.name + "-segment-outline" + i));

                if(i > 0){
                    drawCenter(image, get(type.segmentRegions[i]));
                }
            }
            save(image, type.name);
        }

        //outline is currently never needed, although it could theoretically be necessary
        if(type.needsBodyOutline()){
            save(image, type.name + "-outline", SpriteProcessor.outlineFile(type.name + "-outline"));
        }else if(type.segments == 0){
            //the outlined version replaces the source sprite
            save(outline.get(get(type.region)), name(type.region));
        }

        //draw weapons that are under the base
        for(Weapon weapon : weapons.select(w -> w.layerOffset < 0)){
            drawWeapon.get(weapon, outline.get(weaponRegion.get(weapon)));
            anyUnder = true;
        }

        //draw over the weapons under the image
        if(anyUnder){
            image.draw(outline.get(get(type.previewRegion)), true);
        }

        //draw treads
        if(sample instanceof Tankc){
            Pixmap treads = outline.get(get(type.treadRegion));
            image.draw(treads, image.width / 2 - treads.width / 2, image.height / 2 - treads.height / 2, true);
            image.draw(get(type.previewRegion), true);
        }

        //draw mech parts
        if(sample instanceof Mechc){
            drawCenter(image, get(type.baseRegion));
            drawCenter(image, get(type.legRegion));
            drawCenter(image, get(type.legRegion).flipX());
            image.draw(get(type.previewRegion), true);
        }

        //draw weapon outlines on base
        for(Weapon weapon : weapons){
            //skip weapons under unit
            if(weapon.layerOffset < 0) continue;

            drawWeapon.get(weapon, outline.get(weaponRegion.get(weapon)));
        }

        //draw base region on top to mask weapons
        if(type.drawCell) image.draw(get(type.previewRegion), true);

        if(type.drawCell){
            Pixmap baseCell = get(type.cellRegion);
            if(baseCell == null) baseCell = get(type.previewRegion);
            Pixmap cell = baseCell.copy();

            cell.replace(in -> in == 0xffffffff ? 0xffa664ff : in == 0xdcc6c6ff || in == 0xdcc5c5ff ? 0xd06b53ff : 0);

            image.draw(cell, image.width / 2 - cell.width / 2, image.height / 2 - cell.height / 2, true);
        }

        for(Weapon weapon : weapons){
            //skip weapons under unit
            if(weapon.layerOffset < 0) continue;

            Pixmap reg = weaponRegion.get(weapon);
            Pixmap wepReg = weapon.top ? outline.get(reg) : reg;

            drawWeapon.get(weapon, wepReg);

            if(weapon.cellRegion.found()){
                Pixmap weaponCell = get(weapon.cellRegion);
                weaponCell.replace(in -> in == 0xffffffff ? 0xffa664ff : in == 0xdcc6c6ff || in == 0xdcc5c5ff ? 0xd06b53ff : 0);
                drawWeapon.get(weapon, weaponCell);
            }
        }

        save(image, "unit-" + type.name + "-full", SpriteProcessor.fullFile("unit-" + type.name + "-full"));

        Rand rand = new Rand();
        rand.setSeed(type.name.hashCode());

        //generate random wrecks

        int splits = 3;
        float degrees = rand.random(360f);
        float offsetRange = Math.max(image.width, image.height) * 0.15f;
        Vec2 offset = new Vec2(1, 1).rotate(rand.random(360f)).setLength(rand.random(0, offsetRange)).add(image.width/2f, image.height/2f);

        Pixmap[] wrecks = new Pixmap[splits];
        for(int i = 0; i < wrecks.length; i++){
            wrecks[i] = new Pixmap(image.width, image.height);
        }

        VoronoiNoise vn = new VoronoiNoise(type.id, true);

        image.each((x, y) -> {
            //add darker cracks on top
            boolean rValue = Math.max(Ridged.noise2d(1, x, y, 3, 1f / (20f + image.width/8f)), 0) > 0.16f;
            //cut out random chunks with voronoi
            boolean vval = vn.noise(x, y, 1f / (14f + image.width/40f)) > 0.47;

            float dst = offset.dst(x, y);
            //distort edges with random noise
            float noise = (float)Noise.rawNoise(dst / (9f + image.width/70f)) * (60 + image.width/30f);
            int section = (int)Mathf.clamp(Mathf.mod(offset.angleTo(x, y) + noise + degrees, 360f) / 360f * splits, 0, splits - 1);
            if(!vval) wrecks[section].setRaw(x, y, Color.muli(image.getRaw(x, y), rValue ? 0.7f : 1f));
        });

        for(int i = 0; i < wrecks.length; i++){
            save(wrecks[i], type.name + "-wreck" + i, Fi.get("sprites").child("rubble").child(type.name + "-wreck" + i + ".png"));
        }

        int maxd = Math.min(Math.max(image.width, image.height), maxUiIcon);
        Pixmap fit = new Pixmap(maxd, maxd);
        drawScaledFit(fit, image);

        saveScaled(fit, type.name + "-icon-logic", logicIconSize, SpriteProcessor.uiFile(type.name + "-icon-logic"));
        save(fit, "unit-" + type.name + "-ui", SpriteProcessor.uiFile("unit-" + type.name + "-ui"));
    }

    /** Returns the backing pixmap of an atlas-generated region, or null when missing. */
    private static Pixmap get(TextureRegion region){
        return region instanceof GeneratedRegion gen && gen.found() ? gen.pixmap() : null;
    }

    private static Pixmap find(String name){
        return Tools.atlas.has(name) ? Tools.atlas.find(name).pixmap() : null;
    }

    private static String name(TextureRegion region){
        return ((AtlasRegion)region).name;
    }

    private static void save(Pixmap pixmap, String name){
        GeneratedRegion existing = Tools.atlas.find(name);
        save(pixmap, name, existing.file != null ? existing.file : Fi.get("sprites").child(name + ".png"));
    }

    private static void save(Pixmap pixmap, String name, Fi file){
        new GeneratedRegion(name, pixmap, file).save(true);
    }

    private static void drawCenter(Pixmap dst, Pixmap src){
        dst.draw(src, (dst.width - src.width) / 2, (dst.height - src.height) / 2, true);
    }

    private static void drawScaledFit(Pixmap dest, Pixmap src){
        float factor = Math.min((float)dest.width / src.width, (float)dest.height / src.height);
        Pixmap scaled = Pixmaps.scale(src, Mathf.round(src.width * factor), Mathf.round(src.height * factor), true);
        dest.draw(scaled, (dest.width - scaled.width) / 2, (dest.height - scaled.height) / 2);
        scaled.dispose();
    }

    private static void saveScaled(Pixmap pixmap, String name, int maxSize, Fi file){
        if(pixmap.width > maxSize){
            save(Pixmaps.scale(pixmap, maxSize, Mathf.round((float)maxSize / pixmap.width * pixmap.height), true), name, file);
        }else{
            save(pixmap, name, file);
        }
    }
}
