package nitis.gravillaso.maps.planet;

import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Sector;

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
}
