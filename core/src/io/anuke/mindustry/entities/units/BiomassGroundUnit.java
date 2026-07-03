package io.anuke.mindustry.entities.units;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.util.Timers;
import io.anuke.mindustry.entities.Predict;
import io.anuke.mindustry.entities.Units;
import io.anuke.mindustry.type.AmmoType;
import io.anuke.mindustry.world.meta.BlockFlag;
import arc.util.Time;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;

public class BiomassGroundUnit extends GroundUnit{

    public final UnitState
    patrol = new UnitState(){
        public void update(){
            if(retarget()){
                targetClosest();

                if(target != null && !Units.invalidateTarget(target, team, x, y)){
                    state.set(pursue);
                    return;
                }
            }

            BiomassGroundUnit.super.patrol.update();
        }
    },
    pursue = new UnitState(){
        public void update(){
            if(Units.invalidateTarget(target, team, x, y) || dst(target) > getType().pursueRange){
                target = null;
                onCommand(getCommand());
            }else{
                if(dst(target) > getWeapon().getAmmo().getRange() * 0.8f){
                    moveTo(target.getX(), target.getY());
                }

                if(dst(target) < getWeapon().getAmmo().getRange()){
                    rotate(angleTo(target));

                    if(Mathf.angNear(angleTo(target), rotation, 13f)){
                        AmmoType ammo = getWeapon().getAmmo();

                        Vec2 to = Predict.intercept(BiomassGroundUnit.this, target, ammo.bullet.speed);

                        getWeapon().update(BiomassGroundUnit.this, to.x, to.y);
                    }
                }
            }
        }
    };

    @Override
    public void onCommand(UnitCommand command){
        state.set(command == UnitCommand.retreat ? retreat :
                command == UnitCommand.attack ? attack :
                command == UnitCommand.patrol ? patrol :
                null);
    }

    @Override
    public void drawStats(){
        float hf = healthf();
        float frequency = 1f + (1f - hf) * 3f;
        float amplitude = 0.1f + (1f - hf) * 0.15f;

        float scale = 1f + Mathf.sin(Timers.time() * frequency, 2f, amplitude);

        Draw.color(Color.black, team.color, hf + Mathf.absin(Timers.time(), hf * 5f, 1f - hf));
        Draw.alpha(hitTime);
        Draw.rect(getPowerCellRegion(), x, y,
                getPowerCellRegion().width * scale,
                getPowerCellRegion().height * scale,
                rotation - 90);
        Draw.color();
    }

    @Override
    public TextureRegion getPowerCellRegion(){
        if(type.hitsize > 10f)return Core.atlas.find("biomass-heart");
        else  return Core.atlas.find("small-biomass-heart");
    }
}
