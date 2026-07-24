package io.anuke.mindustry.entities.units.types;

import io.anuke.mindustry.entities.Units;
import io.anuke.mindustry.entities.units.FlyingUnit;
import io.anuke.mindustry.entities.units.UnitType;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.net.Net;
import io.anuke.mindustry.type.ContentType;
import io.anuke.mindustry.type.Weapon;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Angles;
import io.anuke.ucore.util.Mathf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static io.anuke.mindustry.Vars.content;

public class Revenant extends FlyingUnit{
    protected Weapon weapon;

    public Revenant(){
        itWobbles = false;
    }

    @Override
    public void init(UnitType type, Team team){
        super.init(type, team);
        this.weapon = type.weapon;
    }

    @Override
    protected void attack(float circleLength){
        moveTo(circleLength);
    }

    @Override
    public Weapon getWeapon(){
        return weapon;
    }

    public void setWeapon(Weapon weapon){
        this.weapon = weapon;
    }

    @Override
    protected void updateRotation(){
        if(!Units.invalidateTarget(target, this)){
            rotation = Mathf.slerpDelta(rotation, angleTo(target), type.rotatespeed);
        }else{
            rotation = Mathf.slerpDelta(rotation, velocity.angle(), type.baseRotateSpeed);
        }
    }

    @Override
    protected void wobble(){
        if(Net.client()) return;
        x += Mathf.sin(Timers.time() + id * 999, 25f, 0.08f) * Timers.delta();
        y += Mathf.cos(Timers.time() + id * 999, 25f, 0.08f) * Timers.delta();
    }

    @Override
    public void draw(){
        Draw.alpha(hitTime / hitDuration);

        Draw.rect(type.name, x, y, rotation - 90);

        drawItems();

        Draw.alpha(1f);

        for(int i : Mathf.signs){
            if(!getWeapon().weaponMirror && i < 0) continue;
            float tra = rotation - 90,
                    trY = -getWeapon().getRecoil(this, i > 0) + type.weaponOffsetY;
            float wx = x + Angles.trnsx(tra, type.weaponOffsetX * i, trY),
                    wy = y + Angles.trnsy(tra, type.weaponOffsetX * i, trY);
            Draw.rect(weapon.equipRegion, wx, wy, weaponAngles[i > 0 ? 1 : 0] - 90);
        }

        Draw.alpha(1f);
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
}
