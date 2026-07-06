package io.anuke.mindustry.graphics;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.GL20;
import arc.graphics.Gfx;
import arc.graphics.Pixmap.Format;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.gl.FrameBuffer;
import arc.struct.Seq;
import arc.util.Disposable;
import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.game.EventType.TileChangeEvent;
import io.anuke.mindustry.game.EventType.WorldLoadGraphicsEvent;
import io.anuke.mindustry.world.Tile;
import arc.Core;
import arc.Events;
import arc.Graphics;
import arc.entities.EntityDraw;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;

import java.nio.ByteBuffer;

import static io.anuke.mindustry.Vars.*;

/**Used for rendering fog of war. A framebuffer is used for this.*/
public class FogRenderer implements Disposable{
    private TextureRegion region = new TextureRegion();
    private FrameBuffer buffer;
    private ByteBuffer pixelBuffer;
    private Seq<Tile> changeQueue = new Seq<>();
    private int shadowPadding;
    private boolean dirty;

    public FogRenderer(){
        Events.on(WorldLoadGraphicsEvent.class, event -> {
            dispose();

            shadowPadding = -1;

            buffer = new FrameBuffer(Format.RGBA8888, world.width(), world.height(), false);
            changeQueue.clear();

            //clear buffer to black
            buffer.begin();
            Core.graphics.clear(0, 0, 0, 1f);
            buffer.end();

            for(int x = 0; x < world.width(); x++){
                for(int y = 0; y < world.height(); y++){
                    Tile tile = world.tile(x, y);
                    if(tile.getTeam() == players[0].getTeam() && tile.block().synthetic() && tile.block().viewRange > 0){
                        changeQueue.add(tile);
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
        Core.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        Core.gl.glReadPixels(0, 0, world.width(), world.height(), GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixelBuffer);

        pixelBuffer.position(0);
        for(int i = 0; i < world.width() * world.height(); i++){
            byte r = pixelBuffer.get();
            if(r != 0){
                world.tile(i).setVisibility((byte)1);
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

        float vw = Core.camera.width * Core.camera.zoom;
        float vh = Core.camera.height * Core.camera.zoom;

        float px = Core.camera.position.x - vw / 2f;
        float py = Core.camera.position.y - vh / 2f;

        float u = (px / tilesize) / buffer.getWidth();
        float v = (py / tilesize) / buffer.getHeight();

        float u2 = ((px + vw) / tilesize) / buffer.getWidth();
        float v2 = ((py + vh) / tilesize) / buffer.getHeight();

        Draw.proj(0, 0, buffer.getWidth() * tilesize, buffer.getHeight() * tilesize);

        Draw.color(Color.white);

        buffer.begin();

        Gfx.beginClip((-shadowPadding), (-shadowPadding), (world.width() + shadowPadding*2), (world.height() + shadowPadding*2));

        Gfx.begin();
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
            for(int i = 0; i < world.width() * world.height(); i++){
                Tile tile = world.tile(i);
                if(tile.discovered()){
                    Fill.rect(tile.worldx(), tile.worldy(), tilesize, tilesize);
                }
            }
            dirty = false;
        }

        EntityDraw.setClip(true);
        Gfx.end();
        buffer.end();

        Gfx.endClip();

        region.texture = buffer.getTexture();
        region.set(u, v2, u2, v);

        Draw.proj(Core.camera);
        Gfx.shader(Shaders.fog);
        renderer.pixelSurface.getBuffer().begin();
        Gfx.begin();

        Draw.rect(region, px + vw/2f, py + vh/2f, vw, vh);

        Gfx.end();
        renderer.pixelSurface.getBuffer().end();
        Gfx.shader();

        Gfx.begin();
        Draw.rect(Draw.wrap(renderer.pixelSurface.getTexture()), Gfx.getWidth()/2f, Gfx.getHeight()/2f, Gfx.getWidth(), -Gfx.getHeight());
        Gfx.end();
    }

    public Texture getTexture(){
        return buffer.getTexture();
    }

    @Override
    public void dispose(){
        if(buffer != null) buffer.dispose();
    }
}

