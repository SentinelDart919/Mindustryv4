package io.anuke.mindustry.entities.units;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.Predict;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.Units;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.type.AmmoType;
import io.anuke.mindustry.type.ContentType;
import io.anuke.mindustry.type.Weapon;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.Floor;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Angles;
import io.anuke.ucore.util.Mathf;
import io.anuke.ucore.util.Translator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import io.anuke.mindustry.content.blocks.UnitBlocks;
import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.world.meta.BlockFlag;
import io.anuke.ucore.util.Geometry;

import static io.anuke.mindustry.Vars.*;

public abstract class GroundUnit extends BaseUnit{
    protected static Translator vec = new Translator();
    private static final int maxOrderPathNodes = 700;
    private static final int orderPathRepathDelay = 30;

    protected float walkTime;
    protected float stuckTime;
    protected float baseRotation;
    protected float[] weaponAngles = {0, 0};
    protected Weapon weapon;
    protected IntArray orderPath = new IntArray();
    protected int orderPathCursor = 0;
    protected int orderPathRepath = 0;

    public final UnitState

    attack = new UnitState(){
        public void entered(){
            target = null;
        }

        public void update(){
            if(health < maxHealth() * 0.5f){
                Tile repair = Geometry.findClosest(x, y, world.indexer.getAllied(team, BlockFlag.repair));
                Unit healer = Units.getClosest(team, x, y, getType().healRange, u -> u.isHealer() && u != GroundUnit.this);
                if(repair != null && distanceTo(repair) < getType().healRange){
                    state.set(retreat);
                    return;
                }else if(healer != null){
                    state.set(retreat);
                    return;
                }
            }

            TileEntity core = getClosestEnemyCore();

            if(target == null && core != null){
                target = core;
            }

            if(core != null && distanceTo(core) > getWeapon().getAmmo().getRange() * 0.5f){
                moveToEnemyCore();
            }
        }
    },
    patrol = new UnitState(){
        public void update(){
            TileEntity target = getClosestCore();
            if(target != null){
                if(distanceTo(target) > 400f){
                    moveAwayFromCore();
                }else{
                    patrol();
                }
            }
        }
    },
    retreat = new UnitState(){
        public void entered(){
            target = null;
        }

        public void update(){
            Unit healer = Units.getClosest(team, x, y, getType().healRange, u -> u.isHealer() && u != GroundUnit.this);
            Tile repair = Geometry.findClosest(x, y, world.indexer.getAllied(team, BlockFlag.repair));
            if(health >= maxHealth()){
                if(isCommanded()){
                    onCommand(getCommand());
                }else{
                    state.set(attack);
                }
                return;
            }

            if(retarget() || target == null || (target instanceof TileEntity && (((TileEntity)target).getTile() == null || ((TileEntity)target).getTile().target().block().flags == null || !((TileEntity)target).getTile().target().block().flags.contains(BlockFlag.repair))) || (target instanceof Unit && !((Unit)target).isHealer())){
                if(repair != null) target = repair.entity();
                else if(healer != null) target = healer;
                else target = getClosestCore();
            }

            if(target != null){
                float dst = distanceTo(target);
                if(dst > 7f){
                    if(target instanceof TileEntity && ((TileEntity)target).getTile() != null && ((TileEntity)target).getTile().target().block().flags != null && ((TileEntity)target).getTile().target().block().flags.contains(BlockFlag.repair)){
                        moveTo(target.getX(), target.getY());
                    }else if(target instanceof Unit && ((Unit)target).isHealer()){
                        if(dst > type.healRange){
                            moveToHome();
                        }else{
                            moveTo(target.getX(), target.getY());
                        }
                    }else{
                        moveToHome();
                    }
                }else{
                    velocity.setZero();
                }
            }else{
                moveToHome();
            }
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
    public void init(UnitType type, Team team){
        super.init(type, team);
        this.weapon = type.weapon;
    }

    @Override
    public void interpolate(){
        super.interpolate();

        if(interpolator.values.length > 1){
            baseRotation = interpolator.values[1];
        }
    }

    @Override
    public void move(float x, float y){
        if(Mathf.dst(x, y) > 0.01f){
            baseRotation = Mathf.slerpDelta(baseRotation, Mathf.atan2(x, y), type.baseRotateSpeed);
        }
        super.move(x, y);
    }

    @Override
    public UnitState getStartState(){
        return attack;
    }

    @Override
    public void update(){
        super.update();

        stuckTime = !vec.set(x, y).sub(lastPosition()).isZero(0.0001f) ? 0f : stuckTime + Timers.delta();

        if(!velocity.isZero()){
            baseRotation = Mathf.slerpDelta(baseRotation, velocity.angle(), 0.05f);
        }

        if(stuckTime < 1f){
            walkTime += Timers.delta();
        }
    }

    @Override
    public Weapon getWeapon(){
        return weapon;
    }

    public void setWeapon(Weapon weapon){
        this.weapon = weapon;
    }

    private float orderArrivalDst(float x, float y){
        Tile t = world.tileWorld(x, y);
        float blockRadius = t != null && t.block() != null && t.block().size > 0 ? t.block().size * tilesize / 2f : 0f;
        return Math.max(type.hitsize, 10f) + blockRadius;
    }

    @Override
    protected boolean updateOrder(){
        if(!hasOrder()){
            clearOrderPath();
            return false;
        }

        float arrivalDst = orderArrivalDst(getOrderX(), getOrderY());

        if(getOrderType() == UnitOrderType.move){
            float dst = distanceTo(getOrderX(), getOrderY());
            if(dst <= arrivalDst){
                clearOrder();
                clearOrderPath();
                velocity.scl(0.5f);
                state.set(hold);
                return false;
            }

            followOrderPath();
            return true;
        }

        if(getOrderType() == UnitOrderType.attackMove){
            if(retarget()){
                targetClosest();
            }

            if(target != null && !Units.invalidateTarget(target, this) && distanceTo(target) < getWeapon().getAmmo().getRange()){
                rotate(angleTo(target));
                if(Mathf.angNear(angleTo(target), rotation, 13f)){
                    AmmoType ammo = getWeapon().getAmmo();
                    Vector2 to = Predict.intercept(GroundUnit.this, target, ammo.bullet.speed);
                    getWeapon().update(GroundUnit.this, to.x, to.y);
                }
            }

            float dst = distanceTo(getOrderX(), getOrderY());
            if(dst <= arrivalDst){
                clearOrder();
                clearOrderPath();
                state.set(hold);
                return false;
            }

            followOrderPath();
            return true;
        }

        if(getOrderType() == UnitOrderType.attackTarget){
            if(target == null || target.isDead() || target.getTeam() == team){
                clearOrder();
                clearOrderPath();
                state.set(hold);
                return false;
            }

            orderX = target.getX();
            orderY = target.getY();

            if(target != null && !Units.invalidateTarget(target, this) && distanceTo(target) < getWeapon().getAmmo().getRange()){
                rotate(angleTo(target));
                if(Mathf.angNear(angleTo(target), rotation, 13f)){
                    AmmoType ammo = getWeapon().getAmmo();
                    Vector2 to = Predict.intercept(GroundUnit.this, target, ammo.bullet.speed);
                    getWeapon().update(GroundUnit.this, to.x, to.y);
                }
            }

            followOrderPath();
            return true;
        }

        return false;
    }

    protected void clearOrderPath(){
        orderPath.clear();
        orderPathCursor = 0;
        orderPathRepath = 0;
    }

    protected void followOrderPath(){
        Tile start = world.tileWorld(x, y);
        Tile goal = world.tileWorld(getOrderX(), getOrderY());

        if(start == null || goal == null){
            moveTo(getOrderX(), getOrderY());
            return;
        }

        goal = findPassableGoal(goal);
        if(start == goal){
            return;
        }

        if(orderPathRepath <= 0 || orderPath.size == 0 || orderPathCursor >= orderPath.size){
            buildOrderPath(start, goal);
            orderPathRepath = orderPathRepathDelay;
        }else{
            orderPathRepath--;
        }

        if(orderPath.size == 0 || orderPathCursor >= orderPath.size){
            moveTo(goal.worldx() + tilesize / 2f, goal.worldy() + tilesize / 2f);
            return;
        }

        Tile waypoint = world.tile(orderPath.get(orderPathCursor));
        if(waypoint == null){
            buildOrderPath(start, goal);
            if(orderPath.size == 0){
                moveTo(goal.worldx() + tilesize / 2f, goal.worldy() + tilesize / 2f);
                return;
            }
            waypoint = world.tile(orderPath.get(orderPathCursor));
            if(waypoint == null){
                moveTo(goal.worldx() + tilesize / 2f, goal.worldy() + tilesize / 2f);
                return;
            }
        }

        if(Mathf.dst(x - waypoint.worldx(), y - waypoint.worldy()) <= tilesize * 0.55f){
            orderPathCursor++;
            if(orderPathCursor >= orderPath.size){
                moveTo(goal.worldx() + tilesize / 2f, goal.worldy() + tilesize / 2f);
                return;
            }
            waypoint = world.tile(orderPath.get(orderPathCursor));
            if(waypoint == null){
                moveTo(goal.worldx() + tilesize / 2f, goal.worldy() + tilesize / 2f);
                return;
            }
        }

        moveTo(waypoint.worldx(), waypoint.worldy());
    }

    private Tile findPassableGoal(Tile goal){
        if(orderPassable(goal)) return goal;
        for(int r = 1; r <= 3; r++){
            for(int dx = -r; dx <= r; dx++){
                for(int dy = -r; dy <= r; dy++){
                    if(Math.abs(dx) != r && Math.abs(dy) != r) continue;
                    Tile t = world.tile(goal.x + dx, goal.y + dy);
                    if(t != null && orderPassable(t)) return t;
                }
            }
        }
        return goal;
    }

    protected void buildOrderPath(Tile start, Tile goal){
        orderPath.clear();
        orderPathCursor = 0;

        if(start == goal){
            return;
        }

        IntArray open = new IntArray();
        IntIntMap cameFrom = new IntIntMap();
        IntIntMap gScore = new IntIntMap();
        IntIntMap fScore = new IntIntMap();
        IntIntMap closed = new IntIntMap();

        int startPos = start.packedPosition();
        int goalPos = goal.packedPosition();

        open.add(startPos);
        gScore.put(startPos, 0);
        fScore.put(startPos, (Math.abs(start.x - goal.x) + Math.abs(start.y - goal.y)) * 10);

        int expanded = 0;

        while(open.size > 0 && expanded < maxOrderPathNodes){
            int bestIndex = 0;
            int current = open.get(0);
            int bestScore = fScore.get(current, Integer.MAX_VALUE);

            for(int i = 1; i < open.size; i++){
                int node = open.get(i);
                int score = fScore.get(node, Integer.MAX_VALUE);
                if(score < bestScore){
                    bestScore = score;
                    current = node;
                    bestIndex = i;
                }
            }

            open.removeIndex(bestIndex);

            if(current == goalPos){
                reconstructOrderPath(cameFrom, current, startPos);
                return;
            }

            closed.put(current, 1);
            expanded++;

            Tile currentTile = world.tile(current);
            if(currentTile == null) continue;

            for(int sx = -1; sx <= 1; sx++){
                for(int sy = -1; sy <= 1; sy++){
                    if(sx == 0 && sy == 0) continue;
                    int nx = currentTile.x + sx, ny = currentTile.y + sy;
                    Tile next = world.tile(nx, ny);
                    if(next == null || !orderPassable(next)) continue;
                    if(sx != 0 && sy != 0 && (world.solid(currentTile.x + sx, currentTile.y) || world.solid(currentTile.x, currentTile.y + sy))){
                        continue;
                    }

                    int nextPos = next.packedPosition();
                    if(closed.get(nextPos, 0) == 1) continue;

                    int currentScore = gScore.get(current, Integer.MAX_VALUE / 8);
                    int stepCost = (sx == 0 || sy == 0 ? 10 : 14) + (int)(next.cost * 2f);
                    int tentativeG = currentScore + stepCost;
                    int known = gScore.get(nextPos, Integer.MAX_VALUE / 8);

                    if(tentativeG < known){
                        cameFrom.put(nextPos, current);
                        gScore.put(nextPos, tentativeG);
                        int heuristic = (Math.abs(next.x - goal.x) + Math.abs(next.y - goal.y)) * 10;
                        fScore.put(nextPos, tentativeG + heuristic);

                        boolean exists = false;
                        for(int i = 0; i < open.size; i++){
                            if(open.get(i) == nextPos){
                                exists = true;
                                break;
                            }
                        }
                        if(!exists) open.add(nextPos);
                    }
                }
            }
        }
    }

    protected void reconstructOrderPath(IntIntMap cameFrom, int current, int startPos){
        IntArray rev = new IntArray();
        rev.add(current);

        while(cameFrom.containsKey(current)){
            current = cameFrom.get(current, startPos);
            rev.add(current);
            if(current == startPos) break;
        }

        for(int i = rev.size - 2; i >= 0; i--){
            orderPath.add(rev.get(i));
        }
        orderPathCursor = 0;
    }

    protected boolean orderPassable(Tile tile){
        if(tile.solid() && !(tile.breakable() && tile.target().getTeam() != team)) return false;
        return tile.floor().drownTime <= 0f;
    }

    public int getOrderPathCursor(){
        return orderPathCursor;
    }

    public int getOrderPathSize(){
        return orderPath.size;
    }

    public int getOrderPathTilePacked(int index){
        return orderPath.get(index);
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
            Draw.rect(type.legRegion,
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

        for(int i : Mathf.signs){
            if(!weapon.weaponMirror && i < 0) continue;
            float tra = rotation - 90, trY = -weapon.getRecoil(this, i > 0);
            float w = i > 0 ? -12 : 12;
            float weaponRot = type.rotateWeapon ? rotation + weaponAngles[i > 0 ? 1 : 0] : rotation;
            Draw.rect(weapon.equipRegion,
                    x + Angles.trnsx(tra, weapon.width * i, trY),
                    y + Angles.trnsy(tra, weapon.width * i, trY), w, 12, weaponRot - 90);
        }

        drawItems();

        Draw.alpha(1f);
    }

    @Override
    public void behavior(){
        if(health <= health * type.retreatPercent && !isCommanded()){
            setState(retreat);
        }

        if(!Units.invalidateTarget(target, this)){
            boolean inRange = distanceTo(target) < getWeapon().getAmmo().getRange();

            if(inRange){
                rotate(angleTo(target));
            }else if(!velocity.isZero()){
                rotation = Mathf.slerpDelta(rotation, velocity.angle(), type.rotatespeed);
            }

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

                    if(inRange){
                        float worldAngle = rotation - 90 + weaponAngles[wi];
                        float tipX = wx + Angles.trnsx(worldAngle, getWeapon().length);
                        float tipY = wy + Angles.trnsy(worldAngle, getWeapon().length);
                        getWeapon().update(GroundUnit.this, tipX, tipY, worldAngle, left);
                    }
                }
            }else if(inRange && Mathf.angNear(angleTo(target), rotation, 13f)){
                AmmoType ammo = getWeapon().getAmmo();

                Vector2 to = Predict.intercept(GroundUnit.this, target, ammo.bullet.speed);

                getWeapon().update(GroundUnit.this, to.x, to.y);
            }
        }else{
            if(!velocity.isZero()){
                rotation = Mathf.slerpDelta(rotation, velocity.angle(), type.rotatespeed);
            }
            if(type.rotateWeapon){
                for(boolean left : new boolean[]{true, false}){
                    int wi = left ? 1 : 0;
                    weaponAngles[wi] = 0f;
                }
            }
        }
    }

    @Override
    public boolean isRetreating(){
        return state.is(retreat);
    }

    @Override
    public void updateTargeting(){
        super.updateTargeting();

        if(!isRetreating() && Units.invalidateTarget(target, team, x, y, Float.MAX_VALUE)){
            target = null;
        }

        if(getOrderType() != UnitOrderType.attackTarget){
            retarget(this::targetClosest);
        }
    }

    @Override
    public void write(DataOutput data) throws IOException{
        super.write(data);
        data.writeByte(weapon.id);
    }

    @Override
    public void read(DataInput data, long time) throws IOException{
        super.read(data, time);
        weapon = content.getByID(ContentType.weapon, data.readByte());
    }

    @Override
    public void writeSave(DataOutput stream) throws IOException{
        stream.writeByte(weapon.id);
        super.writeSave(stream);
    }

    @Override
    public void readSave(DataInput stream) throws IOException{
        weapon = content.getByID(ContentType.weapon, stream.readByte());
        super.readSave(stream);
    }

    protected void patrol(){
        vec.trns(baseRotation, type.speed * Timers.delta());
        velocity.add(vec.x, vec.y);
        vec.trns(baseRotation, type.hitsizeTile);
        Tile tile = world.tileWorld(x + vec.x, y + vec.y);
        if((tile == null || tile.solid() || tile.floor().drownTime > 0) || stuckTime > 10f){
            baseRotation += Mathf.sign(id % 2 - 0.5f) * Timers.delta() * 3f;
        }

        rotation = Mathf.slerpDelta(rotation, velocity.angle(), type.rotatespeed);
    }

    protected void circle(float circleLength){
        if(target == null) return;

        vec.set(target.getX() - x, target.getY() - y);

        if(vec.len() < circleLength){
            vec.rotate((circleLength - vec.len()) / circleLength * 180f);
        }

        vec.setLength(type.speed * Timers.delta());

        velocity.add(vec);
    }

    protected void moveTo(float x, float y){
        float angle = angleTo(x, y);
        velocity.add(vec.trns(angle, type.speed * Timers.delta()));
        rotation = Mathf.slerpDelta(rotation, angle, type.rotatespeed);
    }

    protected void moveToEnemyCore(){
        Tile tile = world.tileWorld(x, y);
        if(tile == null) return;
        Tile targetTile = world.pathfinder.getTargetTile(team, tile);

        if(tile == targetTile) return;

        velocity.add(vec.trns(angleTo(targetTile), type.speed*Timers.delta()));
    }

    protected void moveToHome(){
        Team enemy = null;
        for(Team team : Vars.state.teams.enemiesOf(team)){
            if(Vars.state.teams.isActive(team)){
                enemy = team;
                break;
            }
        }

        if(enemy == null) return;

        Tile tile = world.tileWorld(x, y);
        if(tile == null) return;
        Tile targetTile = world.pathfinder.getTargetTile(enemy, tile);

        if(tile == targetTile) return;

        float angle = angleTo(targetTile);

        velocity.add(vec.trns(angleTo(targetTile), type.speed*Timers.delta()));
        rotation = Mathf.slerpDelta(rotation, angle, type.rotatespeed);
    }

    protected void moveAwayFromCore(){
        moveToHome();
    }
}
