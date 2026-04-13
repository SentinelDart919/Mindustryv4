package io.anuke.mindustry.world.blocks.distribution;

import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.util.Geometry;
import io.anuke.ucore.util.Mathf;
/*TODO
*  Update Draw make it bigger when it has items or change block color when have items
*  idk something else maybe
*
* */
public class Veins extends Conveyor{//Conveinsyors now exist
    public float spawnTimer;
    public float minSpawnTimer;
    public float maxSpawnTimer;
    public Veins(String name){
        super(name);
        setAmbientSound(null);
        maxSpawnTimer = 15f;
        minSpawnTimer = 5f;
    }

    @Override
    public void unitOn(Tile tile, Unit unit) {
    }
    @Override
    public void update(Tile tile) {
        super.update(tile);
        tile.infect();
        spawnTimer += Timers.delta();
        if (spawnTimer >= Mathf.random(minSpawnTimer, maxSpawnTimer) * 60f){
            int amount = Mathf.random(2, 8);
                for(int i = 0; i < 8; i++){
                    if(Mathf.random(8 - i - 1) < amount){
                    amount--;
                    Tile other = tile.getNearby(Geometry.d8[i]);
                    if(other != null) other.infect();
                }
            }
        }
    }
}
