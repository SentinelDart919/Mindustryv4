package io.anuke.mindustry.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.IntSet.IntSetIterator;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.maps.MapTileData.DataPosition;
import io.anuke.mindustry.world.Block;
import io.anuke.ucore.core.Core;
import io.anuke.ucore.core.Graphics;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.graphics.IndexedRenderer;
import io.anuke.ucore.util.Structs;
import io.anuke.ucore.util.Bits;
import io.anuke.ucore.util.Geometry;

import static io.anuke.mindustry.Vars.content;
import static io.anuke.mindustry.Vars.tilesize;

public class MapRenderer implements Disposable{
    private static final int chunksize = 64;
    private IndexedRenderer[][] chunks;
    private IconDraw[] floats;
    private IntSet updates = new IntSet();
    private IntSet delayedUpdates = new IntSet();
    private MapEditor editor;
    private int width, height;
    private Color tmpColor = Color.WHITE.cpy();

    private static class IconDraw{
        final TextureRegion region;
        final float x, y, w, h, rotation;

        IconDraw(TextureRegion region, float x, float y, float w, float h, float rotation){
            this.region = region;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.rotation = rotation;
        }
    }

    public MapRenderer(MapEditor editor){
        this.editor = editor;
    }

    public void resize(int width, int height){
        if(chunks != null){
            for(int x = 0; x < chunks.length; x++){
                for(int y = 0; y < chunks[0].length; y++){
                    chunks[x][y].dispose();
                }
            }
        }

        chunks = new IndexedRenderer[(int) Math.ceil((float) width / chunksize)][(int) Math.ceil((float) height / chunksize)];

        for(int x = 0; x < chunks.length; x++){
            for(int y = 0; y < chunks[0].length; y++){
                chunks[x][y] = new IndexedRenderer(chunksize * chunksize * 2);
            }
        }
        this.width = width;
        this.height = height;
        floats = new IconDraw[width * height];
        updateAll();
    }


    public void draw(float tx, float ty, float tw, float th){
        Graphics.end();

        IntSetIterator it = updates.iterator();
        while(it.hasNext){
            int i = it.next();
            int x = i % width;
            int y = i / width;
            render(x, y);
        }
        updates.clear();

        updates.addAll(delayedUpdates);
        delayedUpdates.clear();

        for(int x = 0; x < chunks.length; x++){
            for(int y = 0; y < chunks[0].length; y++){
                IndexedRenderer mesh = chunks[x][y];

                if(mesh == null){
                    chunks[x][y] = new IndexedRenderer(chunksize * chunksize * 2);
                    mesh = chunks[x][y];
                }

                mesh.getTransformMatrix().setToTranslation(tx, ty, 0).scl(tw / (width * tilesize),
                        th / (height * tilesize), 1f);
                mesh.setProjectionMatrix(Core.batch.getProjectionMatrix());

                mesh.render(Core.atlas.getTextures().first());
            }
        }

        if(floats != null){
            Matrix4 oldTransform = new Matrix4(Core.batch.getTransformMatrix());
            Core.batch.setTransformMatrix(new Matrix4().setToTranslation(tx, ty, 0).scl(tw / (width * tilesize),
                    th / (height * tilesize), 1f));
            Core.batch.begin();
            for(int i = 0; i < floats.length; i++){
                IconDraw draw = floats[i];
                if(draw == null){
                    continue;
                }
                if(draw.rotation == 0){
                    Core.batch.draw(draw.region, draw.x, draw.y, draw.w, draw.h);
                }else{
                    Core.batch.draw(draw.region, draw.x, draw.y, draw.w / 2, draw.h / 2,
                            draw.w, draw.h, 1f, 1f, draw.rotation);
                }
            }
            Core.batch.end();
            Core.batch.setTransformMatrix(oldTransform);
        }

        Graphics.begin();
    }

    public void updatePoint(int x, int y){
        //TODO spread out over multiple frames?
        updates.add(x + y * width);
    }

    public void updateAll(){
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                render(x, y);
            }
        }
    }

    private void render(int wx, int wy){
        int x = wx / chunksize, y = wy / chunksize;
        IndexedRenderer mesh = chunks[x][y];
        //TileDataMarker data = editor.getMap().readAt(wx, wy);
        short bf = editor.getMap().read(wx, wy, DataPosition.floor);
        short bw = editor.getMap().read(wx, wy, DataPosition.wall);
        byte btr = (byte) editor.getMap().read(wx, wy, DataPosition.rotationTeam);
        byte elev = (byte) editor.getMap().read(wx, wy, DataPosition.elevation);
        byte rotation = Bits.getLeftByte(btr);
        Team team = Team.all[Bits.getRightByte(btr)];
        int index = (wx % chunksize) + (wy % chunksize) * chunksize;
        int tile = wx + wy * width;

        floats[tile] = null;

        Block floor = content.block(bf);
        Block wall = content.block(bw);

        TextureRegion region;

        if(editor.showFloor()){
            region = floor.getEditorIcon();
        }else{
            region = Draw.region("clear");
        }

        draw(mesh, index, tile, region, wx * tilesize, wy * tilesize, 8, 8, 0);

        if(bw != 0){
            if(editor.showBuildings()){
                region = wall.getEditorIcon();

                if(wall.rotate){
                    draw(mesh, index, tile, region,
                            wx * tilesize + wall.offset(), wy * tilesize + wall.offset(),
                            region.getRegionWidth(), region.getRegionHeight(), rotation * 90 - 90);
                }else{
                    draw(mesh, index, tile, region,
                            wx * tilesize + wall.offset() + (tilesize - region.getRegionWidth())/2f,
                            wy * tilesize + wall.offset() + (tilesize - region.getRegionHeight())/2f,
                            region.getRegionWidth(), region.getRegionHeight(), 0);
                }
            }else{
                region = Draw.region("clear");
                draw(mesh, index, tile, region, wx * tilesize, wy * tilesize, 8, 8, 0);
            }
        }

        boolean check = checkElevation(elev, wx, wy);

        if(editor.showBuildings() && (wall.update || wall.destructible)){
            mesh.setColor(team.color);
            region = Draw.region("block-border");
        }else if(editor.showFloor() && elev > 0 && check){
            mesh.setColor(tmpColor.fromHsv((360f * elev / 127f * 4f) % 360f, 0.5f + (elev / 4f) % 0.5f, 1f));
            region = Draw.region("block-elevation");
        }else if(editor.showFloor() && elev == -1){
            region = Draw.region("block-slope");
        }else{
            region = Draw.region("clear");
        }

        draw(mesh, index + chunksize * chunksize, tile, region,
                wx * tilesize - (wall.size/3) * tilesize, wy * tilesize - (wall.size/3) * tilesize,
                region.getRegionWidth(), region.getRegionHeight(), 0);
        mesh.setColor(Color.WHITE);
    }

    private void draw(IndexedRenderer mesh, int index, int tile, TextureRegion region, float x, float y, float w, float h, float rotation){
        if(region.getTexture() == Core.atlas.getTextures().first()){
            if(rotation == 0){
                mesh.draw(index, region, x, y, w, h);
            }else{
                mesh.draw(index, region, x, y, w, h, rotation);
            }
        }else{
            floats[tile] = new IconDraw(region, x, y, w, h, rotation);
        }
    }

    private boolean checkElevation(byte elev, int x, int y){
        for(GridPoint2 p : Geometry.d4){
            int wx = x + p.x, wy = y + p.y;
            if(!Structs.inBounds(wx, wy, editor.getMap().width(), editor.getMap().height())){
                return true;
            }
            byte value = (byte) editor.getMap().read(wx, wy, DataPosition.elevation);

            if(value < elev){
                return true;
            }else if(value > elev){
                delayedUpdates.add(wx + wy * width);
            }
        }
        return false;
    }

    @Override
    public void dispose(){
        if(chunks == null){
            return;
        }
        for(int x = 0; x < chunks.length; x++){
            for(int y = 0; y < chunks[0].length; y++){
                if(chunks[x][y] != null){
                    chunks[x][y].dispose();
                }
            }
        }
    }
}
