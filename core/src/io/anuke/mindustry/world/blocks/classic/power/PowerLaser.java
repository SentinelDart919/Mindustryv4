package io.anuke.mindustry.world.blocks.classic.power;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongArray;
import io.anuke.mindustry.graphics.Layer;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.PowerBlock;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.graphics.Lines;
import io.anuke.ucore.graphics.Shapes;
import io.anuke.ucore.util.Mathf;

import static io.anuke.mindustry.Vars.tilesize;
import static io.anuke.mindustry.Vars.world;


public class PowerLaser extends PowerBlock{
    public Color color = Color.valueOf("ffdf5e");
    public Color noPowerColor = Color.valueOf("ef8a4e");
    public int laserRange = 6;
    public int laserDirections = 1;

    public PowerLaser(String name){
        super(name);
        rotate = true;
        solid = true;
        update = true;
        consumesPower = false;
        outputsPower = false;
        powerCapacity = 20f;
        health = 50;
        hasBloom = true;
        layer = Layer.power;
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

        for(int i = 0; i < laserDirections; i++){
            int rot = Mathf.mod(tile.getRotation() + i - laserDirections / 2, 4);
            Tile target = laserTarget(tile, rot);
            if(target == null) continue;

            target = target.target();
            if(target.entity == null || target.entity.power == null || target.getTeamID() != tile.getTeamID())
                continue;

            if(!tile.entity.power.links.contains(target.packedPosition())){
                tile.entity.power.links.add(target.packedPosition());
                target.entity.power.links.add(tile.packedPosition());
            }
            unwanted.removeValue(target.packedPosition());
        }

        if(unwanted.size > 0){
            for(int i = 0; i < unwanted.size; i++){
                long pos = unwanted.get(i);
                Tile other = world.tile(pos);
                if(other != null && other.entity != null && other.entity.power != null){
                    if(other.block() instanceof PowerLaser && ((PowerLaser) other.block()).hasLinkTarget(other, tile)){
                        continue;
                    }
                    other.entity.power.links.removeValue(tile.packedPosition());
                }
                tile.entity.power.links.removeValue(pos);
            }
        }
    }

    boolean hasLinkTarget(Tile tile, Tile target){
        for(int i = 0; i < laserDirections; i++){
            int rot = Mathf.mod(tile.getRotation() + i - laserDirections / 2, 4);
            if(laserTarget(tile, rot) == target) return true;
        }
        return false;
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

    Tile laserTarget(Tile tile, int rot){
        rot = Mathf.mod(rot, 4);
        int dx = 0, dy = 0;
        if(rot == 0) dx = 1;
        else if(rot == 1) dy = 1;
        else if(rot == 2) dx = -1;
        else dy = -1;

        for(int i = 1; i < laserRange; i++){
            Tile other = world.tile(tile.x + i * dx, tile.y + i * dy);
            if(other == null) continue;
            other = other.target();
            if(other.entity != null && other.entity.power != null){
                return other;
            }
        }
        return null;
    }

    @Override
    public void drawLayer(Tile tile){
        float powerFactor = tile.entity.power.graph != null && tile.entity.power.graph.hasPower() ? 1f : 0f;
        Draw.color(noPowerColor, color, powerFactor);

        for(int i = 0; i < laserDirections; i++){
            int dir = Mathf.mod(tile.getRotation() + i - laserDirections / 2, 4);
            Tile target = laserTarget(tile, dir);
            if(target == null || !tile.entity.power.links.contains(target.packedPosition())) continue;

            float lx = dirX(dir), ly = dirY(dir);
            float x1 = tile.worldx() + lx * tilesize / 2;
            float y1 = tile.worldy() + ly * tilesize / 2;
            float edge = target.block().size * tilesize / 2f;
            float x2 = target.worldx() - lx * edge;
            float y2 = target.worldy() - ly * edge;
            if(target.getX() == tile.x + (int)lx && target.getY() == tile.y + (int)ly){
                Draw.rect("classic-laserfull",
                        (x1 + x2) / 2f, (y1 + y2) / 2f,
                        Mathf.atan2(x2 - x1, y2 - y1));
            }else{
                Shapes.laser("classic-laser", "classic-laserend", x1, y1, x2, y2);
            }
        }

        Draw.reset();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        Draw.color(color);
        Lines.stroke(2f);

        for(int i = 0; i < laserDirections; i++){
            int dir = Mathf.mod(i + rotation - laserDirections / 2, 4);
            float lx = dirX(dir), ly = dirY(dir);
            Lines.dashLine(
                    x * tilesize + lx * tilesize / 2,
                    y * tilesize + ly * tilesize / 2,
                    x * tilesize + lx * laserRange * tilesize - lx * tilesize,
                    y * tilesize + ly * laserRange * tilesize - ly * tilesize, 9);
        }

        Draw.reset();
    }

    @Override
    public void drawSelect(Tile tile){
        Draw.color(color);
        Lines.stroke(2f);

        for(int i = 0; i < laserDirections; i++){
            int dir = Mathf.mod(i + tile.getRotation() - laserDirections / 2, 4);
            float lx = dirX(dir), ly = dirY(dir);
            Lines.dashLine(
                    tile.drawx() + lx * tilesize / 2,
                    tile.drawy() + ly * tilesize / 2,
                    tile.drawx() + lx * laserRange * tilesize - lx * tilesize,
                    tile.drawy() + ly * laserRange * tilesize - ly * tilesize, 9);
        }

        Draw.reset();
    }

    static float dirX(int dir){
        return dir == 0 ? 1f : dir == 2 ? -1f : 0f;
    }

    static float dirY(int dir){
        return dir == 1 ? 1f : dir == 3 ? -1f : 0f;
    }
}