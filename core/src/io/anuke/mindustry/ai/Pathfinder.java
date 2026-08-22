package io.anuke.mindustry.ai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.TimeUtils;
import io.anuke.mindustry.game.EventType.TileChangeEvent;
import io.anuke.mindustry.game.EventType.WorldLoadEvent;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.game.Teams.TeamData;
import io.anuke.mindustry.net.Net;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.meta.BlockFlag;
import io.anuke.ucore.core.Events;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.util.Geometry;
import io.anuke.ucore.util.Structs;

import static io.anuke.mindustry.Vars.*;


public class Pathfinder{
    private long maxUpdate = TimeUtils.millisToNanos(4);
    private PathData[] paths;
    private IntArray blocked = new IntArray();

    public Pathfinder(){
        Events.on(WorldLoadEvent.class, event -> clear());
        Events.on(TileChangeEvent.class, event -> {
            if(Net.client()) return;

            for(Team team : Team.all){
                TeamData data = state.teams.get(team);
                if(state.teams.isActive(team) && data.team != event.tile.getTeam()){
                    update(event.tile, data.team);
                }
            }

            update(event.tile, event.tile.getTeam());
        });
    }

    public void activateTeamPath(Team team){
        createFor(team);
    }

    public void recenter(int playerTX, int playerTY){
        if(paths == null) return;

        for(Team team : Team.all){
            PathData path = paths[team.ordinal()];
            if(path == null) continue;

            int halfGrid = path.gridSize / 2;
            int gridCenterX = path.offsetX + halfGrid;
            int gridCenterY = path.offsetY + halfGrid;

            int dx = playerTX - gridCenterX;
            int dy = playerTY - gridCenterY;

            if(Math.abs(dx) > halfGrid / 2 || Math.abs(dy) > halfGrid / 2){
                createFor(team);
            }
        }
    }

    public void update(){
        if(Net.client() || paths == null) return;

        for(Team team : Team.all){
            if(state.teams.isActive(team)){
                updateFrontier(team, maxUpdate);
            }
        }
    }

    public Tile getTargetTile(Team team, Tile tile){
        PathData path = paths[team.ordinal()];
        if(path == null) return tile;

        float[][] values = path.weights;
        if(values == null || tile == null) return tile;

        int ax = tile.x - path.offsetX;
        int ay = tile.y - path.offsetY;
        if(ax < 0 || ay < 0 || ax >= path.gridSize || ay >= path.gridSize) return tile;

        float value = values[ax][ay];

        Tile target = null;
        float tl = 0f;
        for(GridPoint2 point : Geometry.d8){
            int dx = tile.x + point.x, dy = tile.y + point.y;

            Tile other = world.tile(dx, dy);
            if(other == null) continue;

            int bx = dx - path.offsetX;
            int by = dy - path.offsetY;
            if(bx < 0 || by < 0 || bx >= path.gridSize || by >= path.gridSize) continue;

            if(values[bx][by] < value && (target == null || values[bx][by] < tl) &&
                    !other.solid() &&
                    !(point.x != 0 && point.y != 0 && (world.solid(tile.x + point.x, tile.y) || world.solid(tile.x, tile.y + point.y)))){ //diagonal corner trap
                target = other;
                tl = values[bx][by];
            }
        }

        if(target == null || tl == Float.MAX_VALUE) return tile;

        return target;
    }

    public float getValueforTeam(Team team, int x, int y){
        if(paths == null || team.ordinal() >= paths.length || paths[team.ordinal()] == null) return 0;
        PathData path = paths[team.ordinal()];
        int ax = x - path.offsetX;
        int ay = y - path.offsetY;
        if(ax < 0 || ay < 0 || ax >= path.gridSize || ay >= path.gridSize) return 0;
        return path.weights[ax][ay];
    }

    private boolean passable(Tile tile, Team team){
        return (!tile.solid()) || (tile.breakable() && (tile.target().getTeam() != team));
    }

    /**Clears the frontier, increments the search and sets up all flow sources.
     * This only occurs for active teams.*/
    private void update(Tile tile, Team team){
        //make sure team exists and has path data
        if(paths == null || team.ordinal() >= paths.length || paths[team.ordinal()] == null) return;

        PathData path = paths[team.ordinal()];

        int ax = tile.x - path.offsetX;
        int ay = tile.y - path.offsetY;
        if(ax < 0 || ay < 0 || ax >= path.gridSize || ay >= path.gridSize) return;

        //impassable tiles have a weight of float.max
        if(!passable(tile, team)){
            path.weights[ax][ay] = Float.MAX_VALUE;
        }

        //increment search, clear frontier
        path.search++;
        path.frontier.clear();
        path.lastSearchTime = TimeUtils.millis();

        //add all targets to the frontier
        for(Tile other : world.indexer.getEnemy(team, BlockFlag.target)){
            int ox = other.x - path.offsetX;
            int oy = other.y - path.offsetY;
            if(ox >= 0 && oy >= 0 && ox < path.gridSize && oy < path.gridSize){
                path.weights[ox][oy] = 0;
                path.searches[ox][oy] = path.search;
                path.frontier.addFirst(other);
            }
        }
    }

    private void createFor(Team team){
        PathData path = new PathData();
        path.search++;
        path.frontier.ensureCapacity((path.gridSize + path.gridSize) * 3);

        paths[team.ordinal()] = path;

        int hw = path.gridSize / 2;

        for(int x = 0; x < path.gridSize; x++){
            for(int y = 0; y < path.gridSize; y++){
                int wx = x + path.offsetX;
                int wy = y + path.offsetY;
                Tile tile = world.tile(wx, wy);

                if(tile == null){
                    path.weights[x][y] = Float.MAX_VALUE;
                    continue;
                }

                if(tile.block().flags != null && state.teams.areEnemies(tile.getTeam(), team)
                        && tile.block().flags.contains(BlockFlag.target)){
                    path.frontier.addFirst(tile);
                    path.weights[x][y] = 0;
                    path.searches[x][y] = path.search;
                }else{
                    path.weights[x][y] = Float.MAX_VALUE;
                }
            }
        }

        updateFrontier(team, -1);
    }

    private void updateFrontier(Team team, long nsToRun){
        PathData path = paths[team.ordinal()];

        long start = TimeUtils.nanoTime();

        while(path.frontier.size > 0 && (nsToRun < 0 || TimeUtils.timeSinceNanos(start) <= nsToRun)){
            Tile tile = path.frontier.removeLast();
            int ax = tile.x - path.offsetX;
            int ay = tile.y - path.offsetY;
            if(ax < 0 || ay < 0 || ax >= path.gridSize || ay >= path.gridSize) continue;

            float cost = path.weights[ax][ay];

            if(cost < Float.MAX_VALUE){
                for(GridPoint2 point : Geometry.d4){

                    int dx = tile.x + point.x, dy = tile.y + point.y;
                    Tile other = world.tile(dx, dy);

                    if(other != null){
                        int bx = dx - path.offsetX;
                        int by = dy - path.offsetY;
                        if(bx >= 0 && by >= 0 && bx < path.gridSize && by < path.gridSize){
                            if((path.weights[bx][by] > cost + other.cost || path.searches[bx][by] < path.search)
                                    && passable(other, team)){
                                path.frontier.addFirst(other);
                                path.weights[bx][by] = cost + other.cost;
                                path.searches[bx][by] = path.search;
                            }
                        }
                    }
                }
            }
        }
    }

    private void clear(){
        Timers.mark();

        paths = new PathData[Team.all.length];
        blocked.clear();

        for(Team team : Team.all){
            PathData path = new PathData();
            paths[team.ordinal()] = path;

            if(state.teams.isActive(team)){
                createFor(team);
            }
        }

        world.spawner.checkAllQuadrants();
    }

    class PathData{
        float[][] weights;
        int[][] searches;
        int search = 0;
        long lastSearchTime;
        Queue<Tile> frontier = new Queue<>();
        int gridSize;
        int offsetX, offsetY;

        PathData(){
            gridSize = world.width();

            if(world.isOpenWorld()){
                int playerTX = players.length > 0 && players[0] != null
                        ? MathUtils.floor(players[0].x / tilesize) : 0;
                int playerTY = players.length > 0 && players[0] != null
                        ? MathUtils.floor(players[0].y / tilesize) : 0;
                offsetX = playerTX - gridSize / 2;
                offsetY = playerTY - gridSize / 2;
            }else{
                offsetX = 0;
                offsetY = 0;
            }

            weights = new float[gridSize][gridSize];
            searches = new int[gridSize][gridSize];
        }
    }
}
