package io.anuke.mindustry.io.versions;

import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.content.blocks.StorageBlocks;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.maps.Map;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.BlockPart;
import io.anuke.ucore.util.Bits;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static io.anuke.mindustry.Vars.*;

/**
 * Save version with short block and floor IDs, allowing more than 256 blocks.
 */
public class Save18 extends Save17{

    public Save18(){
        super(18);
    }

    @Override
    public void writeMap(DataOutputStream stream) throws IOException{

        //write world size
        stream.writeShort(world.width());
        stream.writeShort(world.height());

        for(int i = 0; i < world.width() * world.height(); i++){
            Tile tile = world.tile(i);

            stream.writeShort(tile.getFloorID());
            stream.writeShort(tile.getBlockID());
            stream.writeByte(tile.getElevation());

            if(tile.block() instanceof BlockPart){
                stream.writeByte(tile.link);
            }else if(tile.entity != null){
                stream.writeByte(Bits.packByte(tile.getTeamID(), tile.getRotation())); //team + rotation
                stream.writeShort((short) tile.entity.health); //health

                if(tile.entity.items != null) tile.entity.items.write(stream);
                if(tile.entity.power != null) tile.entity.power.write(stream);
                if(tile.entity.liquids != null) tile.entity.liquids.write(stream);
                if(tile.entity.cons != null) tile.entity.cons.write(stream);

                tile.entity.writeConfig(stream);
                tile.entity.write(stream);
            }else if(tile.block() == Blocks.air){
                int consecutives = 0;

                for(int j = i + 1; j < world.width() * world.height() && consecutives < 255; j++){
                    Tile nextTile = world.tile(j);

                    if(nextTile.getFloorID() != tile.getFloorID() || nextTile.block() != Blocks.air || nextTile.getElevation() != tile.getElevation()){
                        break;
                    }

                    consecutives++;
                }

                stream.writeByte(consecutives);
                i += consecutives;
            }
        }

        //write visibility, length-run encoded
        for(int i = 0; i < world.width() * world.height(); i++){
            Tile tile = world.tile(i);
            boolean discovered = tile.discovered();

            int consecutives = 0;

            for(int j = i + 1; j < world.width() * world.height() && consecutives < 32767*2-1; j++){
                Tile nextTile = world.tile(j);

                if(nextTile.discovered() != discovered){
                    break;
                }

                consecutives++;
            }

            stream.writeBoolean(discovered);
            stream.writeShort(consecutives);
            i += consecutives;
        }
    }

    @Override
    public void readMap(DataInputStream stream) throws IOException{
        short width = stream.readShort();
        short height = stream.readShort();

        if(world.getSector() != null){
            world.setMap(new Map("Sector " + world.getSector().x + ", " + world.getSector().y, width, height));
        }else if(world.getMap() == null){
            world.setMap(new Map("unknown", width, height));
        }

        world.beginMapLoad();

        Tile[][] tiles = world.createTiles(width, height);

        for(int i = 0; i < width * height; i++){
            int x = i % width, y = i / width;
            short floorid = stream.readShort();
            short wallid = stream.readShort();
            byte elevation = stream.readByte();

            Tile tile = new Tile(x, y, floorid, wallid);
            tile.setElevation(elevation);

            if(wallid == Blocks.blockpart.id){
                tile.link = stream.readByte();
            }else if(tile.entity != null){
                byte tr = stream.readByte();
                short health = stream.readShort();

                byte team = Bits.getLeftByte(tr);
                byte rotation = Bits.getRightByte(tr);

                Team t = Team.all[team];

                tile.setTeam(Team.all[team]);
                tile.entity.health = health;
                tile.setRotation(rotation);

                if(tile.entity.items != null) tile.entity.items.read(stream);
                if(tile.entity.power != null) tile.entity.power.read(stream);
                if(tile.entity.liquids != null) tile.entity.liquids.read(stream);
                if(tile.entity.cons != null) tile.entity.cons.read(stream);

                tile.entity.readConfig(stream);
                tile.entity.read(stream);

                if(tile.block() == StorageBlocks.core){
                    state.teams.get(t).cores.add(tile);
                }
            }else if(wallid == 0){
                int consecutives = stream.readUnsignedByte();

                for(int j = i + 1; j < i + 1 + consecutives; j++){
                    int newx = j % width, newy = j / width;
                    Tile newTile = new Tile(newx, newy, floorid, wallid);
                    newTile.setElevation(elevation);
                    tiles[newx][newy] = newTile;
                }

                i += consecutives;
            }

            tiles[x][y] = tile;
        }

        for(int i = 0; i < width * height; i++){
            boolean discovered = stream.readBoolean();
            int consecutives = stream.readUnsignedShort();
            if(discovered){
                for(int j = i + 1; j < i + 1 + consecutives; j++){
                    int newx = j % width, newy = j / width;
                    tiles[newx][newy].setVisibility((byte) 1);
                }
            }
            i += consecutives;
        }

        content.setTemporaryMapper(null);
        world.endMapLoad();
    }
}
