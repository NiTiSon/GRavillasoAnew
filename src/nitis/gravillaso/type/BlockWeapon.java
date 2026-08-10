package nitis.gravillaso.type;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.Log;
import mindustry.audio.SoundLoop;
import mindustry.entities.Effect;
import mindustry.entities.Mover;
import mindustry.entities.Predict;
import mindustry.entities.Sized;
import mindustry.entities.Units;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.gen.Teamc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Weapon;
import nitis.gravillaso.GravillasoMod;
import nitis.gravillaso.world.blocks.defense.MultiWeaponBuild;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;
import static mindustry.Vars.mods;

/** A weapon that can be mounted on a {@link MultiWeaponBuild} instead of a unit. */
public class BlockWeapon extends Weapon{
    public boolean targetAir = true, targetGround = true, targetBlocks = false, targetUnderBlocks = false;

    public BlockWeapon(){
        super();
        autoTarget = true;
        controllable = false;
        rotate = true;
    }

    public BlockWeapon(String name){
        super(name);
        autoTarget = true;
        controllable = false;
        rotate = true;
    }

    @Override
    public void load(){
        super.load();
        //mod sprites are packed with a mod-name prefix, but Weapon.load() looks them up unprefixed
        var mod = mods.getMod(GravillasoMod.class);
        if(mod == null) return;
        String prefix = mod.name + "-";
        region = Core.atlas.find(prefix + name);
        heatRegion = Core.atlas.find(prefix + name + "-heat");
        cellRegion = Core.atlas.find(prefix + name + "-cell");
        outlineRegion = Core.atlas.find(prefix + name + "-outline");
        Log.info("BlockWeapon '@': region found=@ (lookup '@')", name, region.found(), prefix + name);
    }

    public void draw(MultiWeaponBuild build, WeaponMount mount){
        float z = Draw.z();
        Draw.z(z + layerOffset);

        float rotation = build.rotation() - 90,
        realRecoil = Mathf.pow(mount.recoil, recoilPow) * recoil,
        weaponRotation = rotation + (rotate ? mount.rotation : baseRotation),
        wx = build.x() + Angles.trnsx(rotation, x, y) + Angles.trnsx(weaponRotation, 0, -realRecoil),
        wy = build.y() + Angles.trnsy(rotation, x, y) + Angles.trnsy(weaponRotation, 0, -realRecoil);

        if(shadow > 0){
            Drawf.shadow(wx, wy, shadow);
        }

        float prev = Draw.xscl;
        Draw.xscl *= -Mathf.sign(flipSprite);

        if(region != null && region.found()){
            Draw.rect(region, wx, wy, weaponRotation);
        }

        if(cellRegion != null && cellRegion.found()){
            Draw.color(Pal.accent, mount.heat);
            Draw.rect(cellRegion, wx, wy, weaponRotation);
            Draw.color();
        }

        if(heatRegion != null && heatRegion.found() && mount.heat > 0){
            Draw.color(heatColor, mount.heat);
            Draw.blend(arc.graphics.Blending.additive);
            Draw.rect(heatRegion, wx, wy, weaponRotation);
            Draw.blend();
            Draw.color();
        }

        Draw.xscl = prev;
        Draw.z(z);
    }

    public void update(MultiWeaponBuild build, WeaponMount mount){
        boolean can = build.canShoot();
        mount.reload = Math.max(mount.reload - Time.delta * build.efficiency(), 0);
        mount.recoil = Mathf.approachDelta(mount.recoil, 0, build.efficiency() / recoilTime);
        if(recoils > 0){
            if(mount.recoils == null) mount.recoils = new float[recoils];
            for(int i = 0; i < recoils; i++){
                mount.recoils[i] = Mathf.approachDelta(mount.recoils[i], 0, build.efficiency() / recoilTime);
            }
        }
        mount.smoothReload = Mathf.lerpDelta(mount.smoothReload, mount.reload / reload, smoothReloadSpeed);
        mount.charge = mount.charging && shoot.firstShotDelay > 0 ? Mathf.approachDelta(mount.charge, 1, 1 / shoot.firstShotDelay) : 0;

        float warmupTarget = (can && mount.shoot) || (continuous && mount.bullet != null) || mount.charging ? 1f : 0f;
        if(linearWarmup){
            mount.warmup = Mathf.approachDelta(mount.warmup, warmupTarget, shootWarmupSpeed);
        }else{
            mount.warmup = Mathf.lerpDelta(mount.warmup, warmupTarget, shootWarmupSpeed);
        }

        float rotation = build.rotation() - 90,
        mountX = build.x() + Angles.trnsx(rotation, x, y),
        mountY = build.y() + Angles.trnsy(rotation, x, y);

        //find a new target
        if(!controllable && autoTarget){
            if((mount.retarget -= Time.delta) <= 0f){
                mount.target = findTarget(build, mountX, mountY, bullet.range, targetAir && bullet.collidesAir, targetGround && bullet.collidesGround);
                mount.retarget = mount.target == null ? targetInterval : targetSwitchInterval;
                Log.info("BlockWeapon '@': retarget range=@ -> @", name, bullet.range, mount.target);
            }

            if(mount.target != null && checkTarget(build, mount.target, mountX, mountY, bullet.range)){
                mount.target = null;
            }

            boolean shoot = false;

            if(mount.target != null){
                shoot = mount.target.within(mountX, mountY, bullet.range + Math.abs(shootY) + (mount.target instanceof Sized s ? s.hitSize()/2f : 0f)) && can;

                if(predictTarget){
                    Vec2 to = Predict.intercept(build, mount.target, bullet);
                    mount.aimX = to.x;
                    mount.aimY = to.y;
                }else{
                    mount.aimX = mount.target.x();
                    mount.aimY = mount.target.y();
                }
            }

            mount.shoot = mount.rotate = shoot;
        }

        //rotate if applicable
        if(rotate && (mount.rotate || mount.shoot) && can){
            float axisX = build.x() + Angles.trnsx(build.rotation() - 90, x, y),
            axisY = build.y() + Angles.trnsy(build.rotation() - 90, x, y);

            mount.targetRotation = Angles.angle(axisX, axisY, mount.aimX, mount.aimY) - build.rotation();
            mount.rotation = Angles.moveToward(mount.rotation, mount.targetRotation, rotateSpeed * Time.delta);
            if(rotationLimit < 360){
                float dst = Angles.angleDist(mount.rotation, baseRotation);
                if(dst > rotationLimit/2f){
                    mount.rotation = Angles.moveToward(mount.rotation, baseRotation, dst - rotationLimit/2f);
                }
            }
        }else if(!rotate){
            mount.rotation = baseRotation;
            mount.targetRotation = Angles.angle(build.x(), build.y(), mount.aimX, mount.aimY);
        }

        float weaponRotation = build.rotation() - 90 + (rotate ? mount.rotation : baseRotation),
        bulletX = mountX + Angles.trnsx(weaponRotation, this.shootX, this.shootY),
        bulletY = mountY + Angles.trnsy(weaponRotation, this.shootX, this.shootY),
        shootAngle = bulletRotation(build, mount, bulletX, bulletY);

        if(alwaysShooting) mount.shoot = true;

        //update continuous state
        if(continuous && mount.bullet != null){
            if(!mount.bullet.isAdded() || mount.bullet.time >= mount.bullet.lifetime || mount.bullet.type != bullet){
                mount.bullet = null;
            }else{
                mount.bullet.rotation(weaponRotation + 90);
                mount.bullet.set(bulletX, bulletY);
                mount.reload = reload;
                mount.recoil = 1f;
                if(shootSound != Sounds.none && !headless){
                    if(mount.sound == null) mount.sound = new SoundLoop(shootSound, 1f);
                    mount.sound.update(bulletX, bulletY, true);
                }

                //target length of laser
                float shootLength = Math.min(Mathf.dst(bulletX, bulletY, mount.aimX, mount.aimY), range());
                //current length of laser
                float curLength = Mathf.dst(bulletX, bulletY, mount.bullet.aimX, mount.bullet.aimY);
                //resulting length of the bullet (smoothed)
                float resultLength = Mathf.approachDelta(curLength, shootLength, aimChangeSpeed);
                //actual aim end point based on length
                Tmp.v1.trns(shootAngle, mount.lastLength = resultLength).add(bulletX, bulletY);

                mount.bullet.aimX = Tmp.v1.x;
                mount.bullet.aimY = Tmp.v1.y;

                if(alwaysContinuous && mount.shoot){
                    mount.bullet.time = mount.bullet.lifetime * mount.bullet.type.optimalLifeFract * mount.warmup;
                    mount.bullet.keepAlive = true;
                }
            }
        }else{
            //heat decreases when not firing
            mount.heat = Math.max(mount.heat - Time.delta * build.efficiency() / cooldownTime, 0);

            if(mount.sound != null){
                mount.sound.update(bulletX, bulletY, false);
            }
        }

        if(!headless && activeSound != Sounds.none && mount.shoot && can && mount.warmup >= minWarmup){
            control.sound.loop(activeSound, build, activeSoundVolume);
        }

        //ponytail: static mount, no velocity
        float velLen = 1f;

        //shoot if applicable
        if(mount.shoot && //must be shooting
        can && //must be able to shoot
        !(bullet.killShooter && mount.totalShots > 0) && //if the bullet kills the shooter, you should only ever be able to shoot once
        mount.warmup >= minWarmup && //must be warmed up
        velLen >= minShootVelocity && //check velocity requirements
        (mount.reload <= 0.0001f || (alwaysContinuous && mount.bullet == null)) && //reload has to be 0, or it has to be an always-continuous weapon
        (alwaysShooting || Angles.within(rotate ? mount.rotation : build.rotation() + baseRotation, mount.targetRotation, shootCone)) //has to be within the cone
        ){
            shoot(build, mount, bulletX, bulletY, shootAngle);

            mount.reload = reload;
        }
    }

    protected Teamc findTarget(MultiWeaponBuild build, float x, float y, float range, boolean air, boolean ground){
        return Units.closestTarget(build.team(), x, y, range + Math.abs(shootY), u -> u.checkTarget(air, ground), t -> ground && (targetUnderBlocks || !t.block.underBullets) && targetBlocks);
    }

    protected boolean checkTarget(MultiWeaponBuild build, Teamc target, float x, float y, float range){
        return Units.invalidateTarget(target, build.team(), x, y, range + Math.abs(shootY));
    }

    protected float bulletRotation(MultiWeaponBuild build, WeaponMount mount, float bulletX, float bulletY){
        return rotate ? build.rotation() + mount.rotation : Angles.angle(bulletX, bulletY, mount.aimX, mount.aimY) + (build.rotation() - Angles.angle(build.x(), build.y(), mount.aimX, mount.aimY)) + baseRotation;
    }

    protected void shoot(MultiWeaponBuild build, WeaponMount mount, float shootX, float shootY, float rotation){
        if(shoot.firstShotDelay > 0){
            mount.charging = true;
            chargeSound.at(shootX, shootY, Mathf.random(soundPitchMin, soundPitchMax));
            bullet.chargeEffect.at(shootX, shootY, rotation, null);
        }

        shoot.shoot(mount.barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
            //this is incremented immediately, as it is used for total bullet creation amount detection
            mount.totalShots ++;
            int barrel = mount.barrelCounter;

            if(delay > 0f){
                Time.run(delay, () -> {
                    //hack: make sure the barrel is the same as what it was when the bullet was queued to fire
                    int prev = mount.barrelCounter;
                    mount.barrelCounter = barrel;
                    bullet(build, mount, xOffset, yOffset, angle, mover);
                    mount.barrelCounter = prev;
                });
            }else{
                bullet(build, mount, xOffset, yOffset, angle, mover);
            }
        }, () -> mount.barrelCounter++);
    }

    protected void bullet(MultiWeaponBuild build, WeaponMount mount, float xOffset, float yOffset, float angleOffset, Mover mover){
        if(!build.isAdded()) return;

        mount.charging = false;
        float
        xSpread = Mathf.range(xRand),
        ySpread = Mathf.range(yRand),
        weaponRotation = build.rotation() - 90 + (rotate ? mount.rotation : baseRotation),
        mountX = build.x() + Angles.trnsx(build.rotation() - 90, x, y),
        mountY = build.y() + Angles.trnsy(build.rotation() - 90, x, y),
        bulletX = mountX + Angles.trnsx(weaponRotation, this.shootX + xOffset + xSpread, this.shootY + yOffset + ySpread),
        bulletY = mountY + Angles.trnsy(weaponRotation, this.shootX + xOffset + xSpread, this.shootY + yOffset + ySpread),
        shootAngle = bulletRotation(build, mount, bulletX, bulletY) + angleOffset,
        lifeScl = bullet.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, mount.aimX, mount.aimY) / bullet.range) : 1f,
        angle = shootAngle + Mathf.range(inaccuracy + bullet.inaccuracy);

        mount.bullet = bullet.create(build, build, build.team(), bulletX, bulletY, angle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd) + extraVelocity, lifeScl, null, mover, mount.aimX, mount.aimY, mount.target);
        handleBullet(build, mount, mount.bullet);

        if(!continuous){
            shootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax), shootSoundVolume);
        }else{
            initialShootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax), shootSoundVolume);
        }

        if(mount.allowShootEffects){
            ejectEffect.at(mountX, mountY, angle * Mathf.sign(this.x));
            bullet.shootEffect.at(bulletX, bulletY, angle, bullet.hitColor, build);
            bullet.smokeEffect.at(bulletX, bulletY, angle, bullet.hitColor, build);
        }

        Effect.shake(shake, shake, bulletX, bulletY);
        mount.recoil = 1f;
        if(recoils > 0){
            mount.recoils[mount.barrelCounter % recoils] = 1f;
        }
        mount.heat = 1f;
    }

    //override to do special things to a bullet after spawning
    protected void handleBullet(MultiWeaponBuild build, WeaponMount mount, Bullet bullet){
        if(continuous){
            float
                weaponRotation = build.rotation() - 90 + (rotate ? mount.rotation : baseRotation),
                mountX = build.x() + Angles.trnsx(build.rotation() - 90, x, y),
                mountY = build.y() + Angles.trnsy(build.rotation() - 90, x, y),
                bulletX = mountX + Angles.trnsx(weaponRotation, this.shootX, this.shootY),
                bulletY = mountY + Angles.trnsy(weaponRotation, this.shootX, this.shootY);
            //make sure the length updates to the last set value
            Tmp.v1.trns(bulletRotation(build, mount, bulletX, bulletY), shootY + mount.lastLength).add(bulletX, bulletY);
            bullet.aimX = Tmp.v1.x;
            bullet.aimY = Tmp.v1.y;
        }
    }
}
