package nitis.gravillaso.entities.bullet;

import arc.graphics.g2d.*;
import mindustry.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.world.*;

public class PayloadBulletType extends BulletType{
    public Block block;

    public PayloadBulletType(Block block, float speed, float damage){
        super(speed, damage);
        this.block = block;
    }

    @Override
    public void draw(Bullet b){
        super.draw(b);
        Draw.rect(block.uiIcon, b.x, b.y, block.size * Vars.tilesize, block.size * Vars.tilesize, b.rotation());
    }
}
