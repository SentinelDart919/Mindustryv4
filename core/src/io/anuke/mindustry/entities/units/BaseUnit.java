package io.anuke.mindustry.entities.units;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.LongArray;
import io.anuke.annotations.Annotations.Loc;
import io.anuke.annotations.Annotations.Remote;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.content.fx.ExplosionFx;
import io.anuke.mindustry.entities.Damage;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.entities.Units;
import io.anuke.mindustry.entities.effect.ScorchDecal;
import io.anuke.mindustry.entities.traits.ShooterTrait;
import io.anuke.mindustry.entities.traits.SpawnerTrait;
import io.anuke.mindustry.entities.traits.TargetTrait;
import io.anuke.mindustry.entities.units.ai.AIController;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.net.Net;
import io.anuke.mindustry.type.ContentType;
import io.anuke.mindustry.type.ItemStack;
import io.anuke.mindustry.type.Weapon;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.units.CommandCenter.CommandCenterEntity;
import io.anuke.mindustry.world.meta.BlockFlag;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.entities.EntityGroup;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static io.anuke.mindustry.Vars.*;
import static io.anuke.mindustry.sounds.Sounds.unitExplode;

/**Base class for AI units.*/
public abstract class BaseUnit extends Unit implements ShooterTrait{

    protected static int timerIndex = 0;

    protected static final int timerTarget = timerIndex++;
    protected static final int timerShootLeft = timerIndex++;
    protected static final int timerShootRight = timerIndex++;

    protected UnitType type;
    protected Timer timer = new Timer(10);
    protected StateMachine state = new StateMachine();
    protected TargetTrait target;
    protected AIController controller;
    protected UnitOrderType orderType = UnitOrderType.none;
    protected float orderX, orderY;

    protected boolean isWave;
    protected Squad squad;
    protected long spawner = -1;

    /**internal constructor used for deserialization, DO NOT USE*/
    public BaseUnit(){
    }

    @Remote(called = Loc.server)
    public static void onUnitDeath(BaseUnit unit){
        if(unit == null) return;

        if(Net.server() || !Net.active()){
            UnitDrops.dropItems(unit);
        }

        float explosiveness = 2f + (unit.inventory.hasItem() ? unit.inventory.getItem().item.explosiveness * unit.inventory.getItem().amount : 0f);
        float flammability = (unit.inventory.hasItem() ? unit.inventory.getItem().item.flammability * unit.inventory.getItem().amount : 0f);
        Damage.dynamicExplosion(unit.x, unit.y, flammability, explosiveness, 0f, unit.getSize() / 2f, Palette.darkFlame);

        unit.onSuperDeath();
        //visual only.
        if(Net.client()){
            Tile tile = world.tile(unit.spawner);
            if(tile != null){
                tile.block().unitRemoved(tile, unit);
            }

            unit.spawner = -1;
        }

        if(unit.getType().living){
            ScorchDecal.create(unit.x, unit.y, Color.valueOf("2b0000"));
        }else{
            ScorchDecal.create(unit.x, unit.y);
        }
        Effects.effect(ExplosionFx.explosion, unit);
        Effects.shake(2f, 2f, unit);
        Sound sound = unitExplode;
        if(Vars.soundController != null && sound != null){
            Vars.soundController.at(sound, unit.x, unit.y, 1f, 0.7f);}
        //must run afterwards so the unit's group is not null
        threads.runDelay(unit::remove);
    }

    @Override
    public float getDrag(){
        return type.drag;
    }

    /**Called when a command is recieved from the command center.*/
    public abstract void onCommand(UnitCommand command);

    /**Initialize the type and team of this unit. Only call once!*/
    public void init(UnitType type, Team team){
        if(this.type != null) throw new RuntimeException("This unit is already initialized!");

        this.type = type;
        this.team = team;
        this.isPlayerControllable = type.playerControllable;
        this.isRTSAIControllable = type.rtsAIControllable;
    }

    public boolean isCommanded(){
        return !isWave && world.indexer.getAllied(team, BlockFlag.comandCenter).size != 0 && world.indexer.getAllied(team, BlockFlag.comandCenter).first().entity instanceof CommandCenterEntity;
    }

    public UnitCommand getCommand(){
        if(isCommanded()){
            return world.indexer.getAllied(team, BlockFlag.comandCenter).first().<CommandCenterEntity>entity().command;
        }
        return null;
    }

    public UnitType getType(){
        return type;
    }

    public void setDirectTarget(TargetTrait target){
        this.target = target;
    }

    public UnitOrderType getOrderType(){
        return orderType;
    }

    public boolean hasOrder(){
        return orderType != UnitOrderType.none;
    }

    public void clearOrder(){
        orderType = UnitOrderType.none;
    }

    public void orderMove(float x, float y){
        orderType = UnitOrderType.move;
        orderX = x;
        orderY = y;
    }

    public void orderAttackMove(float x, float y){
        orderType = UnitOrderType.attackMove;
        orderX = x;
        orderY = y;
    }

    public void orderAttackTarget(float x, float y){
        orderType = UnitOrderType.attackTarget;
        orderX = x;
        orderY = y;
    }

    public TargetTrait getTarget(){
        return target;
    }

    public float getOrderX(){
        return orderX;
    }

    public float getOrderY(){
        return orderY;
    }

    public Tile getSpawner(){
        return world.tile(spawner);
    }

    public void setSpawner(Tile tile){
        this.spawner = tile.packedPosition();
    }

    public void setIntSpawner(long pos){
        this.spawner = pos;
    }

    /**Sets this to a 'wave' unit, which means it has slightly different AI and will not run out of ammo.*/
    public void setWave(){
        isWave = true;
    }

    public void setSquad(Squad squad){
        this.squad = squad;
        squad.units++;
    }

    public void rotate(float angle){
        rotation = Mathf.slerpDelta(rotation, angle, type.rotatespeed);
    }

    public boolean targetHasFlag(BlockFlag flag){
        return target instanceof TileEntity && ((TileEntity) target).tile.block().flags != null &&
            ((TileEntity) target).tile.block().flags.contains(flag);
    }

    public void updateRespawning(){
        if(spawner == -1) return;

        Tile tile = world.tile(spawner);
        if(tile != null && tile.entity != null){
            if(tile.entity instanceof SpawnerTrait){
                ((SpawnerTrait) tile.entity).updateSpawning(this);
            }
        }else{
            spawner = -1;
        }
    }

    public void setState(UnitState state){
        this.state.set(state);
    }

    public void retarget(Runnable run){
        if(timer.get(timerTarget, 20)){
            run.run();
        }
    }
    public boolean retarget(){
        return timer.get(timerTarget, 20);
    }

    /**Only runs when the unit has a target.*/
    public void behavior(){

    }

    /**
     * Fires at the current target whenever it is valid, in weapon range and aimed at.
     * Runs every tick, independent of AI states, orders and movement (modern-style
     * autonomous weapon targeting). No-op by default; combat classes override it.
     */
    protected void updateShooting(){

    }

    /**True when a valid enemy target is inside weapon range; movement code must not fight body rotation while this is set.*/
    protected boolean isAiming(){
        Weapon weapon = getWeapon();
        return target != null && weapon != null && weapon.getAmmo() != null
                && !Units.invalidateTarget(target, team, x, y, weapon.getAmmo().getRange());
    }

    public boolean isRetreating(){
        return false;
    }

    public void updateTargeting(){
        if(target == null || (target instanceof Unit && (target.isDead() || (!isRetreating() && target.getTeam() == team)))
        || (target instanceof TileEntity && ((TileEntity) target).tile.entity == null)){
            target = null;
        }
    }

    public void targetClosestAllyFlag(BlockFlag flag){
        Tile target = Geometry.findClosest(x, y, world.indexer.getAllied(team, flag));
        if(target != null) this.target = target.entity;
    }

    public void targetClosestEnemyFlag(BlockFlag flag){
        Tile target = Geometry.findClosest(x, y, world.indexer.getEnemy(team, flag));
        if(target != null) this.target = target.entity;
    }

    public void targetClosest(){
        TargetTrait next = Units.getClosestTarget(team, x, y, Math.max(getWeapon().getAmmo().getRange(), type.range), u -> type.targetAir || !u.isFlying());
        if(next != null) target = next;
    }

    public TileEntity getClosestEnemyCore(){

        for(Team enemy : Vars.state.teams.enemiesOf(team)){
            Tile tile = Geometry.findClosest(x, y, Vars.state.teams.get(enemy).cores);
            if(tile != null){
                return tile.entity;
            }
        }

        return null;
    }

    protected transient LongArray chunkPath;
    protected transient int chunkPathIndex;
    protected transient float chunkPathGoalX, chunkPathGoalY;
    protected transient int chunkPathCooldown;
    private final transient Translator chunkVec = new Translator();
    private static final float[] avoidOffsets = {40f, -40f, 80f, -80f, 120f, -120f};

    /** Returns true when a ground unit moving along this angle would hit solid terrain or deeper drowning liquid. */
    protected boolean blockedAtAngle(float angle){
        float look = type.hitsize * 0.75f + 5f;
        float lx = x + Angles.trnsx(angle, look);
        float ly = y + Angles.trnsy(angle, look);
        Tile t = world.tileWorld(lx, ly);
        if(t == null || t.solid()) return true;

        if(!isFlying()){
            Tile here = world.tileWorld(x, y);
            float curDrown = here == null ? 0f : here.floor().drownTime;
            if(t.floor().drownTime > curDrown + 0.01f) return true;
        }
        return false;
    }

    /**
     * Adjusts a movement angle to slide around obstacles instead of grinding into
     * walls/corners. Flying units pass through unchanged.
     */
    protected float avoidAngle(float angle){
        if(isFlying() || !blockedAtAngle(angle)) return angle;

        for(float off : avoidOffsets){
            if(!blockedAtAngle(angle + off)) return angle + off;
        }
        return angle;
    }

    /**
     * Steers along a cached waypoint path toward (gx, gy).
     * Tries the hierarchical chunk graph first (open world only), then a fine tile-level
     * A* over the team's flow snapshot (both modes). Returns false when no path is
     * available (caller should fall back to direct steering).
     */
    protected boolean steerAlongChunkPath(float gx, float gy){
        if(chunkPathCooldown > 0) chunkPathCooldown--;

        boolean valid = chunkPath != null && chunkPathIndex < chunkPath.size
                && Mathf.dst(chunkPathGoalX - x, chunkPathGoalY - y) < 8 * tilesize;

        if(!valid && chunkPathCooldown <= 0){
            chunkPathCooldown = 90;
            chunkPath = null;

            if(world.isOpenWorld()){
                chunkPath = world.pathfinder.findChunkPath(x, y, gx, gy);
            }

            if(chunkPath == null){
                chunkPath = world.pathfinder.findUnitPath(team, x, y, gx, gy);
            }

            if(chunkPath == null){
                Long fallback = world.pathfinder.findFallbackWaypoint(team, x, y, gx, gy);
                if(fallback != null){
                    chunkPath = new LongArray();
                    chunkPath.add(fallback);
                }
            }

            chunkPathIndex = 0;
            chunkPathGoalX = gx;
            chunkPathGoalY = gy;
        }

        if(chunkPath == null || chunkPath.size == 0) return false;

        long wp = chunkPath.items[Math.min(chunkPathIndex, chunkPath.size - 1)];
        float wx = (int)(wp >> 32) * tilesize + tilesize / 2f;
        float wy = (int)wp * tilesize + tilesize / 2f;

        if(Mathf.dst(wx - x, wy - y) < 3 * tilesize){
            if(chunkPathIndex >= chunkPath.size - 1) return false; //path consumed
            chunkPathIndex++;
            wp = chunkPath.items[chunkPathIndex];
            wx = (int)(wp >> 32) * tilesize + tilesize / 2f;
            wy = (int)wp * tilesize + tilesize / 2f;
        }

        float angle = angleTo(wx, wy);
        velocity.add(chunkVec.trns(angle, type.speed * Timers.delta()));
        if(!isAiming()) rotation = Mathf.slerpDelta(rotation, angle, type.rotatespeed);
        return true;
    }

    public UnitState getStartState(){
        return null;
    }

    protected boolean updateOrder(){
        if(!hasOrder()) return false;

        if(getOrderType() == UnitOrderType.attackMove){
            if(retarget()){
                targetClosest();
            }
            if(target != null && !Units.invalidateTarget(target, this) && distanceTo(target) < getWeapon().getAmmo().getRange()){
                rotate(angleTo(target));
            }
        }

        if(getOrderType() == UnitOrderType.attackTarget){
            if(target == null || target.isDead() || target.getTeam() == team){
                clearOrder();
                return false;
            }

            orderX = target.getX();
            orderY = target.getY();

            if(target != null && !Units.invalidateTarget(target, this) && distanceTo(target) < getWeapon().getAmmo().getRange()){
                rotate(angleTo(target));
            }
        }

        float dst = distanceTo(getOrderX(), getOrderY());
        Tile goalTile = world.tileWorld(getOrderX(), getOrderY());
        float blockRadius = goalTile != null && goalTile.block() != null && goalTile.block().size > 0 ? goalTile.block().size * tilesize / 2f : 0f;
        if(dst <= Math.max(getSize(), 10f) + blockRadius){
            clearOrder();
            return false;
        }

        float angle = angleTo(getOrderX(), getOrderY());
        float rad = angle * 0.01745329252f;
        velocity.add(type.speed * Timers.delta() * (float)Math.cos(rad),
                     type.speed * Timers.delta() * (float)Math.sin(rad));
        rotate(angle);
        return true;
    }

    public void updateDefaultAI(){
        if(!updateOrder()){
            state.update();
        }
    }

    protected void drawItems(){
        float backTrns = 4f, itemSize = 5f;
        if(inventory.hasItem()){
            ItemStack stack = inventory.getItem();
            int stored = Mathf.clamp(stack.amount / 6, 1, 8);

            for(int i = 0; i < stored; i++){
                float angT = i == 0 ? 0 : Mathf.randomSeedRange(i + 2, 60f);
                float lenT = i == 0 ? 0 : Mathf.randomSeedRange(i + 3, 1f) - 1f;
                Draw.rect(stack.item.region,
                    x + Angles.trnsx(rotation + 180f + angT, backTrns + lenT),
                    y + Angles.trnsy(rotation + 180f + angT, backTrns + lenT),
                    itemSize, itemSize, rotation);
            }
        }
    }

    @Override
    public boolean isValid(){
        return super.isValid() && isAdded();
    }

    @Override
    public Timer getTimer(){
        return timer;
    }

    @Override
    public int getShootTimer(boolean left){
        return left ? timerShootLeft : timerShootRight;
    }

    @Override
    public Weapon getWeapon(){
        return type.weapon;
    }

    @Override
    public TextureRegion getIconRegion(){
        return type.iconRegion;
    }

    @Override
    public int getItemCapacity(){
        return type.itemCapacity;
    }

    @Override
    public void interpolate(){
        super.interpolate();

        if(interpolator.values.length > 0){
            rotation = interpolator.values[0];
        }
    }

    @Override
    public float maxHealth(){
        return type.health;
    }

    @Override
    public float getArmor(){
        return type.armor;
    }

    @Override
    public float getSize(){
        return type.hitsize;
    }

    @Override
    public boolean isHealer(){
        return type.isHealer;
    }

    @Override
    public float getMass(){
        return type.mass;
    }

    @Override
    public boolean isFlying(){
        return type.isFlying;
    }

    @Override
    public void update(){
        hitTime -= Timers.delta();

        if(isDead()){
            updateRespawning();
            return;
        }

        if(Net.client()){
            interpolate();
            status.update(this);
            return;
        }

        avoidOthers(1.25f);

        if(spawner != -1 && (world.tile(spawner) == null || world.tile(spawner).entity == null)){
            damage(health);
        }

        if(squad != null){
            squad.update();
        }

        updateTargeting();

        if(controller != null){
            controller.updateUnit();
        }else{
            updateDefaultAI();
        }
        updateVelocityStatus();

        if(target != null) behavior();
        updateShooting();

        if(!world.isOpenWorld()){
            x = Mathf.clamp(x, tilesize, world.width() * tilesize - tilesize);
            y = Mathf.clamp(y, tilesize, world.height() * tilesize - tilesize);
        }
    }

    @Override
    public void draw(){
    }

    @Override
    public void drawLight(){
        boolean emit = emitLight != null ? emitLight : type.emitLight;
        float radius = (lightRadius < 0 ? type.lightRadius : lightRadius);
        float opacity = (lightOpacity < 0 ? type.lightOpacity : lightOpacity);
        Color color = lightColor == null ? type.lightColor : lightColor;

        if(emit && radius > 0.001f){
            Draw.color(color);
            io.anuke.mindustry.graphics.Shaders.light.region = Draw.region("circle");
            Draw.alpha(opacity);
            Draw.rect("circle", x, y, radius * 2, radius * 2);
            Draw.alpha(opacity * 0.5f);
            Draw.rect("circle", x, y, radius * 2, radius * 2);
        }
    }

    @Override
    public float getMaxVelocity(){
        return type.maxVelocity;
    }

    @Override
    public void removed(){
        super.removed();
        Tile tile = world.tile(spawner);
        if(tile != null && !Net.client()){
            tile.block().unitRemoved(tile, this);
        }
        spawner = -1;
    }

    @Override
    public float drawSize(){
        return 14;
    }

    @Override
    public void onDeath(){
        Call.onUnitDeath(this);
    }

    @Override
    public void added(){
        state.set(getStartState());

        health(maxHealth());

        if(isCommanded()){
            onCommand(getCommand());
        }
    }

    @Override
    public void getHitbox(Rectangle rectangle){
        rectangle.setSize(type.hitsize).setCenter(x, y);
    }

    @Override
    public void getHitboxTile(Rectangle rectangle){
        rectangle.setSize(type.hitsizeTile).setCenter(x, y);
    }

    @Override
    public EntityGroup targetGroup(){
        return unitGroups[team.ordinal()];
    }

    @Override
    public void writeSave(DataOutput stream) throws IOException{
        super.writeSave(stream);
        stream.writeByte(type.id);
        stream.writeBoolean(isWave);
        stream.writeLong(spawner);
    }

    @Override
    public void readSave(DataInput stream) throws IOException{
        super.readSave(stream);
        byte type = stream.readByte();
        this.isWave = stream.readBoolean();
        this.spawner = stream.readLong();

        this.type = content.getByID(ContentType.unit, type & 0xFF);
        add();
    }

    @Override
    public void write(DataOutput data) throws IOException{
        super.writeSave(data);
        data.writeByte(type.id);
    }

    @Override
    public void read(DataInput data, long time) throws IOException{
        float lastx = x, lasty = y, lastrot = rotation;
        super.readSave(data);
        this.type = content.getByID(ContentType.unit, data.readByte() & 0xFF);

        interpolator.read(lastx, lasty, x, y, time, rotation);
        rotation = lastrot;
    }

    public void onSuperDeath(){
        super.onDeath();
    }
}
