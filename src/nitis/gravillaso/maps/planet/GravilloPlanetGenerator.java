package nitis.gravillaso.maps.planet;

import arc.graphics.Color;
import arc.math.geom.Vec3;
import arc.util.Log;
import arc.util.noise.Ridged;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Sector;
import mindustry.world.Block;
import mindustry.world.Tiles;
import mindustry.world.WorldParams;

import static mindustry.Vars.state;

public class GravilloPlanetGenerator extends PlanetGenerator {
    public boolean allowNumberedLaunch(Sector sector) {
        return sector != null && sector.hasBase() && !sector.isAttacked() && (sector.info.bestCoreType.size >= 4 ||
                sector.isBeingPlayed() && state.rules.defaultTeam.cores().contains(b -> b.block.size >= 4));
    }

    @Override
    public boolean allowLanding(Sector sector) {
        return sector != null && sector.planet.allowLaunchToNumbered && (sector.hasBase() || sector.near().contains(this::allowNumberedLaunch));
    }

    private Block getFloor(Vec3 pos) {
        return Blocks.ice;
    }

    @Override
    public float getHeight(Vec3 pos) {
        return 0.125f;
    }

    @Override
    public void getColor(Vec3 pos, Color out) {

        Block floor = getFloor(pos);

        out.set(floor.mapColor);

        float noise = 1f + Simplex.noise3d(1, 4, 0.5f, 0.2f, pos.x, pos.y, pos.z) * 0.15f;

        out.mul(noise);
    }

    @Override
    protected void generate() {
        distort(6, 12);

        median(3);
    }
}
