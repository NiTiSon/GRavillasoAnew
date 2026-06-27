package nitis.gravillaso.maps.planet;

import arc.graphics.Color;
import arc.math.geom.Vec3;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Sector;
import mindustry.world.Block;
import mindustry.world.TileGen;
import nitis.gravillaso.content.GRBlocks;

import static mindustry.Vars.state;

public class GravilloPlanetGenerator extends PlanetGenerator {
    {
        defaultLoadout = new Schematic(
                Seq.with(new Schematic.Stile(GRBlocks.coreFortress, 0, 0, null, (byte)0)),
                new StringMap(), 4, 4
        );
    }

    // ice, snow, calcite, perite?,
    // cryofluid lakes
    // weather: snow, snowstorm
    // buildings?

    public boolean allowNumberedLaunch(Sector sector) {
        return sector != null && sector.hasBase() && !sector.isAttacked() && (sector.info.bestCoreType.size >= 4 ||
                sector.isBeingPlayed() && state.rules.defaultTeam.cores().contains(b -> b.block.size >= 4));
    }

    @Override
    public boolean allowLanding(Sector sector) {
        return sector != null && sector.planet.allowLaunchToNumbered && (sector.hasBase() || sector.near().contains(this::allowNumberedLaunch));
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

    private Block getFloor(Vec3 pos) {
        float noise = Simplex.noise3d(seed, 4, 0.5f, 0.03f, pos.x, pos.y, pos.z);
        return noise < -0.05f ? Blocks.snow : Blocks.iceSnow;
    }

    @Override
    public void genTile(Vec3 pos, TileGen result) {
        result.floor = getFloor(pos);
    }

    @Override
    protected void generate() {
        distort(6, 12);
        median(3);

        blend(Blocks.snow, Blocks.iceSnow, 0.008f);

        ores(Seq.with(Blocks.oreLead));

        decoration(0.06f);

        Schematics.placeLaunchLoadout(width / 2, height / 2);
    }
}
