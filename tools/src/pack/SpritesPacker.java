package pack;

import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.packer.TexturePacker;
import arc.struct.Seq;

import java.util.Arrays;

public class SpritesPacker {

    static int getRGB(Pixmap image, int ix, int iy) {
        return image.getRaw(
            Math.max(Math.min(ix, image.width - 1), 0),
            Math.max(Math.min(iy, image.height - 1), 0)
        );
    }

    static void antialias(Fi file) {
        Pixmap image = new Pixmap(file);
        Pixmap out = image.copy();

        Color color = new Color();
        Color sum = new Color();
        Color suma = new Color();
        int[] p = new int[9];

        for (int x = 0; x < image.width; x++) {
            for (int y = 0; y < image.height; y++) {
                int A = getRGB(image, x - 1, y + 1);
                int B = getRGB(image, x, y + 1);
                int C = getRGB(image, x + 1, y + 1);
                int D = getRGB(image, x - 1, y);
                int E = getRGB(image, x, y);
                int F = getRGB(image, x + 1, y);
                int G = getRGB(image, x - 1, y - 1);
                int H = getRGB(image, x, y - 1);
                int I = getRGB(image, x + 1, y - 1);

                Arrays.fill(p, E);

                if (D == B && D != H && B != F) p[0] = D;
                if ((D == B && D != H && B != F && E != C) || (B == F && B != D && F != H && E != A)) p[1] = B;
                if (B == F && B != D && F != H) p[2] = F;
                if ((H == D && H != F && D != B && E != A) || (D == B && D != H && B != F && E != G)) p[3] = D;
                if ((B == F && B != D && F != H && E != I) || (F == H && F != B && H != D && E != C)) p[5] = F;
                if (H == D && H != F && D != B) p[6] = D;
                if ((F == H && F != B && H != D && E != G) || (H == D && H != F && D != B && E != I)) p[7] = H;
                if (F == H && F != B && H != D) p[8] = F;

                suma.r = 0; suma.g = 0; suma.b = 0; suma.a = 0;

                for (int val : p) {
                    color.rgba8888(val);
                    color.premultiplyAlpha();
                    suma.r += color.r;
                    suma.g += color.g;
                    suma.b += color.b;
                    suma.a += color.a;
                }

                float fm = suma.a <= 0.001f ? 0f : (1f / suma.a);
                suma.r *= fm;
                suma.g *= fm;
                suma.b *= fm;
                suma.a *= fm;

                float total = 0;
                sum.r = 0; sum.g = 0; sum.b = 0; sum.a = 0;

                for (int val : p) {
                    color.rgba8888(val);
                    float a = color.a;
                    color.lerp(suma, 1f - a);
                    sum.r += color.r;
                    sum.g += color.g;
                    sum.b += color.b;
                    sum.a += a;
                    total += 1f;
                }

                fm = 1f / total;
                sum.r *= fm;
                sum.g *= fm;
                sum.b *= fm;
                sum.a *= fm;
                out.setRaw(x, y, sum.rgba8888());
            }
        }

        file.writePng(out);
        image.dispose();
        out.dispose();
    }

    static void generateOutline(Fi file, int color, int radius) {
        Pixmap image = new Pixmap(file);
        Pixmap outlined = image.outline(color, radius);
        Fi outlineFile = file.sibling(file.nameWithoutExtension() + "-outline.png");
        outlineFile.writePng(outlined);
        image.dispose();
        outlined.dispose();
        System.out.println("  Generated outline: " + outlineFile.name());
    }

    public static void packAtlas(Fi inputDir, Fi outputDir, String name) {
        System.out.println("\nPacking atlas: " + name + ".aatls");
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.maxWidth = 4096;
        settings.maxHeight = 4096;
        settings.edgePadding = true;
        settings.bleed = true;
        settings.bleedIterations = 2;
        settings.fast = true;
        settings.pot = true;
        settings.silent = true;
        TexturePacker.process(settings, inputDir.absolutePath(), outputDir.absolutePath(), name);
        System.out.println("  Packed: " + name + ".aatls -> " + outputDir.absolutePath());
    }

    public static void main(String[] args) {
        Fi rawDir = new Fi("assets-raw/sprites");
        Fi outDir = new Fi("assets/sprites");

        if (!rawDir.exists() || !rawDir.isDirectory()) {
            System.out.println("No 'assets-raw/sprites/' directory found. Create it and place raw sprites there.");
            System.out.println("Structure: assets-raw/sprites/items/cobalt.png, assets-raw/sprites/blocks/..., etc.");
            return;
        }

        System.out.println("Processing sprites from " + rawDir.absolutePath() + " -> " + outDir.absolutePath());

        Seq<Fi> pngs = new Seq<>();
        rawDir.walk(f -> {
            if (f.extEquals("png")) pngs.add(f);
        });

        if (pngs.size == 0) {
            System.out.println("No PNG files found in " + rawDir.absolutePath());
            return;
        }

        System.out.println("Found " + pngs.size + " PNG files to process.");

        if (outDir.exists()) {
            outDir.deleteDirectory();
        }
        outDir.mkdirs();

        for (Fi src : pngs) {
            String relPath = src.path().substring(rawDir.path().length());
            Fi dest = new Fi(outDir.absolutePath() + "/" + relPath);
            dest.parent().mkdirs();

            src.copyTo(dest);

            String pathForward = src.path().replace("\\", "/");
            boolean isUISprite = pathForward.contains("/ui/");
            boolean isOutline = src.nameWithoutExtension().endsWith("-outline");
            boolean isTurret = pathForward.contains("/turrets/");
            boolean isHeatMask = src.nameWithoutExtension().endsWith("-heat");

            if (!isUISprite && !isOutline && !isHeatMask) {
                antialias(dest);
                System.out.println("  [AA] " + relPath);
            } else {
                String skipReason = isUISprite ? "UI sprite" : isOutline ? "outline" : "mask";
                System.out.println("  [--] " + relPath + " (skipped AA: " + skipReason + ")");
            }

            if (isTurret && !isOutline && !isHeatMask) {
                generateOutline(dest, 0x000000ff, 3);
            }
        }

        System.out.println("\nDone! Processed " + pngs.size + " sprites.");
    }
}
