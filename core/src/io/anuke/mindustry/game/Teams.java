package io.anuke.mindustry.game;

import arc.struct.ObjectSet;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.world.Tile;
import java.util.EnumSet;
import arc.struct.ObjectSet;

/**
 * Class for various team-based utilities.
 */
public class Teams{
    private TeamData[] map = new TeamData[Team.all.length];

    /**
     * Register a team.
     *
     * @param team The team type enum.
     * @param enemies The array of enemies of this team. Any team not in this array is considered neutral.
     */
    public void add(Team team, Team... enemies){
        java.util.EnumSet<Team> set = java.util.EnumSet.noneOf(Team.class);
        java.util.Collections.addAll(set, enemies);
        map[team.ordinal()] = new TeamData(team, set);
    }

    /**Returns team data by type.*/
    public TeamData get(Team team){
        if(map[team.ordinal()] == null){
            //By default, a non-defined team will be enemies of everything.
            Team[] others = new Team[Team.all.length-1];
            for(int i = 0, j = 0; i < Team.all.length; i++){
                if(Team.all[i] != team) others[j++] = Team.all[i];
            }
            add(team, others);
        }
        return map[team.ordinal()];
    }

    /**Returns whether a team is active, e.g. whether it has any cores remaining.*/
    public boolean isActive(Team team){
        //the selected enemy team is always active
        return (!Vars.state.mode.disableWaves && team == Vars.state.enemyTeam) || get(team).cores.size > 0;
    }

    /**Returns a set of all teams that are enemies of this team.*/
    public EnumSet<Team> enemiesOf(Team team){
        return get(team).enemies;
    }

    /**Returns whether {@param other} is an enemy of {@param #team}.*/
    public boolean areEnemies(Team team, Team other){
        return enemiesOf(team).contains(other);
    }

    public class TeamData{
        public final ObjectSet<Tile> cores = new ObjectSet<>();
        public final EnumSet<Team> enemies;
        public final Team team;
        public io.anuke.mindustry.ai.RtsAI rtsAI;

        public TeamData(Team team, EnumSet<Team> enemies){
            this.team = team;
            this.enemies = enemies;
        }
    }
}
