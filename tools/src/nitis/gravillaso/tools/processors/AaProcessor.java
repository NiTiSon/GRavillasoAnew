package nitis.gravillaso.tools.processors;

import arc.files.*;
import arc.graphics.*;
import arc.util.*;

import nitis.gravillaso.tools.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mirrors the AA pass from mindustry's tools/build.gradle: applies Pixmaps.antialias to every
 * sprite, skipping UI icon-* files, .9.png nine-patches and 'aaaa' (pre-padded) sprites.
 * Runs last so generated sprites are smoothed too.
 */
public class AaProcessor implements SpriteProcessor{
    @Override
    public void process(){
        AtomicInteger applied = new AtomicInteger();
        AtomicInteger ignored = new AtomicInteger();
        Fi.get("sprites").walk(file -> {
            if(!file.extEquals("png")) return;

            String path = file.path().replace("\\", "/");
            if((path.contains("/ui/") && file.name().startsWith("icon-")) || file.name().contains(".9.png") || file.name().contains("aaaa")){
                Log.info("[AA] Skipped @", path);
                ignored.getAndIncrement();
            }

            Pixmap pixmap = new Pixmap(file);
            Pixmaps.antialias(pixmap);
            file.writePng(pixmap);
            pixmap.dispose();
            applied.getAndIncrement();
        });

        Log.info("Antialiased @ sprites, @ ignored.", applied.get(), ignored.get());
    }
}