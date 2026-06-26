package pack;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureAtlas.*;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.mock.MockSettings;
import arc.struct.ObjectMap;
import arc.util.*;
import mindustry.Vars;
import mindustry.core.ContentLoader;
import mindustry.ctype.UnlockableContent;

public class ImagePacker {
    static ObjectMap<String, PackIndex> cache = new ObjectMap<>();

    public static void main(String[] args) {
        Vars.headless = true;
        // makes PNG loading slightly faster
        // ArcNativesLoader.load();

        // fixSubdirectory("blocks/environment/character-overlay");
        // fixSubdirectory("blocks/environment/rune-overlay");

        Core.settings = new MockSettings();
        Log.logger = new Log.NoopLogHandler();
        Vars.content = new ContentLoader();
        Vars.content.createBaseContent();
        Vars.content.init();
        Log.logger = new Log.DefaultLogHandler();

        Fi.get("../assets-raw/sprites_out").walk(path -> {
            if(!path.extEquals("png")) return;

            cache.put(path.nameWithoutExtension(), new PackIndex(path));
        });

        Core.atlas = new TextureAtlas() {
            @Override
            public AtlasRegion find(String name) {
                if (!cache.containsKey(name)) {
                    GenRegion region = new GenRegion(name, null);
                    region.invalid = true;
                    return region;
                }

                PackIndex index = cache.get(name);
                if (index.pixmap == null) {
                    index.pixmap = new Pixmap(index.file);
                    index.region = new GenRegion(name, index.file){{
                        width = index.pixmap.width;
                        height = index.pixmap.height;
                        u2 = v2 = 1f;
                        u = v = 0f;
                    }};
                }
                return index.region;
            }

            @Override
            public AtlasRegion find(String name, TextureRegion def) {
                if (!cache.containsKey(name)) {
                    return (AtlasRegion)def;
                }
                return find(name);
            }

            @Override
            public AtlasRegion find(String name, String def) {
                if (!cache.containsKey(name)) {
                    return find(def);
                }
                return find(name);
            }

            @Override
            public PixmapRegion getPixmap(AtlasRegion region) {
                return new PixmapRegion(get(region.name));
            }

            @Override
            public boolean has(String s) {
                return cache.containsKey(s);
            }
        };

        Time.mark();
        Vars.content.load();
        Generators.run();
        Log.info("&ly[Generator]&lc Total time to generate: &lg@&lcms", Time.elapsed());
    }

    static String texname(UnlockableContent c) {
        return c.getContentType() + "-" + c.name + "-ui";
    }

    static void generate(String name, Runnable run) {
        Time.mark();
        run.run();
        Log.info("&ly[Generator]&lc Time to generate &lm@&lc: &lg@&lcms", name, Time.elapsed());
    }

    static Pixmap get(String name) {
        return get(Core.atlas.find(name));
    }

    static boolean has(String name) {
        return Core.atlas.has(name);
    }

    static Pixmap get(TextureRegion region) {
        validate(region);

        return cache.get(((AtlasRegion)region).name).pixmap.copy();
    }

    static void save(Pixmap pix, String path) {
        Fi.get(path + ".png").writePng(pix);
    }

    static void drawCenter(Pixmap pix, Pixmap other) {
        pix.draw(other, pix.width/2 - other.width/2, pix.height/2 - other.height/2, true);
    }

    static void saveScaled(Pixmap pix, String name, int size) {
        Pixmap scaled = new Pixmap(size, size);
        //TODO bad linear scaling
        scaled.draw(pix, 0, 0, pix.width, pix.height, 0, 0, size, size, true, true);
        save(scaled, name);
    }

    static void drawScaledFit(Pixmap base, Pixmap image) {
        Vec2 size = Scaling.fit.apply(image.width, image.height, base.width, base.height);
        int wx = (int)size.x, wy = (int)size.y;
        //TODO bad linear scaling
        base.draw(image, 0, 0, image.width, image.height, base.width/2 - wx/2, base.height/2 - wy/2, wx, wy, true, true);
    }

    static void delete(String name) {
        ((GenRegion)Core.atlas.find(name)).path.delete();
    }

    static void replace(String name, Pixmap image) {
        replace(name, name, image);
    }

    static void replace(String path, String name, Pixmap image) {
        Fi.get(path + ".png").writePng(image);
        ((GenRegion)Core.atlas.find(name)).path.delete();
    }

    static void replace(TextureRegion region, Pixmap image) {
        replace(((GenRegion)region).name, image);
    }

    static void err(String message, Object... args) {
        throw new IllegalArgumentException(Strings.format(message, args));
    }

    static void validate(TextureRegion region) {
        if(((GenRegion)region).invalid){
            ImagePacker.err("Region does not exist: @", ((GenRegion)region).name);
        }
    }

    static class GenRegion extends AtlasRegion {
        boolean invalid;
        Fi path;

        GenRegion(String name, Fi path) {
            if(name == null) throw new IllegalArgumentException("name is null");
            this.name = name;
            this.path = path;
        }

        @Override
        public boolean found() {
            return !invalid;
        }
    }

    static class PackIndex {
        @Nullable AtlasRegion region;
        @Nullable Pixmap pixmap;
        Fi file;

        public PackIndex(Fi file){
            this.file = file;
        }
    }
}
