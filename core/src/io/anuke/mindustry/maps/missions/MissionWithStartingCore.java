package io.anuke.mindustry.maps.missions;

import arc.math.geom.Point2;
import arc.struct.Seq;
import io.anuke.mindustry.content.blocks.StorageBlocks;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.maps.generation.Generation;
import io.anuke.mindustry.world.Tile;

import static io.anuke.mindustry.Vars.state;

public abstract class MissionWithStartingCore extends Mission{
    /** Stores a custom starting location for the core, or null if the default calculation (map center) shall be used. */
    private final Point2 customStartingPoint;

    /** Default constructor. Missions created this way will have a player starting core in the center of the map. */
    MissionWithStartingCore(){
        this.customStartingPoint = null;
    }

    /**
     * Creates a mission with a core on a non-default location.
     * @param xCorePos The x coordinate of the custom core position.
     * @param yCorePos The y coordinate of the custom core position.
     */
    MissionWithStartingCore(int xCorePos, int yCorePos){
        this.customStartingPoint = new Point2(xCorePos, yCorePos);
    }

    /**
     * Generates a player core based on generation parameters.
     * @param gen  The generation parameters which provide the map size.
     * @param team The team to generate the core for.
     */
    public void generateCoreAtFirstSpawnPoint(Generation gen, Team team){
        Seq<Point2> spawnPoints = getSpawnPoints(gen);
        if(spawnPoints == null || spawnPoints.size == 0){
            throw new IllegalArgumentException("A MissionWithStartingCore subclass did not provide a spawn point in getSpawnPoints(). However, at least one point must always be provided.");
        }

        Tile startingCoreTile = gen.tiles[spawnPoints.first().x][spawnPoints.first().y];
        startingCoreTile.setBlock(StorageBlocks.core);
        startingCoreTile.setTeam(team);
        state.teams.get(team).cores.add(startingCoreTile);
    }

    /**
     * Retrieves the spawn point in the center of the map or at a custom location which was provided through the constructor.
     * @param gen The generation parameters which provide the map size.
     * @return The center of the map or a custom location.
     * @implNote Must return an array with at least one entry.
     */
    @Override
    public Seq<Point2> getSpawnPoints(Generation gen){
        if(this.customStartingPoint == null){
            return Seq.with(new Point2(gen.width / 2, gen.height / 2));
        }else{
            return Seq.with(this.customStartingPoint);
        }
    }
}
