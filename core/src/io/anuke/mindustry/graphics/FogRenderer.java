package io.anuke.mindustry.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.game.EventType.TileChangeEvent;
import io.anuke.mindustry.game.EventType.WorldLoadGraphicsEvent;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Core;
import io.anuke.ucore.core.Events;
import io.anuke.ucore.core.Graphics;
import io.anuke.ucore.entities.EntityDraw;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.graphics.Fill;

import java.nio.ByteBuffer;

import static io.anuke.mindustry.Vars.*;

/**Used for rendering fog of war. A framebuffer is used for this.*/
public class FogRenderer implements Disposable{
    private static final int RECENTER_THRESHOLD = 16;
    private TextureRegion region = new TextureRegion();
    private FrameBuffer buffer;
    private ByteBuffer pixelBuffer;
    private Array<Tile> changeQueue = new Array<>();
    private int shadowPadding;
    private boolean dirty;
    private int fogOriginX, fogOriginY;

    public FogRenderer(){
        Events.on(WorldLoadGraphicsEvent.class, event -> {
            dispose();

            shadowPadding = -1;

            buffer = new FrameBuffer(Format.RGBA8888, world.width(), world.height(), false);
            changeQueue.clear();

            buffer.begin();
            Graphics.clear(0, 0, 0, 1f);
            buffer.end();

            if(world.isOpenWorld()){
                int playerTX = players.length > 0 && players[0] != null
                        ? (int)(players[0].x / tilesize) : 0;
                int playerTY = players.length > 0 && players[0] != null
                        ? (int)(players[0].y / tilesize) : 0;
                fogOriginX = playerTX - world.width() / 2;
                fogOriginY = playerTY - world.height() / 2;

                for(int x = 0; x < world.width(); x++){
                    for(int y = 0; y < world.height(); y++){
                        int wx = x + fogOriginX;
                        int wy = y + fogOriginY;
                        Tile tile = world.tile(wx, wy);
                        if(tile != null && tile.getTeam() == players[0].getTeam() && tile.block().synthetic() && tile.block().viewRange > 0){
                            changeQueue.add(tile);
                        }
                    }
                }
            }else{
                for(int x = 0; x < world.width(); x++){
                    for(int y = 0; y < world.height(); y++){
                        Tile tile = world.tile(x, y);
                        if(tile.getTeam() == players[0].getTeam() && tile.block().synthetic() && tile.block().viewRange > 0){
                            changeQueue.add(tile);
                        }
                    }
                }
            }

            pixelBuffer = ByteBuffer.allocateDirect(world.width() * world.height() * 4);
            dirty = true;
        });

        Events.on(TileChangeEvent.class, event -> threads.runGraphics(() -> {
            if(event.tile.getTeam() == players[0].getTeam() && event.tile.block().synthetic() && event.tile.block().viewRange > 0){
                changeQueue.add(event.tile);
            }
        }));
    }

    public void writeFog(){
        if(buffer == null) return;

        buffer.begin();
        pixelBuffer.position(0);
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        Gdx.gl.glReadPixels(0, 0, world.width(), world.height(), GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixelBuffer);

        pixelBuffer.position(0);
        int w = world.width();
        for(int i = 0; i < w * world.height(); i++){
            byte r = pixelBuffer.get();
            if(r != 0){
                if(world.isOpenWorld()){
                    int px = i % w;
                    int py = i / w;
                    int wx = px + fogOriginX;
                    int wy = py + fogOriginY;
                    Tile tile = world.rawTile(wx, wy);
                    if(tile != null){
                        tile.setVisibility((byte)1);
                    }
                }else{
                    world.tile(i).setVisibility((byte)1);
                }
            }
            pixelBuffer.position(pixelBuffer.position() + 3);
        }
        buffer.end();
    }

    public int getPadding(){
        return -shadowPadding;
    }

    public void draw(){
        if(buffer == null) return;

        float vw = Core.camera.viewportWidth * Core.camera.zoom;
        float vh = Core.camera.viewportHeight * Core.camera.zoom;

        float px = Core.camera.position.x - vw / 2f;
        float py = Core.camera.position.y - vh / 2f;

        Core.batch.getProjectionMatrix().setToOrtho2D(0, 0, buffer.getWidth() * tilesize, buffer.getHeight() * tilesize);

        Draw.color(Color.WHITE);

        buffer.begin();

        if(world.isOpenWorld()){
            if(players.length > 0 && players[0] != null){
                recenterFog();
            }

            if(dirty){
                Gdx.gl.glClearColor(0, 0, 0, 1f);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            }

            Graphics.begin();
            EntityDraw.setClip(false);

            float offX = -fogOriginX * tilesize;
            float offY = -fogOriginY * tilesize;

            for(Tile tile : changeQueue){
                float viewRange = tile.block().viewRange;
                if(viewRange < 0) continue;
                float drawx = (tile.x - fogOriginX) * tilesize + tilesize / 2f;
                float drawy = (tile.y - fogOriginY) * tilesize + tilesize / 2f;
                Fill.circle(drawx, drawy, viewRange);
            }
            changeQueue.clear();

            renderer.drawAndInterpolate(playerGroup, player -> !player.isDead() && player.getTeam() == players[0].getTeam(), player -> {
                Fill.circle(player.x + offX, player.y + offY, player.getViewDistance());
            });
            renderer.drawAndInterpolate(unitGroups[players[0].getTeam().ordinal()], unit -> !unit.isDead(), unit -> {
                Fill.circle(unit.x + offX, unit.y + offY, unit.getViewDistance());
            });

            if(dirty){
                for(int x = 0; x < world.width(); x++){
                    for(int y = 0; y < world.height(); y++){
                        int wx = x + fogOriginX;
                        int wy = y + fogOriginY;
                        Tile tile = world.rawTile(wx, wy);
                        if(tile != null && tile.discovered()){
                            Fill.rect(x * tilesize + tilesize / 2f, y * tilesize + tilesize / 2f, tilesize, tilesize);
                        }
                    }
                }
                dirty = false;
            }

            EntityDraw.setClip(true);
            Graphics.end();

            float u = ((px / tilesize) - fogOriginX) / buffer.getWidth();
            float v = ((py / tilesize) - fogOriginY) / buffer.getHeight();
            float u2 = (((px + vw) / tilesize) - fogOriginX) / buffer.getWidth();
            float v2 = (((py + vh) / tilesize) - fogOriginY) / buffer.getHeight();

            region.setTexture(buffer.getColorBufferTexture());
            region.setRegion(u, v2, u2, v);
        }else{
            float u = (px / tilesize) / buffer.getWidth();
            float v = (py / tilesize) / buffer.getHeight();
            float u2 = ((px + vw) / tilesize) / buffer.getWidth();
            float v2 = ((py + vh) / tilesize) / buffer.getHeight();

            Graphics.beginClip((-shadowPadding), (-shadowPadding), (world.width() + shadowPadding*2), (world.height() + shadowPadding*2));

            Graphics.begin();
            EntityDraw.setClip(false);

            renderer.drawAndInterpolate(playerGroup, player -> !player.isDead() && player.getTeam() == players[0].getTeam(), Unit::drawView);
            renderer.drawAndInterpolate(unitGroups[players[0].getTeam().ordinal()], unit -> !unit.isDead(), Unit::drawView);

            for(Tile tile : changeQueue){
                float viewRange = tile.block().viewRange;
                if(viewRange < 0) continue;
                Fill.circle(tile.drawx(), tile.drawy(), tile.block().viewRange);
            }

            changeQueue.clear();

            if(dirty){
                for(int x = 0; x < world.width(); x++){
                    for(int y = 0; y < world.height(); y++){
                        Tile tile = world.tile(x, y);
                        if(tile != null && tile.discovered()){
                            Fill.rect(tile.worldx(), tile.worldy(), tilesize, tilesize);
                        }
                    }
                }
                dirty = false;
            }

            EntityDraw.setClip(true);
            Graphics.end();

            region.setTexture(buffer.getColorBufferTexture());
            region.setRegion(u, v2, u2, v);
        }

        buffer.end();

        if(!world.isOpenWorld()){
            Graphics.endClip();
        }

        Core.batch.setProjectionMatrix(Core.camera.combined);
        Graphics.shader(Shaders.fog);
        renderer.pixelSurface.getBuffer().begin();
        Graphics.begin();

        Core.batch.draw(region, px, py, vw, vh);

        Graphics.end();
        renderer.pixelSurface.getBuffer().end();
        Graphics.shader();

        Graphics.setScreen();
        Core.batch.draw(renderer.pixelSurface.texture(), 0, Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), -Gdx.graphics.getHeight());
        Graphics.end();
    }

    private void recenterFog(){
        int playerTX = (int)(players[0].x / tilesize);
        int playerTY = (int)(players[0].y / tilesize);

        int halfW = world.width() / 2;
        int halfH = world.height() / 2;

        int localX = playerTX - fogOriginX;
        int localY = playerTY - fogOriginY;

        if(Math.abs(localX - halfW) > RECENTER_THRESHOLD || Math.abs(localY - halfH) > RECENTER_THRESHOLD){
            int newOriginX = playerTX - halfW;
            int newOriginY = playerTY - halfH;

            pixelBuffer.position(0);
            Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
            Gdx.gl.glReadPixels(0, 0, world.width(), world.height(), GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixelBuffer);

            int w = world.width();
            byte[] oldPixels = new byte[w * world.height() * 4];
            pixelBuffer.position(0);
            pixelBuffer.get(oldPixels);

            pixelBuffer.position(0);
            for(int by = 0; by < world.height(); by++){
                for(int bx = 0; bx < w; bx++){
                    int wx = bx + fogOriginX;
                    int wy = by + fogOriginY;
                    int newBx = wx - newOriginX;
                    int newBy = wy - newOriginY;
                    int dstIdx = (newBy * w + newBx) * 4;
                    int srcIdx = (by * w + bx) * 4;
                    if(newBx >= 0 && newBx < w && newBy >= 0 && newBy < world.height()){
                        pixelBuffer.put(dstIdx, oldPixels[srcIdx]);
                        pixelBuffer.put(dstIdx + 1, oldPixels[srcIdx + 1]);
                        pixelBuffer.put(dstIdx + 2, oldPixels[srcIdx + 2]);
                        pixelBuffer.put(dstIdx + 3, oldPixels[srcIdx + 3]);
                    }
                }
            }

            buffer.end();
            buffer.getColorBufferTexture().bind();
            Gdx.gl.glTexSubImage2D(GL20.GL_TEXTURE_2D, 0, 0, 0, w, world.height(), GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixelBuffer);
            buffer.begin();

            fogOriginX = newOriginX;
            fogOriginY = newOriginY;

            changeQueue.clear();
            for(int x = 0; x < world.width(); x++){
                for(int y = 0; y < world.height(); y++){
                    int wx = x + fogOriginX;
                    int wy = y + fogOriginY;
                    Tile tile = world.tile(wx, wy);
                    if(tile != null && tile.getTeam() == players[0].getTeam() && tile.block().synthetic() && tile.block().viewRange > 0){
                        changeQueue.add(tile);
                    }
                }
            }

            dirty = true;
        }
    }

    public Texture getTexture(){
        return buffer.getColorBufferTexture();
    }

    @Override
    public void dispose(){
        if(buffer != null) buffer.dispose();
    }
}
