package nitis.gravillaso.tools.processors;

import arc.files.*;
import arc.graphics.*;
import arc.util.*;

import nitis.gravillaso.tools.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mirrors the game's runtime bleed (Mods.packSprites: Pixmaps.bleed(pix, 2) when NOT pregenerated).
 * When the mod ships with pregenerated=true the game skips that pass, so sprites must arrive
 * pre-bled or semi-transparent edges fringe into a dark halo under linear filtering.
 * Runs last, after AA.
 */
public class BleedProcessor implements SpriteProcessor{
    @Override
    public void process(){
        AtomicInteger applied = new AtomicInteger();

        Fi.get("sprites").walk(file -> {
            if(!file.extEquals("png")) return;

            Pixmap pixmap = new Pixmap(file);
            Pixmaps.bleed(pixmap, 2);
            file.writePng(pixmap);
            pixmap.dispose();
            applied.getAndIncrement();
        });

        Log.info("Bled @ sprites.", applied.get());
    }
}
