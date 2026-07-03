package io.anuke.mindustry.maps.missions;

import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Bundles;
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

public class AttackMission extends MissionWithStartingCore{
    final int spacing = 30;
    public static final int defaultXCorePos = 50;
    public static final int defaultYCorePos = 50;

    /** Creates a Attack mission with the player core being at (@defaultXCorePos, @defaultYCorePos) */
    public AttackMission(){
        this(defaultXCorePos, defaultYCorePos);
    }

    /**
     * Creates a wave survival with the player core being at a custom location.
     * @param xCorePos The X coordinate of the custom core position.
     * @param yCorePos The Y coordinate of the custom core position.
     */
    public AttackMission(int xCorePos, int yCorePos){
        super(xCorePos, yCorePos);
    }

    @Override
    public String getIcon(){
        return "icon-mission-battle";
    }

    @Override
    public GameMode getMode(){
        return GameMode.customAttackMode;
    }

    @Override
    public String displayString(){
        return Bundles.get("text.mission.battle");
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
        Tile enemyCore = findEnemyCore(gen);

        if(enemyCore == null){
            Seq<Point2> spawnPoints = getSpawnPoints(gen);
            if(spawnPoints.size < 2){
                return;
            }

            Point2 enemySpawn = spawnPoints.get(1);
            enemyCore = gen.tiles[enemySpawn.x][enemySpawn.y];
            enemyCore.setBlock(StorageBlocks.core);
            enemyCore.setTeam(Team.red);
            state.teams.get(Team.red).cores.add(enemyCore);
        }

        new FortressGenerator().generate(gen, Team.red, playerCore.x, playerCore.y, enemyCore.x, enemyCore.y);
    }

    @Override
    public boolean isComplete(){
        for(Team team : Vars.state.teams.enemiesOf(Vars.defaultTeam)){
            if(Vars.state.teams.isActive(team)){
                return false;
            }
        }
        return true;
    }

    private Tile findEnemyCore(Generation gen){
        if(state.teams.get(Team.red).cores.size > 0){
            return state.teams.get(Team.red).cores.first();
        }

        for(int x = 0; x < gen.width; x++){
            for(int y = 0; y < gen.height; y++){
                Tile tile = gen.tile(x, y);
                if(tile != null && tile.block() == StorageBlocks.core && tile.getTeam() == Team.red){
                    state.teams.get(Team.red).cores.add(tile);
                    return tile;
                }
            }
        }

        return null;
    }
}
