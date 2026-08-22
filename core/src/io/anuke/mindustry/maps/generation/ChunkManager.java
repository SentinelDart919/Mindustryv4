package io.anuke.mindustry.maps.generation;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongMap;
import com.badlogic.gdx.utils.LongSet;
import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.content.blocks.OreBlocks;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.units.BaseUnit;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.Floor;
import io.anuke.mindustry.world.blocks.BlockPart;
import io.anuke.mindustry.world.blocks.OreBlock;
import io.anuke.mindustry.world.blocks.storage.CoreBlock;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.noise.RidgedPerlin;
import io.anuke.ucore.noise.Simplex;
import io.anuke.ucore.util.Geometry;
import io.anuke.ucore.util.Log;
import io.anuke.ucore.util.SeedRandom;

import static io.anuke.mindustry.Vars.*;

public class ChunkManager{
    public static final int CHUNK_SIZE = 32;
    public static final int LOAD_RADIUS = 3;
    public static final int RENDER_RADIUS = 5;
    public static final int UNLOAD_CHECK_INTERVAL = 120;

    private final LongMap<WorldChunk> loadedChunks = new LongMap<>();
    private final LongSet coreChunks = new LongSet();
    private final long worldSeed;
    private final Simplex sim;
    private final Simplex sim2;
    private final Simplex sim3;
    private final RidgedPerlin rid;
    private final SeedRandom random;
    private final Simplex[] oreNoises;

    private int lastUnloadCheck = 0;
    private boolean generatingChunk = false;
    private String saveName;
    private final OpenWorldSaveManager saveManager = new OpenWorldSaveManager();

    public ChunkManager(long seed){
        this(seed, null);
    }

    public ChunkManager(long seed, String saveName){
        this.worldSeed = seed;
        this.saveName = saveName;
        this.sim = new Simplex(seed);
        this.sim2 = new Simplex(seed + 1);
        this.sim3 = new Simplex(seed + 2);
        this.rid = new RidgedPerlin((int)(seed + 4), 1);
        this.random = new SeedRandom(seed + 3);
        int oreCount = Item.getAllOres().size;
        this.oreNoises = new Simplex[oreCount];
        for(int i = 0; i < oreCount; i++){
            this.oreNoises[i] = new Simplex(seed + 100 + i);
        }
    }

    public long getSeed(){
        return worldSeed;
    }

    public static long packKey(int cx, int cy){
        return ((long)cx << 32) | (cy & 0xFFFFFFFFL);
    }

    public static int keyCx(long key){
        return (int)(key >> 32);
    }

    public static int keyCy(long key){
        return (int)key;
    }

    public void registerCoreChunks(int cx, int cy){
        for(int dx = -1; dx <= 1; dx++){
            for(int dy = -1; dy <= 1; dy++){
                coreChunks.add(packKey(cx + dx, cy + dy));
            }
        }
    }

    public WorldChunk getOrCreateChunk(int cx, int cy){
        long key = packKey(cx, cy);
        WorldChunk chunk = loadedChunks.get(key);
        if(chunk == null){
            if(generatingChunk){
                return null;
            }

            if(saveName != null){
                chunk = saveManager.loadChunk(saveName, cx, cy);
            }

            if(chunk == null){
                chunk = new WorldChunk(cx, cy);
                chunk.tiles = new Tile[CHUNK_SIZE * CHUNK_SIZE];
                generatingChunk = true;
                try{
                    generateChunkTerrain(chunk);
                }finally{
                    generatingChunk = false;
                }
                chunk.generated = true;
            }

            loadedChunks.put(key, chunk);
            updateChunkAndNeighborCliffs(cx, cy);
        }
        chunk.lastAccessFrame = (long)Timers.time();
        return chunk;
    }

    public String getSaveName(){
        return saveName;
    }

    public void setSaveName(String name){
        this.saveName = name;
    }

    public OpenWorldSaveManager getSaveManager(){
        return saveManager;
    }

    /** Updates cliffs/occlusion for a newly loaded chunk and recomputes cliff borders on neighbors. */
    private void updateChunkAndNeighborCliffs(int cx, int cy){
        WorldChunk chunk = loadedChunks.get(packKey(cx, cy));
        if(chunk == null || chunk.tiles == null) return;

        generatingChunk = true;
        try{
            for(int i = 0; i < chunk.tiles.length; i++){
                chunk.tiles[i].updateOcclusion();
            }

            for(int d = 0; d < 4; d++){
                int ncx = cx + Geometry.d4[d].x;
                int ncy = cy + Geometry.d4[d].y;
                WorldChunk neighbor = loadedChunks.get(packKey(ncx, ncy));
                if(neighbor == null || neighbor.tiles == null) continue;
                for(int i = 0; i < neighbor.tiles.length; i++){
                    neighbor.tiles[i].updateOcclusion();
                }
            }

            generateOresForChunk(chunk);

            for(int i = 0; i < chunk.tiles.length; i++){
                Tile tile = chunk.tiles[i];
                if(tile.floor() instanceof OreBlock && tile.hasCliffs()){
                    tile.setFloor(((OreBlock)tile.floor()).base);
                }
                if(tile.block() != Blocks.air && tile.hasCliffs() && !tile.block().isMultiblock() && !(tile.block() instanceof BlockPart)){
                    tile.setBlock(Blocks.air);
                }
            }

            for(int d = 0; d < 4; d++){
                int ncx = cx + Geometry.d4[d].x;
                int ncy = cy + Geometry.d4[d].y;
                WorldChunk neighbor = loadedChunks.get(packKey(ncx, ncy));
                if(neighbor == null || neighbor.tiles == null) continue;

                int ndx = Geometry.d4[d].x;
                int ndy = Geometry.d4[d].y;

                for(int lx = 0; lx < CHUNK_SIZE; lx++){
                    for(int ly = 0; ly < CHUNK_SIZE; ly++){
                        boolean onBorder = (ndx != 0 && (ndx > 0 ? lx == 0 : lx == CHUNK_SIZE - 1))
                                         || (ndy != 0 && (ndy > 0 ? ly == 0 : ly == CHUNK_SIZE - 1));
                        if(!onBorder) continue;

                        Tile tile = neighbor.tiles[lx + ly * CHUNK_SIZE];
                        int wx = neighbor.cx * CHUNK_SIZE + lx;
                        int wy = neighbor.cy * CHUNK_SIZE + ly;

                        if(tile.floor() instanceof OreBlock){
                            tile.setFloor(((OreBlock)tile.floor()).base);
                        }

                        if(tile.floor() instanceof Floor && ((Floor)tile.floor()).hasOres
                            && !tile.hasCliffs() && tile.block() == Blocks.air){
                            Array<Item> ores = Item.getAllOres();
                            int ox = wx + Short.MAX_VALUE;
                            int oy = wy + Short.MAX_VALUE;
                            Floor baseFloor = tile.floor();
                            for(int i = ores.size - 1; i >= 0; i--){
                                Item entry = ores.get(i);
                                Simplex noise = i < oreNoises.length ? oreNoises[i] : oreNoises[0];
                                if(noise.octaveNoise2D(1, 0.7, 1f / (4 + i * 2), ox, oy) / 4f +
                                    Math.abs(0.5f - noise.octaveNoise2D(2, 0.7, 1f / (50 + i * 2), ox, oy)) > 0.48f &&
                                    Math.abs(0.5f - noise.octaveNoise2D(1, 1, 1f / (55 + i * 4), ox, oy)) > 0.22f){
                                    tile.setFloor((Floor) OreBlocks.get(baseFloor, entry));
                                    break;
                                }
                            }
                        }

                        if(tile.block() != Blocks.air && tile.hasCliffs() && !tile.block().isMultiblock() && !(tile.block() instanceof BlockPart)){
                            tile.setBlock(Blocks.air);
                        }
                    }
                }
            }
        }finally{
            generatingChunk = false;
        }
    } // god you need to see how this bugged out like hell before

    private void generateOresForChunk(WorldChunk chunk){
        // needs to be called after cliff generation if not the ore will override the cliff block since ores doesn't have cliffs
        int worldStartX = chunk.cx * CHUNK_SIZE;
        int worldStartY = chunk.cy * CHUNK_SIZE;
        Array<Item> ores = Item.getAllOres();

        for(int lx = 0; lx < CHUNK_SIZE; lx++){
            for(int ly = 0; ly < CHUNK_SIZE; ly++){
                int wx = worldStartX + lx;
                int wy = worldStartY + ly;
                int x = wx + Short.MAX_VALUE;
                int y = wy + Short.MAX_VALUE;
                Tile tile = chunk.tiles[lx + ly * CHUNK_SIZE];

                if(!tile.floor().hasOres || tile.hasCliffs() || tile.block() != Blocks.air) continue;

                Floor baseFloor = tile.floor();
                for(int i = ores.size - 1; i >= 0; i--){
                    Item entry = ores.get(i);
                    Simplex noise = i < oreNoises.length ? oreNoises[i] : oreNoises[0];
                    if(noise.octaveNoise2D(1, 0.7, 1f / (4 + i * 2), x, y) / 4f +
                        Math.abs(0.5f - noise.octaveNoise2D(2, 0.7, 1f / (50 + i * 2), x, y)) > 0.48f &&
                        Math.abs(0.5f - noise.octaveNoise2D(1, 1, 1f / (55 + i * 4), x, y)) > 0.22f){
                        tile.setFloor((Floor) OreBlocks.get(baseFloor, entry));
                        break;
                    }
                }
            }
        }
    }

    // rebuild tile entities and occlusion after loading from disk need to be called after all chunks are loaded
    public void rebuildAfterLoad(){
        Array<WorldChunk> snapshot = loadedChunks.values().toArray();
        for(WorldChunk chunk : snapshot){
            if(chunk.tiles == null) continue;
            for(int i = 0; i < chunk.tiles.length; i++){
                chunk.tiles[i].rebuildEntity();
            }
        }
        for(WorldChunk chunk : snapshot){
            if(chunk.tiles == null) continue;
            for(int i = 0; i < chunk.tiles.length; i++){
                Tile tile = chunk.tiles[i];
                tile.updateOcclusion();
                if(tile.entity != null){
                    tile.entity.updateProximity();
                }
            }
        }
        for(WorldChunk chunk : snapshot){
            if(chunk.tiles == null) continue;
            for(int i = 0; i < chunk.tiles.length; i++){
                Tile tile = chunk.tiles[i];
                if(tile.block() instanceof CoreBlock){
                    state.teams.get(tile.getTeam()).cores.add(tile);
                }
            }
        }
    }

    /** Save all loaded chunks to disk. */
    public void saveAllChunks(){
        if(saveName == null) return;
        int count = 0;
        for(WorldChunk chunk : loadedChunks.values()){
            if(chunk.generated){
                saveManager.saveChunk(saveName, chunk);
                count++;
            }
        }
        if(!headless && players[0] != null){
            saveManager.saveEntities(saveName);
        }
        OpenWorldSaveManager.OpenWorldMeta meta = saveManager.readMeta(saveName);
        if(meta == null){
            meta = new OpenWorldSaveManager.OpenWorldMeta();
            meta.name = saveName;
            meta.seed = worldSeed;
            meta.dateCreated = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()); // fix
        }
        meta.chunksSaved = count;
        if(!headless && players[0] != null){
            meta.playerX = players[0].x;
            meta.playerY = players[0].y;
            meta.playerTeam = players[0].getTeam().ordinal();
        }
        saveManager.writeMeta(saveName, meta);
    }

    public Tile getTile(int worldX, int worldY){
        int cx = MathUtils.floor((float)worldX / CHUNK_SIZE);
        int cy = MathUtils.floor((float)worldY / CHUNK_SIZE);
        long key = packKey(cx, cy);
        WorldChunk chunk = loadedChunks.get(key);
        if(chunk == null){
            if(generatingChunk){
                return null;
            }
            chunk = getOrCreateChunk(cx, cy);
            if(chunk == null) return null;
        }
        int lx = worldX - cx * CHUNK_SIZE;
        int ly = worldY - cy * CHUNK_SIZE;
        if(lx < 0 || lx >= CHUNK_SIZE || ly < 0 || ly >= CHUNK_SIZE){
            return null;
        }
        return chunk.tile(lx, ly);
    }

    public Tile getTileSafe(int worldX, int worldY){
        Tile tile = getTile(worldX, worldY);
        if(tile != null) return tile;
        return Tile.createRaw(worldX, worldY, (Floor)Blocks.deepwater, Blocks.air, (byte)0);
    }

    public boolean isChunkLoaded(int cx, int cy){
        return loadedChunks.containsKey(packKey(cx, cy));
    }

    public boolean isChunkActive(int cx, int cy){
        WorldChunk chunk = loadedChunks.get(packKey(cx, cy));
        return chunk != null && chunk.active;
    }

    public int getLoadedChunkCount(){
        return loadedChunks.size;
    }

    public Iterable<WorldChunk> getLoadedChunks(){
        return loadedChunks.values();
    }

    public void update(){
        if(players.length == 0 || players[0] == null) return;

        int playerCX = MathUtils.floor(players[0].x / (CHUNK_SIZE * tilesize));
        int playerCY = MathUtils.floor(players[0].y / (CHUNK_SIZE * tilesize));

        for(int dx = -LOAD_RADIUS; dx <= LOAD_RADIUS; dx++){
            for(int dy = -LOAD_RADIUS; dy <= LOAD_RADIUS; dy++){
                getOrCreateChunk(playerCX + dx, playerCY + dy);
            }
        }

        markActiveChunks();

        if((int)Timers.time() - lastUnloadCheck > UNLOAD_CHECK_INTERVAL){
            lastUnloadCheck = (int)Timers.time();
            unloadDistantChunks(playerCX, playerCY);
        }
    }

    private void markActiveChunks(){
        for(WorldChunk chunk : loadedChunks.values()){
            chunk.active = false;
        }

        for(TileEntity entity : tileGroup.all()){
            if(entity.tile == null) continue;
            int cx = MathUtils.floor((float)entity.tile.x / CHUNK_SIZE);
            int cy = MathUtils.floor((float)entity.tile.y / CHUNK_SIZE);
            long key = packKey(cx, cy);
            WorldChunk chunk = loadedChunks.get(key);
            if(chunk != null) chunk.active = true;
        }

        for(Team team : Team.all){
            for(BaseUnit unit : unitGroups[team.ordinal()].all()){
                int cx = MathUtils.floor(unit.x / (CHUNK_SIZE * tilesize));
                int cy = MathUtils.floor(unit.y / (CHUNK_SIZE * tilesize));
                long key = packKey(cx, cy);
                WorldChunk chunk = loadedChunks.get(key);
                if(chunk != null) chunk.active = true;
            }
        }

        for(int i = 0; i < players.length; i++){
            if(players[i] == null) continue;
            int cx = MathUtils.floor(players[i].x / (CHUNK_SIZE * tilesize));
            int cy = MathUtils.floor(players[i].y / (CHUNK_SIZE * tilesize));
            long key = packKey(cx, cy);
            WorldChunk chunk = loadedChunks.get(key);
            if(chunk != null) chunk.active = true;
        }
    }

    private void unloadDistantChunks(int playerCX, int playerCY){
        Array<Long> toRemove = new Array<>();

        for(LongMap.Entry<WorldChunk> entry : loadedChunks.entries()){
            WorldChunk chunk = entry.value;
            int cx = chunk.cx;
            int cy = chunk.cy;

            int dist = Math.max(Math.abs(cx - playerCX), Math.abs(cy - playerCY));

            if(dist <= LOAD_RADIUS) continue;
            if(chunk.active) continue;
            if(coreChunks.contains(packKey(cx, cy))) continue;

            if(dist > RENDER_RADIUS + 2){
                toRemove.add(entry.key);
            }
        }

        for(long key : toRemove){
            loadedChunks.remove(key);
        }
    }

    private void generateChunkTerrain(WorldChunk chunk){
        int worldStartX = chunk.cx * CHUNK_SIZE;
        int worldStartY = chunk.cy * CHUNK_SIZE;

        for(int lx = 0; lx < CHUNK_SIZE; lx++){
            for(int ly = 0; ly < CHUNK_SIZE; ly++){
                int wx = worldStartX + lx;
                int wy = worldStartY + ly;

                chunk.tiles[lx + ly * CHUNK_SIZE] = generateTileAt(wx, wy);
            }
        }

        for(int lx = 0; lx < CHUNK_SIZE; lx++){
            for(int ly = 0; ly < CHUNK_SIZE; ly++){
                int wx = worldStartX + lx;
                int wy = worldStartY + ly;
                Tile tile = chunk.tiles[lx + ly * CHUNK_SIZE];
                byte elevation = tile.getElevation();

                if(elevation <= 0) continue;

                for(int d = 0; d < 4; d++){
                    int nx = wx + Geometry.d4[d].x;
                    int ny = wy + Geometry.d4[d].y;
                    Tile neighbor = getTile(nx, ny);
                    if(neighbor != null && neighbor.getElevation() < elevation){
                        if(sim2.octaveNoise2D(1, 1, 1.0 / 8, wx + Short.MAX_VALUE, wy + Short.MAX_VALUE) > 0.8){
                            tile.setElevation(-1);
                        }
                        break;
                    }
                }
            }
        }

        for(int lx = 0; lx < CHUNK_SIZE; lx++){
            for(int ly = 0; ly < CHUNK_SIZE; ly++){
                int wx = worldStartX + lx;
                int wy = worldStartY + ly;
                Tile tile = chunk.tiles[lx + ly * CHUNK_SIZE];

                if(tile.block() != Blocks.air) continue;

                Block floor = tile.floor();
                if(floor instanceof OreBlock){
                    floor = ((OreBlock) floor).base;
                }

                if(floor == Blocks.grass){
                    if(random.chance(0.0145f) && hasWaterNear(wx, wy, 3.5f)){
                        tile.setBlock(Blocks.tree);
                    }
                }else if(floor == Blocks.sand){
                    if(random.chance(0.0075f) && hasWaterNear(wx, wy, 7f)){
                        tile.setBlock(Blocks.deadTree);
                    }
                }
            }
        }
    }

    private boolean hasWaterNear(int wx, int wy, float radius){
        int r = (int) Math.ceil(radius);
        for(int dx = -r; dx <= r; dx++){
            for(int dy = -r; dy <= r; dy++){
                if(dx * dx + dy * dy > radius * radius) continue;
                Tile t = getTile(wx + dx, wy + dy);
                if(t != null){
                    Block f = t.floor();
                    if(f == Blocks.water || f == Blocks.deepwater){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private Tile generateTileAt(int wx, int wy){
        int x = wx + Short.MAX_VALUE;
        int y = wy + Short.MAX_VALUE;

        double ridge = rid.getValue(x, y, 1f / 400f);
        double iceridge = rid.getValue(x + 99999, y, 1f / 300f) + sim3.octaveNoise2D(2, 1f, 1f / 14f, x, y) / 11f;
        double elevation = sim.octaveNoise2D(7, 0.62, 1f / 800, x, y) * 6.1 - 1 - ridge;
        double temp = sim3.octaveNoise2D(12, 0.6, 1f / 1100f, x - 120, y);
        double lake = sim2.octaveNoise2D(1, 1, 1f / 110f, x, y);

        elevation -= Math.pow(lake + 0.15f, 5);

        io.anuke.mindustry.world.Block floor;
        io.anuke.mindustry.world.Block wall = Blocks.air;

        if(elevation < 0.7){
            floor = Blocks.deepwater;
        }else if(elevation < 0.79){
            floor = Blocks.water;
        }else if(elevation < 0.85){
            floor = Blocks.sand;
        }else if(elevation < 2.5 && temp > 0.5){
            floor = Blocks.sand;
        }else if(temp < 0.42){
            floor = Blocks.snow;
        }else if(temp < 0.5){
            floor = Blocks.stone;
        }else if(temp < 0.6){
            floor = Blocks.grass;
        }else if(temp + ridge / 2f < 0.8 || elevation < 1.3){
            floor = Blocks.blackstone;
            if(iceridge > 0.25){
                elevation++;
            }
        }else{
            floor = Blocks.lava;
        }

        if(elevation > 3.3 && iceridge > 0.25 && temp < 0.6f){
            elevation++;
            floor = Blocks.ice;
        }

        if(((io.anuke.mindustry.world.blocks.Floor)floor).liquidDrop != null){
            elevation = 0;
        }

        if(wall == Blocks.air && decoration.containsKey(floor) && random.chance(0.03)){
            wall = decoration.get(floor);
        }

        if(wall == Blocks.air && (floor == Blocks.snow || floor == Blocks.ice) && random.chance(0.0045)){
            wall = Blocks.frozenTree;
        }

        byte elev = (byte)Math.max(elevation, 0);
        return Tile.createRaw(wx, wy, (Floor)floor, wall, elev);
    }

    private static final com.badlogic.gdx.utils.ObjectMap<io.anuke.mindustry.world.Block, io.anuke.mindustry.world.Block> decoration;

    static{
        decoration = new com.badlogic.gdx.utils.ObjectMap<>();
        decoration.put(Blocks.grass, Blocks.shrub);
        decoration.put(Blocks.stone, Blocks.rock);
        decoration.put(Blocks.ice, Blocks.icerock);
        decoration.put(Blocks.snow, Blocks.icerock);
        decoration.put(Blocks.blackstone, Blocks.blackrock);
    }

    public static class WorldChunk{
        public final int cx, cy;
        public Tile[] tiles;
        public boolean generated = false;
        public boolean active = false;
        public long lastAccessFrame = 0;

        public WorldChunk(int cx, int cy){
            this.cx = cx;
            this.cy = cy;
        }

        public Tile tile(int localX, int localY){
            return tiles[localX + localY * CHUNK_SIZE];
        }
    }
}
