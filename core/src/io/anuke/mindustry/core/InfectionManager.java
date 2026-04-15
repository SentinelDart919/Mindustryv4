package io.anuke.mindustry.core;

import com.badlogic.gdx.utils.IntSet;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.modules.Module;
import io.anuke.ucore.util.Geometry;
import io.anuke.ucore.util.Mathf;

public class InfectionManager extends Module {
    private IntSet infectedQueue = new IntSet();
    private IntSet nextQueue = new IntSet();
    private float timer;
    private static final float INTERVAL = 60f * 1.5f;
    private static final float BASE_CHANCE = 0.05f;
    private static final float NEIGHBOR_MULTIPLIER = 0.15f;

    public void infect(Tile tile) {
        if (tile == null || tile.isInfected) return;
        
        infectInternal(tile);
        infectedQueue.add(tile.packedPosition());
    }

    private void infectInternal(Tile tile) {
        tile.isInfected = true;

        if (tile.floor().infectedVariant != null) {
            tile.setFloor(tile.floor().infectedVariant);
        }
        
        if (tile.block() instanceof io.anuke.mindustry.world.blocks.Rock) {
            io.anuke.mindustry.world.blocks.Rock rock = (io.anuke.mindustry.world.blocks.Rock) tile.block();
            if (rock.infectedVariant != null) {
                tile.setBlock(rock.infectedVariant);
            }
        }
    }

    @Override
    public void update() {
        if (Vars.state.isPaused() || infectedQueue.size == 0) return;

        timer += Timers.delta();
        if (timer >= INTERVAL) {
            timer = 0;
            spread();
        }
    }

    private void spread() {
        if (infectedQueue.size == 0) return;

        nextQueue.clear();
        
        IntSet.IntSetIterator it = infectedQueue.iterator();
        while (it.hasNext) {
            int packed = it.next();
            Tile tile = Vars.world.tile(packed);
            if (tile == null) {
                continue;
            }

            boolean hasUninfectedNeighbor = false;

            int start = Mathf.random(7);
            for (int i = 0; i < 8; i++) {
                Tile other = tile.getNearby(Geometry.d8[(i + start) % 8]);
                if (other != null && !other.isInfected) {
                    if (canInfect(other)) {
                        hasUninfectedNeighbor = true;

                        int infectedNeighbors = 0;
                        for (int j = 0; j < 8; j++) {
                            Tile n = other.getNearby(Geometry.d8[j]);
                            if (n != null && n.isInfected) {
                                infectedNeighbors++;
                            }
                        }

                        float chance = BASE_CHANCE + (infectedNeighbors * NEIGHBOR_MULTIPLIER);
                        if (Mathf.chance(chance)) {

                            if (other != null && !other.isInfected) {
                                infectInternal(other);
                                nextQueue.add(other.packedPosition());
                                
                                break;
                            }
                        }
                    }
                }
            }

            if (hasUninfectedNeighbor) {
                nextQueue.add(packed);
            }
        }
        
        infectedQueue.clear();
        infectedQueue.addAll(nextQueue);
    }

    private boolean canInfect(Tile tile) {
        if (tile == null || tile.isInfected) return false;
        if (tile.floor().infectedVariant != null) return true;
        if (tile.block() instanceof io.anuke.mindustry.world.blocks.Rock) {
            io.anuke.mindustry.world.blocks.Rock rock = (io.anuke.mindustry.world.blocks.Rock) tile.block();
            return rock.infectedVariant != null;
        }
        return false;
    }
    
    public IntSet getInfectedQueue() {
        return infectedQueue;
    }

    public void reset() {
        infectedQueue.clear();
        nextQueue.clear();
        timer = 0;
    }
}
