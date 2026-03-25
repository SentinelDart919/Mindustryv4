package io.anuke.mindustry.entities.units.types;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import io.anuke.mindustry.entities.Predict;
import io.anuke.mindustry.entities.Units;
import io.anuke.mindustry.entities.units.GroundUnit;
import io.anuke.mindustry.net.Net;
import io.anuke.mindustry.type.AmmoType;
import io.anuke.mindustry.world.blocks.Floor;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Angles;
import io.anuke.ucore.util.Mathf;

public class TankUnit extends GroundUnit {
    protected float weaponRotation;

    protected void updateWeaponRotation(){
        if(!Units.invalidateTarget(target, this)){
            weaponRotation = Mathf.slerpDelta(weaponRotation, angleTo(target), type.rotatespeed);
        }else{
            float targetRotation = velocity.isZero() ? baseRotation : velocity.angle();
            weaponRotation = Mathf.slerpDelta(weaponRotation, targetRotation, type.baseRotateSpeed);
        }
    }

    @Override
    public void update(){
        super.update();

        if(!Net.client()){
            updateWeaponRotation();
        }
    }

    @Override
    public void behavior(){
        if(health <= health * type.retreatPercent && !isCommanded()){
            setState(retreat);
        }

        if(!Units.invalidateTarget(target, this) && distanceTo(target) < getWeapon().getAmmo().getRange()){
            if(Mathf.angNear(angleTo(target), weaponRotation, 13f)){
                AmmoType ammo = getWeapon().getAmmo();
                Vector2 to = Predict.intercept(this, target, ammo.bullet.speed);
                getWeapon().update(this, to.x, to.y);
            }
        }
    }

    @Override
    public void draw(){
        Draw.alpha(hitTime / hitDuration);

        float ft = Mathf.sin(walkTime * type.speed*5f, 6f, 2f);

        Floor floor = getFloorOn();

        if(floor.isLiquid){
            Draw.tint(Color.WHITE, floor.liquidColor, 0.5f);
        }

        for(int i : Mathf.signs){
            Draw.rect(type.trackRegion,
                    x + Angles.trnsx(baseRotation, ft * i),
                    y + Angles.trnsy(baseRotation, ft * i),
                    16 * i, 24f, baseRotation - 90);
        }

        if(floor.isLiquid){
            Draw.tint(Color.WHITE, floor.liquidColor, drownTime * 0.4f);
        }else{
            Draw.tint(Color.WHITE);
        }

        //Draw.rect(type.baseRegion, x, y, baseRotation - 90);

        Draw.rect(type.region, x, y, rotation - 90);


            float tra = rotation - 90, trY = -weapon.getRecoil(this, weapon.getReload() > 0) + type.weaponOffsetY;
            Draw.rect(weapon.equipRegion,
                    x + Angles.trnsx(tra, type.weaponOffsetX, trY),
                    y + Angles.trnsy(tra, type.weaponOffsetX, trY), weaponRotation - 90);


        drawItems();

        Draw.alpha(1f);
    }
}
