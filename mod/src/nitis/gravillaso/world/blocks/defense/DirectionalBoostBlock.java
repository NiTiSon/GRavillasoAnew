package nitis.gravillaso.world.blocks.defense;

/** Basic interface for any block that boost.*/
public interface DirectionalBoostBlock{
    float boost();
    /** @return boost as a fraction of max heat */
    float boostFrac();
}