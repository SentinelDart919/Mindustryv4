package io.anuke.mindustry.world.blocks.distribution;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.world.Tile;
import arc.util.Time;
import arc.graphics.g2d.Draw;
import arc.math.geom.Geometry;
import arc.util.Log;
import arc.math.Mathf;
import arc.math.geom.Vec2;

import static io.anuke.mindustry.Vars.itemSize;
import static io.anuke.mindustry.Vars.tilesize;

public class Veins extends Conveyor{//Conveinsyors now exist
    public float spawnTimer;
    public float minSpawnTimer;
    public float maxSpawnTimer;

    private TextureRegion[][] variantRegions = new TextureRegion[7][4];

    public Veins(String name){
        super(name);
        setAmbientSound(null);
        maxSpawnTimer = 15f;
        minSpawnTimer = 5f;
    }

    @Override
    public void load(){
        super.load();
        for(int i = 0; i < variantRegions.length; i++){
            for(int j = 0; j < 4; j++){
                variantRegions[i][j] = Core.atlas.find(name + "-" + i + "-" + j);
            }
        }
    }

    @Override
    public void draw(Tile tile){
        ConveyorEntity entity = tile.entity();
        byte rotation = tile.getRotation();

        int variant = Mathf.randomSeed(tile.id(), 0, 3);
        float scl = (entity.convey.size > 0) ? 1.2f : 1.0f;

        Draw.rect(variantRegions[Mathf.clamp(entity.blendbits, 0, variantRegions.length - 1)][variant],
            tile.drawx(), tile.drawy(),
            tilesize * entity.blendsclx * scl, tilesize * entity.blendscly * scl, rotation * 90);
    }

    @Override
    public void drawLayer(Tile tile){
        ConveyorEntity entity = tile.entity();
        byte rotation = tile.getRotation();

        try{
            Draw.color(Color.valueOf("ae070748"));
            for(int i = 0; i < entity.convey.size; i++){
                ItemPos pos = drawpos.set(entity.convey.get(i), ItemPos.drawShorts);

                if(pos.item == null) continue;

                tr1.trns(rotation * 90, tilesize, 0);
                tr2.trns(rotation * 90, -tilesize / 2f, pos.x * tilesize / 2f);
                Draw.rect(pos.item.region,
                        (int) (tile.x * tilesize + tr1.x * pos.y + tr2.x),
                        (int) (tile.y * tilesize + tr1.y * pos.y + tr2.y), itemSize, itemSize);
            }
            Draw.color();

        }catch(IndexOutOfBoundsException e){Log.err(e);}
    }

    @Override
    public void unitOn(Tile tile, Unit unit) {
    }

    @Override
    public void update(Tile tile) {
        super.update(tile);
        tile.infect();
    }
}
