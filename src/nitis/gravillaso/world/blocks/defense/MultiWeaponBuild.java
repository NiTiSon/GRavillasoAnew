package nitis.gravillaso.world.blocks.defense;

import arc.struct.Seq;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Entityc;
import mindustry.gen.Posc;
import mindustry.gen.Teamc;
import nitis.gravillaso.type.BlockWeapon;

/** A building that can mount {@link BlockWeapon}s. */
public interface MultiWeaponBuild extends Entityc, Posc, Teamc{
    float rotation();
    float efficiency();
    boolean isControlled();
    boolean logicControlled();
    boolean canShoot();
    Seq<WeaponMount> mounts();
}
