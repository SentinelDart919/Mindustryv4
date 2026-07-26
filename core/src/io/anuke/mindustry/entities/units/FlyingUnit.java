package io.anuke.mindustry.entities.units;

import com.badlogic.gdx.math.Vector2;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.Predict;
import io.anuke.mindustry.entities.Units;
import io.anuke.mindustry.entities.bullet.BulletType;
import io.anuke.mindustry.entities.traits.CarriableTrait;
import io.anuke.mindustry.entities.traits.CarryTrait;
import io.anuke.mindustry.graphics.Trail;
import io.anuke.mindustry.net.Net;
import io.anuke.mindustry.type.AmmoType;
import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.meta.BlockFlag;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.*;

import static io.anuke.mindustry.Vars.world;

public abstract class FlyingUnit extends BaseUnit implements CarryTrait{
    @Override
    public boolean isRetreating(){
        return state.is(retreat);
    }
    protected static Translator vec = new Translator();
    protected static float wobblyness = 0.6f;
    protected float[] weaponAngles = {0, 0};
    protected boolean itWobbles = true;
    protected Trail trail = new Trail(8);
    protected Trail trail2 = new Trail(8);
    protected CarriableTrait carrying;
    protected final UnitState

    idle = new UnitState(){
        public void update(){
            if(!isCommanded()){
                retarget(() -> {
                    targetClosest();
                    targetClosestEnemyFlag(BlockFlag.target);

                    if(target != null){
                        setState(attack);
                    }
                });
            }

            target = getClosestCore();
            if(target != null){
                circle(50f);
            }
            velocity.scl(0.8f);
        }
    },

    attack = new UnitState(){
        public void entered(){
            target = null;
        }

        public void update(){
            if(health < maxHealth() * 0.5f){
                Tile repair = Geometry.findClosest(x, y, world.indexer.getAllied(team, BlockFlag.repair));
                Unit healer = Units.getClosest(team, x, y, getType().healRange, u -> u.isHealer() && u != FlyingUnit.this);
                if(repair != null && distanceTo(repair) < getType().healRange){
                    setState(retreat);
                    return;
                }else if(healer != null){
                    setState(retreat);
                    return;
                }
            }

            if(Units.invalidateTarget(target, team, x, y)){
                target = null;
            }

            if(retarget()){
                targetClosest();

                if(target == null) targetClosestEnemyFlag(BlockFlag.producer);
                if(target == null) targetClosestEnemyFlag(BlockFlag.turret);
                if(target == null) targetClosestEnemyFlag(BlockFlag.target);

                if(target == null && isCommanded() && getCommand() != UnitCommand.attack){
                    onCommand(getCommand());
                }
            }else if(target != null){
                attack(type.attackLength);

                boolean inRange = distanceTo(target) < Math.max(getWeapon().getAmmo().getRange(), type.range);

                if(type.rotateWeapon){
                    for(boolean left : new boolean[]{true, false}){
                        int wi = left ? 1 : 0;
                        float side = left ? 1f : -1f;
                        float mountAngle = rotation - 90;
                        float wx = x + Angles.trnsx(mountAngle, getWeapon().width * side);
                        float wy = y + Angles.trnsy(mountAngle, getWeapon().width * side);

                        if(inRange){
                            weaponAngles[wi] = Mathf.slerpDelta(weaponAngles[wi], Angles.angle(wx, wy, target.getX(), target.getY()) - rotation, 0.1f);
                        }else{
                            weaponAngles[wi] = 0f;
                        }

                        if(inRange && (Mathf.angNear(angleTo(target), rotation, type.shootCone) || !getWeapon().getAmmo().bullet.keepVelocity)){
                            float worldAngle = rotation - 90 + weaponAngles[wi];
                            float tipX = wx + Angles.trnsx(worldAngle, getWeapon().length);
                            float tipY = wy + Angles.trnsy(worldAngle, getWeapon().length);
                            getWeapon().update(FlyingUnit.this, tipX, tipY, worldAngle, left);
                        }
                    }
                }else{
                    if(inRange && (Mathf.angNear(angleTo(target), rotation, type.shootCone) || !getWeapon().getAmmo().bullet.keepVelocity)){
                        Vector2 to = Predict.intercept(FlyingUnit.this, target, getWeapon().getAmmo().bullet.speed);
                        getWeapon().update(FlyingUnit.this, to.x, to.y);
                    }
                }
            } else {
                target = getClosestCore();
                moveTo(Math.max(type.range, 120f));
                if(type.rotateWeapon){
                    for(boolean left : new boolean[]{true, false}){
                        int wi = left ? 1 : 0;
                        weaponAngles[wi] = 0f;
                    }
                }
            }
        }
    },
    patrol = new UnitState(){
        public void update(){
            if(retarget()){
                targetClosestAllyFlag(BlockFlag.comandCenter);
                targetClosest();

                if(target != null && !Units.invalidateTarget(target, team, x, y)){
                    setState(pursue);
                    return;
                }

                if(target == null) target = getClosestCore();
            }

            if(target != null){
                circle(60f + Mathf.absin(Timers.time() + id * 23525, 70f, 1200f));
            }

            //circle(60f + Mathf.absin(Timers.time() + id * 23525, 70f, 1200f));
        }
    },

    pursue = new UnitState(){
        public void update(){
            if(Units.invalidateTarget(target, team, x, y) || distanceTo(target) > getType().pursueRange){
                target = null;
                if(type.rotateWeapon){
                    for(boolean left : new boolean[]{true, false}){
                        int wi = left ? 1 : 0;
                        weaponAngles[wi] = 0f;
                    }
                }
                onCommand(getCommand());
            }else{
                attack(type.attackLength);

                boolean inRange = distanceTo(target) < Math.max(getWeapon().getAmmo().getRange(), type.range);

                if(type.rotateWeapon){
                    for(boolean left : new boolean[]{true, false}){
                        int wi = left ? 1 : 0;
                        float side = left ? 1f : -1f;
                        float mountAngle = rotation - 90;
                        float wx = x + Angles.trnsx(mountAngle, getWeapon().width * side);
                        float wy = y + Angles.trnsy(mountAngle, getWeapon().width * side);

                        if(inRange){
                            weaponAngles[wi] = Mathf.slerpDelta(weaponAngles[wi], Angles.angle(wx, wy, target.getX(), target.getY()) - rotation, 0.1f);
                        }else{
                            weaponAngles[wi] = 0f;
                        }

                        if(inRange && (Mathf.angNear(angleTo(target), rotation, type.shootCone) || !getWeapon().getAmmo().bullet.keepVelocity)){
                            float worldAngle = rotation - 90 + weaponAngles[wi];
                            float tipX = wx + Angles.trnsx(worldAngle, getWeapon().length);
                            float tipY = wy + Angles.trnsy(worldAngle, getWeapon().length);
                            getWeapon().update(FlyingUnit.this, tipX, tipY, worldAngle, left);
                        }
                    }
                }else{
                    if(inRange && (Mathf.angNear(angleTo(target), rotation, type.shootCone) || !getWeapon().getAmmo().bullet.keepVelocity)){
                        Vector2 to = Predict.intercept(FlyingUnit.this, target, getWeapon().getAmmo().bullet.speed);
                        getWeapon().update(FlyingUnit.this, to.x, to.y);
                    }
                }
            }
        }
    },

    retreat = new UnitState(){
        public void entered(){
            target = null;
        }

        public void update(){
            if(health >= maxHealth()){
                if(isCommanded()){
                    onCommand(getCommand());
                }else{
                    setState(attack);
                }
            }

            if(retarget()){
                target = getClosestCore();
                Unit healer = Units.getClosest(team, x, y, getType().healRange, u -> u.isHealer() && u != FlyingUnit.this);
                Tile repair = Geometry.findClosest(x, y, world.indexer.getAllied(team, BlockFlag.repair));
                if(repair != null && (health < maxHealth())) FlyingUnit.this.target = repair.entity;
                if(healer != null && repair == null && (health < maxHealth())) FlyingUnit.this.target = healer;
                if(target == null) target = getClosestCore();
            }

            if(target == getClosestCore())circle(60f + Mathf.absin(Timers.time() + id * 23525, 70f, 1200f));
            else circle(45f + Mathf.randomSeed(id) * 80);
        }
    },

    hold = new UnitState(){
        public void update(){
            velocity.scl(0.9f);
            if(retarget()){
                targetClosest();
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
    public CarriableTrait getCarry(){
        return carrying;
    }

    @Override
    public void setCarry(CarriableTrait unit){
        this.carrying = unit;
    }

    @Override
    public float getCarryWeight(){
        return type.carryWeight;
    }

    @Override
    public void update(){
        super.update();

        if(!Net.client()){
            updateRotation();
            wobble();
        }

        if(type.engineMirror){
            trail.update(
                x + Angles.trnsx(rotation, type.engineOffsetY, -type.engineOffsetX) + Mathf.range(wobblyness),
                y + Angles.trnsy(rotation, type.engineOffsetY, -type.engineOffsetX) + Mathf.range(wobblyness)
            );
            trail2.update(
                x + Angles.trnsx(rotation, type.engineOffsetY, type.engineOffsetX) + Mathf.range(wobblyness),
                y + Angles.trnsy(rotation, type.engineOffsetY, type.engineOffsetX) + Mathf.range(wobblyness)
            );
        }else{
            trail.update(
                x + Angles.trnsx(rotation, type.engineOffsetY, type.engineOffsetX) + Mathf.range(wobblyness),
                y + Angles.trnsy(rotation, type.engineOffsetY, type.engineOffsetX) + Mathf.range(wobblyness)
            );
        }
    }

    @Override
    protected boolean updateOrder(){
        if(!hasOrder()){
            return false;
        }

        if(getOrderType() == UnitOrderType.move){
            if(retarget()){
                targetClosest();
            }
            if(target != null && !Units.invalidateTarget(target, team, x, y)
            && distanceTo(target) < Math.max(getWeapon().getAmmo().getRange(), type.range)){
                AmmoType ammo = getWeapon().getAmmo();
                Vector2 to = Predict.intercept(FlyingUnit.this, target, ammo.bullet.speed);
                getWeapon().update(FlyingUnit.this, to.x, to.y);
            }

            vec.set(getOrderX() - x, getOrderY() - y);
            if(vec.len() <= Math.max(type.hitsize, 10f)){
                clearOrder();
                setState(hold);
                return false;
            }

            vec.setLength(type.speed * Timers.delta());
            velocity.add(vec);
            return true;
        }

        if(getOrderType() == UnitOrderType.attackMove){
            if(retarget()){
                targetClosest();
            }

            if(target != null && !Units.invalidateTarget(target, team, x, y)
            && distanceTo(target) < Math.max(getWeapon().getAmmo().getRange(), type.range)){
                AmmoType ammo = getWeapon().getAmmo();
                Vector2 to = Predict.intercept(FlyingUnit.this, target, ammo.bullet.speed);
                getWeapon().update(FlyingUnit.this, to.x, to.y);
            }

            vec.set(getOrderX() - x, getOrderY() - y);
            if(vec.len() <= Math.max(type.hitsize, 10f)){
                clearOrder();
                setState(hold);
                return false;
            }

            vec.setLength(type.speed * Timers.delta());
            velocity.add(vec);
            return true;
        }

        if(getOrderType() == UnitOrderType.attackTarget){
            if(target == null || target.isDead() || target.getTeam() == team){
                clearOrder();
                setState(hold);
                return false;
            }

            if(target != null && !Units.invalidateTarget(target, team, x, y)
            && distanceTo(target) < Math.max(getWeapon().getAmmo().getRange(), type.range)){
                AmmoType ammo = getWeapon().getAmmo();
                Vector2 to = Predict.intercept(FlyingUnit.this, target, ammo.bullet.speed);
                getWeapon().update(FlyingUnit.this, to.x, to.y);
            }

            vec.set(target.getX() - x, target.getY() - y);
            if(vec.len() <= Math.max(type.hitsize, 10f)){
                clearOrder();
                setState(hold);
                return false;
            }

            vec.setLength(type.speed * Timers.delta());
            velocity.add(vec);
            return true;
        }

        return false;
    }

    @Override
    public void draw(){
        Draw.alpha(hitTime / hitDuration);

        Draw.rect(type.name, x, y, rotation - 90);

        if(type.rotateWeapon){
            Draw.alpha(1f);

            if(Units.invalidateTarget(target, this)){
                for(int wi = 0; wi < 2; wi++){
                    weaponAngles[wi] = 0f;
                }
            }

            for(int i : new int[]{1, -1}){
                boolean left = i > 0;
                if(!getWeapon().weaponMirror && !left) continue;
                float tra = rotation - 90,
                        trY = -getWeapon().getRecoil(this, left);
                float wx = x + Angles.trnsx(tra, getWeapon().width * i, trY),
                        wy = y + Angles.trnsy(tra, getWeapon().width * i, trY);
                Draw.rect(getWeapon().equipRegion, wx, wy, rotation - 90 + weaponAngles[left ? 1 : 0]);
            }
        }

        drawItems();

        Draw.alpha(1f);
    }

    @Override
    public void drawOver(){
        trail.draw(type.trailColor, type.engineSize);
        if(type.engineMirror){
            trail2.draw(type.trailColor, type.engineSize);
        }
    }

    @Override
    public void behavior(){
        if(health <= health * type.retreatPercent && !isCommanded() &&
         Geometry.findClosest(x, y, world.indexer.getAllied(team, BlockFlag.repair)) != null){
            setState(retreat);
        }

        if(squad != null){
            squad.direction.add(velocity.x / squad.units, velocity.y / squad.units);
            velocity.setAngle(Mathf.slerpDelta(velocity.angle(), squad.direction.angle(), 0.3f));
        }
    }

    @Override
    public UnitState getStartState(){
        return attack;
    }

    @Override
    public float drawSize(){
        return 60;
    }

    protected void wobble(){
        if(Net.client()) return;
        if(itWobbles){
        x += Mathf.sin(Timers.time() + id * 999, 25f, 0.08f)*Timers.delta();
        y += Mathf.cos(Timers.time() + id * 999, 25f, 0.08f)*Timers.delta();

        if(velocity.len() <= 0.05f){
            rotation += Mathf.sin(Timers.time() + id * 99, 10f, 2.5f)*Timers.delta();
        }}
    }

    protected void updateRotation(){
        rotation = velocity.angle();
    }

    protected void circle(float circleLength){
        circle(circleLength, type.speed);
    }

    protected void circle(float circleLength, float speed){
        if(target == null) return;

        vec.set(target.getX() - x, target.getY() - y);

        if(vec.len() < circleLength){
            vec.rotate((circleLength - vec.len()) / circleLength * 180f);
        }

        vec.setLength(speed * Timers.delta());

        velocity.add(vec);
    }

    protected void moveTo(float circleLength){
        if(target == null) return;

        vec.set(target.getX() - x, target.getY() - y);

        float length = circleLength <= 0.001f ? 1f : Mathf.clamp((distanceTo(target) - circleLength) / 100f, -1f, 1f);

        vec.setLength(type.speed * Timers.delta() * length);
        if(length < 0) vec.rotate(180f);

        velocity.add(vec);
    }

    protected void attack(float circleLength){
        vec.set(target.getX() - x, target.getY() - y);

        float ang = angleTo(target);
        float diff = Angles.angleDist(ang, rotation);

        if(diff > 100f && vec.len() < circleLength){
            vec.setAngle(velocity.angle());
        }else{
            vec.setAngle(Mathf.slerpDelta(velocity.angle(), vec.angle(), 0.44f));
        }

        vec.setLength(type.speed * Timers.delta());

        velocity.add(vec);
    }
}
