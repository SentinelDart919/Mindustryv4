package io.anuke.mindustry.maps.campaign;

import com.badlogic.gdx.utils.Array;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.maps.Sector;
import io.anuke.mindustry.maps.missions.*;
import io.anuke.mindustry.type.Item;
import io.anuke.ucore.util.Mathf;

public class OpenWorldSectorGenerator implements CampaignSectorGenerator{
    private final SerpuloSectorGenerator base = new SerpuloSectorGenerator();

    @Override
    public Array<Item> getOres(int x, int y, Array<Item> defaultOres){
        return base.getOres(x, y, defaultOres);
    }

    @Override
    public void initSector(Sector sector){
        base.initSector(sector);
        float rand = Mathf.randomSeed(sector.getSeed() + 99);
        
        if(rand < 0.4f){
            for(Mission mission : sector.missions){
                if(mission instanceof WaveMission){
                    mission.addEnemyTeam(Team.purple);
                }
            }
        }else if(rand < 0.7f){
            for(Mission mission : sector.missions){
                if(mission instanceof AttackMission || mission instanceof BattleMission){
                    mission.addEnemyTeam(Team.green);
                }
            }
        }else{
            for(Mission mission : sector.missions){
                mission.addEnemyTeam(Team.purple);
                mission.addEnemyTeam(Team.green);
            }
        }
        sector.spawns = new Array<>();
        for(Mission mission : sector.missions){
            sector.spawns.addAll(mission.getWaves(sector));
        }
    }
}
