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
import arc.math.Mathf;

import static io.anuke.mindustry.Vars.*;

public class BiomassInfectedBattleMission extends MissionWithStartingCore{
    final int spacing = 30;
    public static final int defaultXCorePos = 50;
    public static final int defaultYCorePos = 50;

    private float hiveSpawnTime;
    private boolean hiveSpawned = false;

    public BiomassInfectedBattleMission(){
        this(defaultXCorePos, defaultYCorePos);
    }

    public BiomassInfectedBattleMission(int xCorePos, int yCorePos){
        super(xCorePos, yCorePos);
        this.hiveSpawnTime = Mathf.random(5f, 20f) * 60f * 60f;
    }

    @Override
    public boolean isInfectable(){
        return true;
    }

    @Override
    public boolean isInfected(){
        return false;
    }

    @Override
    public String getIcon(){
        return "icon-mission-battle";
    }

    @Override
    public GameMode getMode(){
        return GameMode.noWaves;
    }

    @Override
    public String displayString(){
        return Bundles.get("text.mission.battle");
    }

    @Override
    public Seq<Point2> getSpawnPoints(Generation gen){
        return Seq.with(new Point2(50, 50), new Point2(gen.width - 1 - spacing, gen.height - 1 - spacing), new Point2(spacing, gen.height - 1 - spacing));
    }

    @Override
    public void generate(Generation gen){
        generateCoreAtFirstSpawnPoint(gen, defaultTeam);

        if(state.teams.get(defaultTeam).cores.size == 0){
            return;
        }

        Tile core = state.teams.get(defaultTeam).cores.first();
        int enx = gen.width - 1 - spacing;
        int eny = gen.height - 1 - spacing;
        new FortressGenerator().generate(gen, Team.red, core.x, core.y, enx, eny);
    }

    @Override
    public void update(){
        if(!hiveSpawned && arc.util.Timers.time() >= hiveSpawnTime){
            io.anuke.mindustry.ai.MassAI.spawnInitialHive();
            hiveSpawned = true;
        }
    }

    @Override
    public boolean isComplete(){
        // Check for enemies of default team (Team.red)
        for(Team team : Vars.state.teams.enemiesOf(Vars.defaultTeam)){
            if(Vars.state.teams.isActive(team)){
                return false;
            }
        }
        // Check for The Mass
        return !Vars.state.teams.isActive(Team.themass);
    }
}
