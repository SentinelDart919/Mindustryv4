package io.anuke.mindustry.world.blocks.distribution;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.graphics.Layer;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.Autotiler;
import io.anuke.mindustry.world.meta.BlockGroup;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Mathf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static io.anuke.mindustry.Vars.tilesize;
import static io.anuke.mindustry.Vars.world;

public class TrainRail extends Block implements Autotiler{
    private final TextureRegion[] regions = new TextureRegion[5];

    public TrainRail(String name){
        super(name);
        rotate = true;
        update = true;
        size = 2;
        layer = Layer.overlay;
        group = BlockGroup.none;
    }

    @Override
    public void load(){
        super.load();
        TextureRegion base = Draw.region(name + "-0");
        for(int i = 0; i < regions.length; i++){
            regions[i] = Draw.region(name + "-" + i, base);
        }
    }

    @Override
    public void draw(Tile tile){
        TrainRailEntity entity = tile.entity();
        if(entity == null) return;
        Draw.rect(
            regions[Mathf.clamp(entity.blendbits, 0, regions.length - 1)],
            tile.drawx(), tile.drawy(),
            tilesize * size * entity.blendsclx, tilesize * size * entity.blendscly,
            tile.getRotation() * 90f
        );
    }

    @Override
    public TextureRegion[] getIcon(){
        if(icon == null){
            icon = new TextureRegion[]{Draw.region(name + "-0")};
        }
        return icon;
    }

    @Override
    public void onProximityUpdate(Tile tile){
        super.onProximityUpdate(tile);
        TrainRailEntity entity = tile.entity();
        if(entity == null) return;
        int[] bits = buildRailBlending(tile, tile.getRotation());
        entity.blendbits = bits[0];
        entity.blendsclx = bits[1];
        entity.blendscly = bits[2];
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
        Tile other = world.tile(otherx, othery);
        if(other == null || other.getTeamID() != tile.getTeamID() || !(other.block() instanceof TrainRail)) return false;

        int dx = other.x - tile.x;
        int dy = other.y - tile.y;
        return (Math.abs(dx) == 2 && dy == 0) || (Math.abs(dy) == 2 && dx == 0);
    }

    @Override
    public boolean canReplace(Block other){
        return false;
    }

    @Override
    public TileEntity newEntity(){
        return new TrainRailEntity();
    }

    private int[] buildRailBlending(Tile tile, int rotation){
        int[] blendresult = io.anuke.mindustry.world.blocks.Autotiler.AutotilerHolder.blendresult;
        blendresult[0] = 0;
        blendresult[1] = blendresult[2] = 1;

        int num =
            (blends(tile, rotation, 2) && blends(tile, rotation, 1) && blends(tile, rotation, 3)) ? 0 :
            (blends(tile, rotation, 1) && blends(tile, rotation, 3)) ? 1 :
            (blends(tile, rotation, 1) && blends(tile, rotation, 2)) ? 2 :
            (blends(tile, rotation, 3) && blends(tile, rotation, 2)) ? 3 :
            blends(tile, rotation, 1) ? 4 :
            blends(tile, rotation, 3) ? 5 :
            -1;

        transformCase(num, blendresult);
        return blendresult;
    }

    public boolean blends(Tile tile, int rotation, int direction){
        int worldDir = Mathf.mod(rotation - direction, 4);
        int dx = io.anuke.ucore.util.Geometry.d4[worldDir].x * 2;
        int dy = io.anuke.ucore.util.Geometry.d4[worldDir].y * 2;
        Tile other = world.tile(tile.x + dx, tile.y + dy);
        if(other != null) other = other.target();

        return other != null && other.getTeamID() == tile.getTeamID() &&
            blends(tile, rotation, other.x, other.y, other.getRotation(), other.block());
    }

    public static class TrainRailEntity extends TileEntity{
        public int blendbits;
        public int blendsclx = 1, blendscly = 1;

        @Override
        public void write(DataOutput stream) throws IOException{
            stream.writeInt(blendbits);
            stream.writeInt(blendsclx);
            stream.writeInt(blendscly);
        }

        @Override
        public void read(DataInput stream) throws IOException{
            blendbits = stream.readInt();
            blendsclx = stream.readInt();
            blendscly = stream.readInt();
        }
    }
}
