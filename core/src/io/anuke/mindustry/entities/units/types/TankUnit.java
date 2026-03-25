package io.anuke.mindustry.entities.units.types;

import com.badlogic.gdx.graphics.Color;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.Units;
import io.anuke.mindustry.entities.units.GroundUnit;
import io.anuke.mindustry.world.blocks.Floor;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Angles;
import io.anuke.ucore.util.Mathf;

public class TankUnit extends GroundUnit {
    protected float weaponRotation;
    protected void updateWeaponRotation(){
        if(!Units.invalidateTarget(target, this)){
            weaponRotation = Mathf.slerpDelta(rotation, angleTo(target), type.rotatespeed);
        }else{
            weaponRotation = Mathf.slerpDelta(rotation, velocity.angle(), type.baseRotateSpeed);
        }
    }
    @Override
    public void update(){
        TileEntity core = getClosestEnemyCore();
        float dst = core == null ? 0 : distanceTo(core);

        if(core != null && dst < getWeapon().getAmmo().getRange() / 1.1f){
            target = core;
        }

        if(dst > getWeapon().getAmmo().getRange() * 0.5f){
            moveToCore();
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
                    12f * i, 12f - Mathf.clamp(ft * i, 0, 2), baseRotation - 90);
        }

        if(floor.isLiquid){
            Draw.tint(Color.WHITE, floor.liquidColor, drownTime * 0.4f);
        }else{
            Draw.tint(Color.WHITE);
        }

        Draw.rect(type.baseRegion, x, y, baseRotation - 90);

        Draw.rect(type.region, x, y, rotation - 90);


            float tra = rotation - 90, trY = -weapon.getRecoil(this, weapon.getReload() > 0) + type.weaponOffsetY;
            Draw.rect(weapon.equipRegion,
                    x + Angles.trnsx(tra, type.weaponOffsetX, trY),
                    y + Angles.trnsy(tra, type.weaponOffsetX, trY), weaponRotation - 90);


        drawItems();

        Draw.alpha(1f);
    }
}
