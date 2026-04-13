package io.anuke.mindustry.entities.units.types;

import com.badlogic.gdx.math.Vector2;
import io.anuke.annotations.Annotations.Loc;
import io.anuke.annotations.Annotations.Remote;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.content.fx.UnitFx;
import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.entities.Predict;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.traits.TargetTrait;
import io.anuke.mindustry.entities.units.BaseUnit;
import io.anuke.mindustry.entities.units.FlyingUnit;
import io.anuke.mindustry.entities.units.UnitCommand;
import io.anuke.mindustry.entities.units.UnitState;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.net.Net;
import io.anuke.mindustry.type.AmmoType;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.util.Mathf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static io.anuke.mindustry.Vars.*;

public class BlockDefenseDrone extends FlyingUnit { // Copy paste of the Alpha Drone, but instead of being used by the Player is used by the blocks
    static final float followDistance = 80f;

    public TileEntity leader;
    public float despawnTimer = 0f;
    public float despawnTime = 10f;

    public final UnitState attack = new UnitState() {
        @Override
        public void update() {
            if(leader == null || leader.isDead()){
                damage(99999f);
                return;
            }

            TargetTrait last = target;
            target = leader;

            if(last == null){
                circle(60f);
            }

            target = last;

            if(leader.lastDamager != null && leader.lastDamager instanceof TargetTrait && !((TargetTrait)leader.lastDamager).isDead() && distanceTo((TargetTrait)leader.lastDamager) < getWeapon().getAmmo().getRange() * 1.5f){
                target = (TargetTrait)leader.lastDamager;
            }else if(distanceTo(leader) < followDistance){
                targetClosest();
            }else{
                target = null;
            }

            if(target != null){
                attack(50f);
                despawnTimer = 0f;

                if((Mathf.angNear(angleTo(target), rotation, 15f) && distanceTo(target) < getWeapon().getAmmo().getRange())){
                    AmmoType ammo = getWeapon().getAmmo();

                    Vector2 to = Predict.intercept(BlockDefenseDrone.this, target, ammo.bullet.speed);
                    getWeapon().update(BlockDefenseDrone.this, to.x, to.y);
                }
            }

            if(target == null){
                despawnTimer += Timers.delta();
                if(despawnTimer > 60f * despawnTime){
                    Call.onDefenseDroneFade(BlockDefenseDrone.this);
                }
            }

            if(target == null && distanceTo(leader) < 8f){
                Call.onDefenseDroneFade(BlockDefenseDrone.this);
            }
            if(distanceTo(leader) > 500f){
                damage(99999f);
            }
        }
    };

    @Override
    public void removed(){
        super.removed();
        if(leader != null){
            leader.defenseDronesCount--;
        }
    }

    @Remote(called = Loc.server)
    public static void onDefenseDroneFade(BaseUnit drone){
        if(drone == null) return;
        drone.remove();
        Effects.effect(UnitFx.pickup, drone);
    }

    @Override
    public void onCommand(UnitCommand command){
        //nuh uh
    }

    @Override
    public void behavior(){
        //do nothing, behold the power of an DRONE
    }

    @Override
    public UnitState getStartState() {
        return attack;
    }

    @Override
    public void write(DataOutput stream) throws IOException {
        super.write(stream);
        stream.writeInt(leader == null ? -1 : leader.tile.id());
    }

    @Override
    public void read(DataInput stream, long time) throws IOException {
        super.read(stream, time);
        int id = stream.readInt();
        Tile tile = world.tile(id);
        if(tile != null && tile.entity != null){
            leader = tile.entity;
        }
    }

    @Override
    public void readSave(DataInput stream) throws IOException{
        super.readSave(stream);
    }
}
