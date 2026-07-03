package io.anuke.mindustry.maps;

import arc.graphics.Texture;
import arc.struct.Seq;
import io.anuke.annotations.Annotations.Serialize;
import io.anuke.mindustry.game.Saves.SaveSlot;
import io.anuke.mindustry.game.SpawnGroup;
import io.anuke.mindustry.maps.missions.*;
import io.anuke.mindustry.type.ItemStack;
import arc.struct.Bits;

import static io.anuke.mindustry.Vars.control;
import static io.anuke.mindustry.Vars.headless;

@Serialize
public class Sector{
    private static final Mission victoryMission = new VictoryMission();

    /**Position on the map, can be positive or negative.*/
    public short x, y;
    /**Whether this sector has already been completed.*/
    public boolean complete;
    /**Slot ID of this sector's save. -1 means no save has been created.*/
    public int saveID = -1;
    /**Num of missions in this sector that have been completed so far.*/
    public int completedMissions;

    /**Display texture. Needs to be disposed.*/
    public transient Texture texture;
    /**Missions of this sector-- what needs to be accomplished to unlock it.*/
    public transient Seq<Mission> missions = new Seq<>();
    /**Enemies spawned at this sector.*/
    public transient Seq<SpawnGroup> spawns;
    /**Difficulty of the sector, measured by calculating distance from origin and applying scaling.*/
    public transient int difficulty;
    /**Items the player starts with on this sector.*/
    public transient Seq<ItemStack> startingItems;

    public Mission getDominantMission(){
        for(Mission mission : missions){
            if(mission instanceof WaveMission || mission instanceof BattleMission){
                return mission;
            }
        }

        for(Mission mission : missions){
            if(mission instanceof BlockMission){
                return mission;
            }
        }
        return missions.first();
    }

    public Mission currentMission(){
        return completedMissions >= missions.size ? victoryMission : missions.get(completedMissions);
    }

    public boolean isInfectable(){
        return currentMission().isInfectable();
    }

    public boolean isInfected(){
        return currentMission().isInfected();
    }

    public int getSeed(){
        return Bits.packInt(x, y);
    }

    public SaveSlot getSave(){
        return !hasSave() ? null : control.saves.getByID(saveID);
    }

    public boolean hasSave(){
        return !headless && control.saves.getByID(saveID) != null;
    }

    public int packedPosition(){
        return Bits.packInt(x, y);
    }
}
