package io.anuke.mindustry.entities.units;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Translator;

import arc.math.geom.Vec2;
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
import arc.util.Time;
import arc.graphics.g2d.Draw;
import arc.util.*;

import static io.anuke.mindustry.Vars.world;

public abstract class FlyingUnit extends BaseUnit implements CarryTrait{
    @Override
    public boolean isRetreating(){
        return state.is(retreat);
    }
    protected static Translator vec = new Translator();
    protected static float wobblyness = 0.6f;
    protected boolean customTrail = false;
    protected boolean itWobbles = true;
    protected Trail trail = new Trail(8);
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
                if(repair != null && dst(repair) < getType().healRange){
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

                if ((Mathf.angNear(angleTo(target), rotation, type.shootCone) || !getWeapon().getAmmo().bullet.keepVelocity) //bombers and such don't care about rotation
                        && dst(target) < Math.max(getWeapon().getAmmo().getRange(), type.range)) {
                    AmmoType ammo = getWeapon().getAmmo();
                    Vec2 to = Predict.intercept(FlyingUnit.this, target, ammo.bullet.speed);
                    getWeapon().update(FlyingUnit.this, to.x, to.y);
                }
            } else {
                target = getClosestCore();
                moveTo(Math.max(type.range, 120f));
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
            if(Units.invalidateTarget(target, team, x, y) || dst(target) > getType().pursueRange){
                target = null;
                onCommand(getCommand());
            }else{
                attack(type.attackLength);

                if ((Mathf.angNear(angleTo(target), rotation, type.shootCone) || !getWeapon().getAmmo().bullet.keepVelocity)
                        && dst(target) < Math.max(getWeapon().getAmmo().getRange(), type.range)) {
                    AmmoType ammo = getWeapon().getAmmo();
                    Vec2 to = Predict.intercept(FlyingUnit.this, target, ammo.bullet.speed);
                    getWeapon().update(FlyingUnit.this, to.x, to.y);
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

        if(!customTrail)trail.update(x + Angles.trnsx(rotation + 180f, 6f) + Mathf.range(wobblyness),
        y + Angles.trnsy(rotation + 180f, 6f) + Mathf.range(wobblyness));
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
            && dst(target) < Math.max(getWeapon().getAmmo().getRange(), type.range)){
                AmmoType ammo = getWeapon().getAmmo();
                Vec2 to = Predict.intercept(FlyingUnit.this, target, ammo.bullet.speed);
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
            && dst(target) < Math.max(getWeapon().getAmmo().getRange(), type.range)){
                AmmoType ammo = getWeapon().getAmmo();
                Vec2 to = Predict.intercept(FlyingUnit.this, target, ammo.bullet.speed);
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
            && dst(target) < Math.max(getWeapon().getAmmo().getRange(), type.range)){
                AmmoType ammo = getWeapon().getAmmo();
                Vec2 to = Predict.intercept(FlyingUnit.this, target, ammo.bullet.speed);
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

        drawItems();

        Draw.alpha(1f);
    }

    @Override
    public void drawOver(){
        trail.draw(type.trailColor, 5f);
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

        float length = circleLength <= 0.001f ? 1f : Mathf.clamp((dst(target) - circleLength) / 100f, -1f, 1f);

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
