package io.anuke.mindustry.maps.missions;

import com.badlogic.gdx.utils.Array;
import io.anuke.mindustry.game.Difficulty;
import io.anuke.mindustry.game.GameMode;
import io.anuke.mindustry.game.SpawnGroup;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.game.Waves;
import io.anuke.mindustry.maps.Sector;
import io.anuke.mindustry.maps.generation.Generation;
import io.anuke.ucore.util.Bundles;

import static io.anuke.mindustry.Vars.*;

public class WaveExtraMission extends MissionWithStartingCore{
    public static int displayedFunds = 10;
    private static final int baseFunds = 10;
    private final int target;

    /**
     * Creates a wave survival mission with the player core being in the center of the map.
     * @param target The number of waves to be survived.
     */
    public WaveExtraMission(int target){
        super();
        this.target = target;
        displayedFunds = baseFunds;
    }

    /**
     * Creates a wave survival with the player core being at a custom location.
     * @param target The number of waves to be survived.
     * @param xCorePos The X coordinate of the custom core position.
     * @param yCorePos The Y coordinate of the custom core position.
     */
    public WaveExtraMission(int target, int xCorePos, int yCorePos){
        super(xCorePos, yCorePos);
        this.target = target;
        displayedFunds = baseFunds;
    }

    @Override
    public Array<SpawnGroup> getWaves(Sector sector){
        return Waves.getSpawns();
    }

    @Override
    public void generate(Generation gen){
        generateCoreAtFirstSpawnPoint(gen, Team.blue);
    }

    @Override
    public void onBegin(){
        super.onBegin();
        resetFunds();
        world.pathfinder.activateTeamPath(waveTeam);
    }

    @Override
    public GameMode getMode(){
        return GameMode.SiegeMode;
    }

    @Override
    public String displayString(){
        String missionText = state.wave > target ?
            Bundles.format(
                state.enemies() > 1 ?
                "text.mission.wave.enemies" :
                "text.mission.wave.enemy", target, target, state.enemies()) :
            Bundles.format("text.mission.wave", state.wave, target, (int)(state.wavetime/60));
        return missionText;
    }

    @Override
    public String menuDisplayString(){
        
        return Bundles.format("text.mission.wave.menu", target);
    }

    @Override
    public void update(){
        if(state.wave > target){
            state.mode = GameMode.noWaves;
            
        }
    }

    @Override
    public boolean isComplete(){
        return state.wave > target && state.enemies() == 0;
    }

    public static void resetFunds(){
        displayedFunds = baseFunds;
    }

    public static void awardWaveFunds(int wave, Difficulty difficulty){
        displayedFunds += getWaveFundsGain(wave, difficulty);
    }

    public static void spendFunds(int amount){
        displayedFunds = Math.max(0, displayedFunds - amount);
    }

    public static int nextWaveFundsGain(int wave, Difficulty difficulty){
        return getWaveFundsGain(wave + 1, difficulty);
    }

    private static int getWaveFundsGain(int wave, Difficulty difficulty){
        float waveScale = 1f + (wave - 1) * 0.2f;
        float difficultyScale = 1f / difficulty.UnitAmountScaling;
        return Math.max(baseFunds, Math.round(baseFunds * waveScale * difficultyScale));
    }
}
