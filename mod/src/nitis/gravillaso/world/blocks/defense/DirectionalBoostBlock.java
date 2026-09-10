package nitis.gravillaso.world.blocks.defense;

/** Basic interface for any block that carries boost.*/
public interface DirectionalBoostBlock{
    float boost();
    /** @return boost as a fraction of max boost */
    float boostFrac();
}