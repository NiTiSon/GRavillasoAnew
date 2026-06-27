package pack;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.g2d.TextureRegion;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Team;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import mindustry.world.Block;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.legacy.LegacyBlock;
import static mindustry.Vars.content;
import static pack.ImagePacker.*;

public class Generators {
    static final int maxUiIcon = 128;

    public static void run() {
        ObjectMap<Block, Pixmap> gens = new ObjectMap<>();

        generate("block-icons", () -> {
            Pixmap colors = new Pixmap(content.blocks().size, 1);

            for (Block block : content.blocks()) {
                if (!has(block.name)) continue;
                if (block.isAir() || block instanceof ConstructBlock || block instanceof OreBlock || block instanceof LegacyBlock) continue;

                Seq<TextureRegion> toOutline = new Seq<>();
                block.getRegionsToOutline(toOutline);

                TextureRegion[] regions = block.getGeneratedIcons();

                for (TextureRegion region : block.makeIconRegions()) {
                    GenRegion gen = (GenRegion)region;
                    save(get(region).outline(block.outlineColor, block.outlineRadius), gen.name + "-outline");
                }

                Pixmap shardTeamTop = null;

                if(block.teamRegion.found()){
                    Pixmap teamr = get(block.teamRegion);

                    for(Team team : Team.all){
                        if(team.hasPalette){
                            Pixmap out = new Pixmap(teamr.width, teamr.height);
                            teamr.each((x, y) -> {
                                int color = teamr.getRaw(x, y);
                                int index = color == 0xffffffff ? 0 : color == 0xdcc6c6ff ? 1 : color == 0x9d7f7fff ? 2 : -1;
                                out.setRaw(x, y, index == -1 ? teamr.getRaw(x, y) : team.palettei[index]);
                            });
                            save(out, block.name + "-team-" + team.name);

                            if(team == Team.sharded){
                                shardTeamTop = out;
                            }
                        }
                    }
                }

                for(TextureRegion region : toOutline){
                    Pixmap pix = get(region).outline(block.outlineColor, block.outlineRadius);
                    save(pix, ((GenRegion)region).name + "-outline");
                }

                if(regions.length == 0){
                    continue;
                }

                try{
                    Pixmap last = null;
                    if(block.outlineIcon){
                        GenRegion region = (GenRegion)regions[block.outlinedIcon >= 0 ? block.outlinedIcon : regions.length -1];
                        Pixmap base = get(region);
                        Pixmap out = last = base.outline(block.outlineColor, block.outlineRadius);

                        //do not run for legacy ones
                        if(block.outlinedIcon >= 0){
                            //prevents the regions above from being ignored/invisible/etc
                            for(int i = block.outlinedIcon + 1; i < regions.length; i++){
                                out.draw(get(regions[i]), true);
                            }
                        }

                        region.path.delete();

                        //1 pixel of padding to prevent edges with linear filtering
                        int padding = 1;
                        Pixmap padded = new Pixmap(base.width + padding*2, base.height + padding*2);
                        padded.draw(base, padding, padding);
                        padded = padded.outline(block.outlineColor, block.outlineRadius);

                        save(padded, region.name);
                    }

                    Pixmap image;

                    if(regions[0].found()){
                        image = get(regions[0]);

                        int i = 0;
                        for(TextureRegion region : regions){
                            i++;
                            if(i != regions.length || last == null){
                                image.draw(get(region), true);
                            }else{
                                image.draw(last, true);
                            }

                            //draw shard (default team top) on top of first sprite
                            if(region == block.teamRegions[Team.sharded.id] && shardTeamTop != null){
                                image.draw(shardTeamTop, true);
                            }
                        }

                        if(!(regions.length == 1 && regions[0] == Core.atlas.find(block.name) && shardTeamTop == null)){
                            save(image, "block-" + block.name + "-full");
                        }

                        saveScaled(image, "../ui/block-" + block.name + "-ui", Math.min(image.width, maxUiIcon));
                    } else if (gens.containsKey(block)){
                        image = gens.get(block);
                    }else{
                        continue;
                    }

                    boolean hasEmpty = false;
                    Color average = new Color(), c = new Color();
                    float asum = 0f;
                    for(int x = 0; x < image.width; x++){
                        for(int y = 0; y < image.height; y++){
                            Color color = c.set(image.get(x, y));
                            average.r += color.r*color.a;
                            average.g += color.g*color.a;
                            average.b += color.b*color.a;
                            asum += color.a;
                            if(color.a < 0.9f){
                                hasEmpty = true;
                            }
                        }
                    }

                    average.mul(1f / asum);

                    if (block instanceof Floor && !((Floor)block).wallOre){
                        average.mul(0.77f);
                    }else {
                        average.mul(1.1f);
                    }
                    //encode square sprite in alpha channel
                    average.a = hasEmpty ? 0.1f : 1f;
                    colors.setRaw(block.id, 0, average.rgba());
                } catch (NullPointerException e) {
                    Log.err("Block &ly'@'&lr has an null region!", block);
                }
            }

            save(colors, "../../../assets/sprites/block_colors");
        });

        generate("item-icons", () -> {
            for(UnlockableContent item : Seq.<UnlockableContent>withArrays(content.items(), content.liquids(), content.statusEffects())){
                String spriteName = item.getContentType().name() + "-" + item.name;
                if(item instanceof StatusEffect && !has(spriteName)){
                    continue;
                }
                if(!(item instanceof StatusEffect) && !has(item.name)){
                    continue;
                }

                Pixmap base = get(item instanceof StatusEffect ? spriteName : item.name);
                //tint status effect icon color
                if(item instanceof StatusEffect){
                    StatusEffect stat = (StatusEffect)item;
                    Pixmap tint = base;
                    base.each((x, y) -> tint.setRaw(x, y, Color.muli(tint.getRaw(x, y), stat.color.rgba())));

                    //outline the image
                    Pixmap container = new Pixmap(tint.width + 6, tint.height + 6);
                    container.draw(base, 3, 3, true);
                    base = container.outline(Pal.gray, 3);
                }

                save(base, "../ui/" + item.getContentType().name() + "-" + item.name + "-ui");
            }
        });
    }
}
