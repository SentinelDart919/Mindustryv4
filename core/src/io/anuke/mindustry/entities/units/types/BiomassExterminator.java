package io.anuke.mindustry.entities.units.types;

import arc.graphics.Color;
import io.anuke.mindustry.entities.units.BiomassGroundUnit;
import io.anuke.mindustry.world.blocks.Floor;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;

public class BiomassExterminator extends BiomassGroundUnit {
    @Override
    public void draw(){
        Draw.alpha(hitTime / hitDuration);

        float ft = Mathf.sin(walkTime * type.speed*5f, 6f, 2f);

        Floor floor = getFloorOn();

        if(floor.isLiquid){
            Draw.tint(Color.white, floor.liquidColor, 0.5f);
        }

        for(int i : Mathf.signs){
            Draw.rect(type.legRegion,
                    x + Angles.trnsx(baseRotation, ft * i),
                    y + Angles.trnsy(baseRotation, ft * i),
                    32 * i, 32 - Mathf.clamp(ft * i, 0, 4), baseRotation - 90);
        }

        if(floor.isLiquid){
            Draw.tint(Color.white, floor.liquidColor, drownTime * 0.4f);
        }else{
            Draw.tint(Color.white);
        }

        Draw.rect(type.baseRegion, x, y, baseRotation - 90);

        Draw.rect(type.region, x, y, rotation - 90);

        for(int i : Mathf.signs){
            if(!weapon.weaponMirror && i < 0) continue;
            float tra = rotation - 90, trY = -weapon.getRecoil(this, i > 0) + type.weaponOffsetY;
            float w = i > 0 ? -12 : 16;
            float h = i > 0 ? 32 : 42;
            Draw.rect(weapon.equipRegion,
                    x + Angles.trnsx(tra, type.weaponOffsetX * i, trY),
                    y + Angles.trnsy(tra, type.weaponOffsetX * i, trY), w, h, rotation - 90);
        }

        drawItems();

        Draw.alpha(1f);
    }
}
