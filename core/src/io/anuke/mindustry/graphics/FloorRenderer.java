package io.anuke.mindustry.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongMap;
import io.anuke.mindustry.game.EventType.TileChangeEvent;
import io.anuke.mindustry.game.EventType.WorldLoadGraphicsEvent;
import io.anuke.mindustry.maps.generation.ChunkManager;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.Floor;
import io.anuke.ucore.core.Core;
import io.anuke.ucore.core.Events;
import io.anuke.ucore.core.Graphics;
import io.anuke.ucore.graphics.CacheBatch;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.graphics.Fill;
import io.anuke.ucore.util.Log;
import io.anuke.ucore.util.Mathf;

import static io.anuke.mindustry.Vars.tilesize;
import static io.anuke.mindustry.Vars.world;

public class FloorRenderer{
    private final static int chunksize = 30;
    private static final int numLayers = CacheLayer.values().length;

    private Chunk[][] cache;
    private boolean[][] dirty;
    private CacheBatch cbatch;
    private boolean[] usedLayers = new boolean[numLayers];
    private int chunksx, chunksy;
    private boolean initialized = false;
    private LongMap<Chunk> openWorldCache = new LongMap<>();

    public FloorRenderer(){
        Events.on(WorldLoadGraphicsEvent.class, event -> {
            reload();
        });
        Events.on(TileChangeEvent.class, event -> {
            recacheTile(event.tile.x, event.tile.y);
        });
    }

    public void recacheTile(int x, int y){
        if(dirty == null) return;
        if(world.isOpenWorld()){
            int cx = (int)Math.floor((float)x / chunksize);
            int cy = (int)Math.floor((float)y / chunksize);
            long key = ChunkManager.packKey(cx, cy);
            Chunk chunk = openWorldCache.get(key);
            if(chunk != null){
                chunk.allDirty = true;
            }
            return;
        }
        int cx = x / chunksize;
        int cy = y / chunksize;
        if(cx >= 0 && cy >= 0 && cx < dirty.length && cy < dirty[0].length){
            dirty[cx][cy] = true;
        }
    }

    public void drawFloor(){
        if(world.isOpenWorld()){
            drawFloorOpenWorld();
            return;
        }
        if(cache == null) return;

        OrthographicCamera camera = Core.camera;

        int minx = Math.max((int)((camera.position.x - camera.viewportWidth * camera.zoom / 2f) / (chunksize * tilesize)), 0);
        int miny = Math.max((int)((camera.position.y - camera.viewportHeight * camera.zoom / 2f) / (chunksize * tilesize)), 0);
        int maxx = Math.min(Mathf.ceil((camera.position.x + camera.viewportWidth * camera.zoom / 2f) / (chunksize * tilesize)), chunksx);
        int maxy = Math.min(Mathf.ceil((camera.position.y + camera.viewportHeight * camera.zoom / 2f) / (chunksize * tilesize)), chunksy);

        CacheLayer[] layers = CacheLayer.values();

        Graphics.end();

        boolean anyDirty = false;
        for(int x = minx; x < maxx; x++){
            for(int y = miny; y < maxy; y++){
                if(dirty[x][y]){
                    anyDirty = true;
                    break;
                }
            }
            if(anyDirty) break;
        }

        if(anyDirty){
            cbatch.clear();
            for(int x = 0; x < chunksx; x++){
                for(int y = 0; y < chunksy; y++){
                    dirty[x][y] = true;
                }
            }
            for(int x = minx; x < maxx; x++){
                for(int y = miny; y < maxy; y++){
                    dirty[x][y] = false;
                    cacheChunk(x, y);
                }
            }
        }

        beginDraw();

        for(int i = 0; i < numLayers; i++){
            CacheLayer layer = layers[i];

            layer.begin();

            for(int x = minx; x < maxx; x++){
                for(int y = miny; y < maxy; y++){
                    Chunk chunk = cache[x][y];
                    if(chunk == null || chunk.caches[i] == -1) continue;
                    cbatch.drawCache(chunk.caches[i]);
                }
            }

            layer.end();
        }

        endDraw();
        Graphics.begin();
    }

    private void drawFloorOpenWorld(){
        if(cbatch == null) return;

        OrthographicCamera camera = Core.camera;

        int minx = (int)Math.floor((camera.position.x - camera.viewportWidth * camera.zoom / 2f) / (chunksize * tilesize));
        int miny = (int)Math.floor((camera.position.y - camera.viewportHeight * camera.zoom / 2f) / (chunksize * tilesize));
        int maxx = Mathf.ceil((camera.position.x + camera.viewportWidth * camera.zoom / 2f) / (chunksize * tilesize));
        int maxy = Mathf.ceil((camera.position.y + camera.viewportHeight * camera.zoom / 2f) / (chunksize * tilesize));

        CacheLayer[] layers = CacheLayer.values();

        Graphics.end();

        boolean anyDirty = false;
        for(int x = minx; x <= maxx && !anyDirty; x++){
            for(int y = miny; y <= maxy && !anyDirty; y++){
                long key = ChunkManager.packKey(x, y);
                Chunk chunk = openWorldCache.get(key);
                if(chunk == null || chunk.allDirty) anyDirty = true;
            }
        }

        if(anyDirty){
            cbatch.clear();

            for(Chunk chunk : openWorldCache.values()){
                java.util.Arrays.fill(chunk.caches, -1);
                chunk.allDirty = true;
            }

            for(int x = minx; x <= maxx; x++){
                for(int y = miny; y <= maxy; y++){
                    long key = ChunkManager.packKey(x, y);
                    Chunk chunk = openWorldCache.get(key);
                    if(chunk == null){
                        chunk = new Chunk();
                        openWorldCache.put(key, chunk);
                    }
                    chunk.allDirty = false;
                    cacheChunkOpenWorld(x, y, chunk);
                }
            }
        }

        beginDraw();

        for(int i = 0; i < numLayers; i++){
            CacheLayer layer = layers[i];

            layer.begin();

            for(int x = minx; x <= maxx; x++){
                for(int y = miny; y <= maxy; y++){
                    long key = ChunkManager.packKey(x, y);
                    Chunk chunk = openWorldCache.get(key);
                    if(chunk == null || chunk.caches[i] == -1) continue;
                    cbatch.drawCache(chunk.caches[i]);
                }
            }

            layer.end();
        }

        endDraw();
        Graphics.begin();
    }

    private void cacheChunkOpenWorld(int cx, int cy, Chunk chunk){
        java.util.Arrays.fill(usedLayers, false);

        int startX = cx * chunksize;
        int startY = cy * chunksize;
        int endX = startX + chunksize;
        int endY = startY + chunksize;

        for(int tilex = startX; tilex < endX; tilex++){
            for(int tiley = startY; tiley < endY; tiley++){
                Tile tile = world.rawTile(tilex, tiley);
                if(tile != null){
                    usedLayers[tile.floor().cacheLayer.ordinal()] = true;
                }
            }
        }

        CacheLayer[] layers = CacheLayer.values();
        for(int i = 0; i < numLayers; i++){
            if(usedLayers[i]){
                cacheChunkLayerOpenWorld(cx, cy, chunk, layers[i]);
            }
        }
    }

    private void cacheChunkLayerOpenWorld(int cx, int cy, Chunk chunk, CacheLayer layer){
        Graphics.useBatch(cbatch);
        cbatch.begin();

        int startX = cx * chunksize;
        int startY = cy * chunksize;
        int endX = startX + chunksize;
        int endY = startY + chunksize;

        for(int tilex = startX; tilex < endX; tilex++){
            for(int tiley = startY; tiley < endY; tiley++){
                Tile tile = world.rawTile(tilex, tiley);

                if(tile == null) continue;

                Floor floor = tile.floor();

                if(floor.cacheLayer == layer){
                    floor.draw(tile);
                }else if(floor.cacheLayer.ordinal() < layer.ordinal()){
                    floor.drawNonLayer(tile);
                }
            }
        }

        cbatch.end();
        Graphics.popBatch();
        chunk.caches[layer.ordinal()] = cbatch.getLastCache();
    }

    public void invalidateOpenWorldCache(){
        for(Chunk chunk : openWorldCache.values()){
            chunk.allDirty = true;
        }
    }

    public void beginDraw(){
        if(cbatch == null) return;

        cbatch.setProjectionMatrix(Core.camera.combined);
        cbatch.beginDraw();

        Gdx.gl.glEnable(GL20.GL_BLEND);
    }

    public void endDraw(){
        if(cbatch == null) return;

        cbatch.endDraw();
    }

    // unused code, relic from the past rn not deleted bc cna be usefull in posterior
    /*
    public void drawLayer(CacheLayer layer){
        if(cache == null) return;

        OrthographicCamera camera = Core.camera;

        int minx = Math.max((int)((camera.position.x - camera.viewportWidth * camera.zoom / 2f) / (chunksize * tilesize)), 0);
        int miny = Math.max((int)((camera.position.y - camera.viewportHeight * camera.zoom / 2f) / (chunksize * tilesize)), 0);
        int maxx = Math.min(Mathf.ceil((camera.position.x + camera.viewportWidth * camera.zoom / 2f) / (chunksize * tilesize)), chunksx);
        int maxy = Math.min(Mathf.ceil((camera.position.y + camera.viewportHeight * camera.zoom / 2f) / (chunksize * tilesize)), chunksy);

        int layerOrd = layer.ordinal();

        for(int x = minx; x < maxx; x++){
            for(int y = miny; y < maxy; y++){
                if(dirty[x][y]){
                    dirty[x][y] = false;
                    cacheChunk(x, y);
                }
            }
        }

        layer.begin();

        for(int x = minx; x < maxx; x++){
            for(int y = miny; y < maxy; y++){
                Chunk chunk = cache[x][y];
                if(chunk == null || chunk.caches[layerOrd] == -1) continue;
                cbatch.drawCache(chunk.caches[layerOrd]);
            }
        }

        layer.end();
    }
    */

    private void cacheChunk(int cx, int cy){
        Chunk chunk = cache[cx][cy];
        if(chunk == null){
            chunk = cache[cx][cy] = new Chunk();
        }
        java.util.Arrays.fill(chunk.caches, -1);

        java.util.Arrays.fill(usedLayers, false);

        int startX = cx * chunksize;
        int startY = cy * chunksize;
        int endX = Math.min(startX + chunksize, world.width());
        int endY = Math.min(startY + chunksize, world.height());

        for(int tilex = startX; tilex < endX; tilex++){
            for(int tiley = startY; tiley < endY; tiley++){
                Tile tile = world.rawTile(tilex, tiley);
                if(tile != null){
                    usedLayers[tile.floor().cacheLayer.ordinal()] = true;
                }
            }
        }

        CacheLayer[] layers = CacheLayer.values();
        for(int i = 0; i < numLayers; i++){
            if(usedLayers[i]){
                cacheChunkLayer(cx, cy, chunk, layers[i]);
            }
        }
    }

    private void cacheChunkLayer(int cx, int cy, Chunk chunk, CacheLayer layer){
        Graphics.useBatch(cbatch);
        cbatch.begin();

        int startX = cx * chunksize;
        int startY = cy * chunksize;
        int endX = Math.min(startX + chunksize, world.width());
        int endY = Math.min(startY + chunksize, world.height());

        for(int tilex = startX; tilex < endX; tilex++){
            for(int tiley = startY; tiley < endY; tiley++){
                Tile tile = world.rawTile(tilex, tiley);

                if(tile == null) continue;

                Floor floor = tile.floor();

                if(floor.cacheLayer == layer){
                    floor.draw(tile);
                }else if(floor.cacheLayer.ordinal() < layer.ordinal()){
                    floor.drawNonLayer(tile);
                }
            }
        }

        cbatch.end();
        Graphics.popBatch();
        chunk.caches[layer.ordinal()] = cbatch.getLastCache();
    }

    public void reload(){
        if(cbatch != null) cbatch.dispose();

        if(world.isOpenWorld()){
            int worldSize = ChunkManager.CHUNK_SIZE * (ChunkManager.RENDER_RADIUS * 2 + 1);
            chunksx = worldSize / chunksize;
            chunksy = worldSize / chunksize;
            cache = null;
            dirty = null;
            cbatch = new CacheBatch(worldSize * worldSize * numLayers);
            openWorldCache.clear();
        }else{
            chunksx = Mathf.ceil((float)(world.width()) / chunksize);
            chunksy = Mathf.ceil((float)(world.height()) / chunksize);
            cache = new Chunk[chunksx][chunksy];
            dirty = new boolean[chunksx][chunksy];
            cbatch = new CacheBatch(world.width() * world.height() * numLayers);
            openWorldCache.clear();

            for(int x = 0; x < chunksx; x++){
                for(int y = 0; y < chunksy; y++){
                    dirty[x][y] = true;
                }
            }
        }

        initialized = true;

        Log.info("Floor cache allocated: {0}x{1} chunks", chunksx, chunksy);
    }

    private class Chunk{
        int[] caches = new int[numLayers];
        boolean allDirty = true;
    }
}
