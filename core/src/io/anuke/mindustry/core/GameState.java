package io.anuke.mindustry.core;

import io.anuke.mindustry.game.Difficulty;
import io.anuke.mindustry.game.EventType.StateChangeEvent;
import io.anuke.mindustry.game.GameMode;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.game.Teams;
import io.anuke.mindustry.net.Net;
import arc.Events;

import static io.anuke.mindustry.Vars.unitGroups;

public class GameState{
    /**Current wave number, can be anything in non-wave modes.*/
    public int wave = 1;
    /**Wave countdown in ticks.*/
    public float wavetime;
    /**Whether the game is in game over state.*/
    public boolean gameOver = false;
    /**The current game mode.*/
    public GameMode mode = GameMode.waves;
    /**The current difficulty for wave modes.*/
    public Difficulty difficulty = Difficulty.normal;
    /**Team data. Gets reset every new game.*/
    public Teams teams = new Teams();
    /**Number of enemies in the game; only used clientside in servers.*/
    public int enemies;
    /**Whether random biomass (hive) spawning is allowed.*/
    public boolean allowMassInfection = false;
    /**Whether to start with present biomass (hive).*/
    public boolean startWithBiomass = false;
    /**The team used as the enemy in waves and custom attack maps.*/
    public Team enemyTeam = Team.red;
    /**Bitmask of teams that should use RTS AI.*/
    public long rtsAIBits = 1L << Team.red.ordinal() | 1L << Team.green.ordinal() | 1L << Team.purple.ordinal() | 1L << Team.orange.ordinal();
    /**Current game state.*/
    private State state = State.menu;

    public int enemies(){
        return Net.client() ? enemies : unitGroups[enemyTeam.ordinal()].size();
    }

    public void set(State astate){
        Events.fire(new StateChangeEvent(state, astate));
        state = astate;
    }

    public boolean isPaused(){
        return is(State.paused) && !Net.active();
    }

    public boolean is(State astate){
        return state == astate;
    }

    public State getState(){
        return state;
    }

    public enum State{
        paused, playing, menu
    }
}
