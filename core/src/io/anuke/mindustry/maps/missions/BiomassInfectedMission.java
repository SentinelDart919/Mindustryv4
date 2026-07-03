package io.anuke.mindustry.maps.missions;

import arc.math.geom.Point2;
import arc.struct.Seq;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.content.blocks.StorageBlocks;
import io.anuke.mindustry.game.GameMode;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.maps.generation.FortressGenerator;
import io.anuke.mindustry.maps.generation.Generation;
import io.anuke.mindustry.world.Tile;
import arc.util.Strings;

import static io.anuke.mindustry.Vars.defaultTeam;
import static io.anuke.mindustry.Vars.state;

public class BiomassInfectedMission extends MissionWithStartingCore{
    final int spacing = 30;
    public static final int defaultXCorePos = 50;
    public static final int defaultYCorePos = 50;

    public BiomassInfectedMission(){
        this(defaultXCorePos, defaultYCorePos);
    }

    public BiomassInfectedMission(int xCorePos, int yCorePos){
        super(xCorePos, yCorePos);
    }

    @Override
    public boolean isInfected(){
        return true;
    }

    @Override
    public String getIcon(){
        return "icon-mission-infected";
    }

    @Override
    public GameMode getMode(){
        return GameMode.customAttackMode;
    }

    @Override
    public String displayString(){
        return Bundles.get("text.mission.biomassinfected");
    }

    @Override
    public Seq<Point2> getSpawnPoints(Generation gen){
        return Seq.with(new Point2(50, 50), new Point2(gen.width - 1 - spacing, gen.height - 1 - spacing));
    }

    @Override
    public void generate(Generation gen){
        generateCoreAtFirstSpawnPoint(gen, defaultTeam);

        if(state.teams.get(defaultTeam).cores.size == 0){
            return;
        }

        Tile playerCore = state.teams.get(defaultTeam).cores.first();
        Tile hiveCore = findHiveCore(gen);

        if(hiveCore == null){
            Seq<Point2> spawnPoints = getSpawnPoints(gen);
            if(spawnPoints.size < 2){
                return;
            }

            Point2 enemySpawn = spawnPoints.get(1);
            hiveCore = gen.tiles[enemySpawn.x][enemySpawn.y];
            hiveCore.setBlock(StorageBlocks.hive);
            hiveCore.setTeam(Team.themass);
            state.teams.get(Team.themass).cores.add(hiveCore);
        }

        Vars.infection.infectAll(gen.tiles);
    }

    @Override
    public boolean isComplete(){
        return !Vars.state.teams.isActive(Team.themass);
    }

    private Tile findHiveCore(Generation gen){
        if(state.teams.get(Team.themass).cores.size > 0){
            return state.teams.get(Team.themass).cores.first();
        }

        for(int x = 0; x < gen.width; x++){
            for(int y = 0; y < gen.height; y++){
                Tile tile = gen.tile(x, y);
                if(tile != null && tile.block() == StorageBlocks.hive && tile.getTeam() == Team.themass){
                    state.teams.get(Team.themass).cores.add(tile);
                    return tile;
                }
            }
        }

        return null;
    }
}
