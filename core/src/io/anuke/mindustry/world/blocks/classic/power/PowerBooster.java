package io.anuke.mindustry.world.blocks.classic.power;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongArray;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.PowerBlock;
import io.anuke.mindustry.world.meta.BlockStat;
import io.anuke.mindustry.world.meta.StatUnit;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.graphics.Lines;

import static io.anuke.mindustry.Vars.tilesize;
import static io.anuke.mindustry.Vars.world;


public class PowerBooster extends PowerBlock{
    public int powerRange = 4;

    public PowerBooster(String name){
        super(name);
        solid = true;
        update = true;
        consumesPower = false;
        outputsPower = false;
        powerCapacity = 20f;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(BlockStat.powerRange, powerRange, StatUnit.blocks);
    }

    @Override
    public void update(Tile tile){
        scanLinks(tile);

        updatePowerGraph(tile);

        if(tile.entity.power.graph != null){
            tile.entity.power.graph.update();
        }
    }

    void scanLinks(Tile tile){
        LongArray unwanted = new LongArray();
        for(int i = 0; i < tile.entity.power.links.size; i++){
            unwanted.add(tile.entity.power.links.get(i));
        }

        for(int x = -powerRange; x <= powerRange; x++){
            for(int y = -powerRange; y <= powerRange; y++){
                if(x == 0 && y == 0) continue;
                if(x * x + y * y >= powerRange * powerRange) continue;

                Tile other = world.tile(tile.x + x, tile.y + y);
                if(other == null) continue;
                other = other.target();

                if(other.entity == null || other.entity.power == null || other.getTeamID() != tile.getTeamID())
                    continue;

                if(!tile.entity.power.links.contains(other.packedPosition())){
                    tile.entity.power.links.add(other.packedPosition());
                    other.entity.power.links.add(tile.packedPosition());
                }
                unwanted.removeValue(other.packedPosition());
            }
        }

        if(unwanted.size > 0){
            for(int i = 0; i < unwanted.size; i++){
                long pos = unwanted.get(i);
                Tile other = world.tile(pos);
                if(other != null && other.entity != null && other.entity.power != null){
                    other.entity.power.links.removeValue(tile.packedPosition());
                }
                tile.entity.power.links.removeValue(pos);
            }
        }
    }

    @Override
    public Array<Tile> getPowerConnections(Tile tile, Array<Tile> out){
        out.clear();

        for(Tile other : tile.entity.proximity()){
            if(other.entity != null && other.entity.power != null && other.getTeamID() == tile.getTeamID()
                    && !tile.entity.power.links.contains(other.packedPosition())){
                out.add(other);
            }
        }

        for(int i = 0; i < tile.entity.power.links.size; i++){
            Tile link = world.tile(tile.entity.power.links.get(i));
            if(link != null && link.entity != null && link.entity.power != null) out.add(link);
        }

        return out;
    }

    @Override
    public void drawSelect(Tile tile){
        Draw.color(Color.YELLOW);
        Lines.dashCircle(tile.drawx(), tile.drawy(), powerRange * tilesize);
        Draw.reset();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        Draw.color(Color.PURPLE);
        Lines.stroke(1f);
        Lines.dashCircle(x * tilesize, y * tilesize, powerRange * tilesize);
        Draw.reset();
    }
}