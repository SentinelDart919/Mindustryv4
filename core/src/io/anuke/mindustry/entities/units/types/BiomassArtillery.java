package io.anuke.mindustry.entities.units.types;

import arc.graphics.Color;
import io.anuke.mindustry.entities.Units;
import io.anuke.mindustry.entities.units.BiomassGroundUnit;
import io.anuke.mindustry.world.blocks.Floor;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;

public class BiomassArtillery extends BiomassGroundUnit {

    @Override
    public void draw(){
        Draw.alpha(hitTime / hitDuration);

        float ft = Mathf.sin(walkTime * type.speed * 5f, 6f, 2f);

        Floor floor = getFloorOn();

        if(floor.isLiquid){
            Draw.tint(Color.white, floor.liquidColor, 0.5f);
        }

        for(int i : Mathf.signs){
            Draw.rect(type.legRegion,
                    x + Angles.trnsx(baseRotation, ft * i),
                    y + Angles.trnsy(baseRotation, ft * i),
                    12f * i, 12f - Mathf.clamp(ft * i, 0, 2), baseRotation - 90);
        }

        if(floor.isLiquid){
            Draw.tint(Color.white, floor.liquidColor, drownTime * 0.4f);
        }else{
            Draw.tint(Color.white);
        }

        Draw.rect(type.baseRegion, x, y, baseRotation - 90);

        Draw.rect(type.region, x, y, rotation - 90);

        float tra = rotation - 90, trY = -weapon.getRecoil(this, true);

        for(int i : Mathf.signs){
            if(!weapon.weaponMirror && i < 0) continue;
            Draw.rect(weapon.equipRegion,
                    x + Angles.trnsx(tra, 0, trY),
                    y + Angles.trnsy(tra, 0, trY), rotation - 90);
        }

        drawItems();

        Draw.alpha(1f);
    }

    @Override
    protected void patrol(){
        if(Units.invalidateTarget(target, this)){
            super.patrol();
        }
    }
    @Override
    protected void moveToEnemyCore(){
        if(Units.invalidateTarget(target, this)){
            super.moveToEnemyCore();
        }
    }
}
