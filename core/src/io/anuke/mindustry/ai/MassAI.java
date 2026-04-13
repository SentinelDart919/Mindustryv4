package io.anuke.mindustry.ai;

import com.badlogic.gdx.utils.*;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.content.blocks.DistributionBlocks;
import io.anuke.mindustry.content.blocks.ProductionBlocks;
import io.anuke.mindustry.content.blocks.StorageBlocks;
import io.anuke.mindustry.game.EventType;
import io.anuke.mindustry.game.EventType.WorldLoadEvent;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.content.blocks.UnitBlocks;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.modules.ItemModule;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Events;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.util.Geometry;
import io.anuke.ucore.util.Mathf;

import static io.anuke.mindustry.Vars.world;
/* TODO make them spawn Units,
    Make them more aggressive - (add vein like system that goes directly to enemy blocks and damage them)
    Finish the Mass Like buildings (mostly looking like the The Flesh That Hates mod from MC)
    Make them spawn turrets when they are under attack - add a trySpawnTurret, also does it randomly
    make them randomly spawn turrets
    Their Units should Defend the Hive Cores - command blocks have retreat / attack / patrol setting my idea is to make the enemy cores run this
    Add Grace time
    Make them have more units, turrets, and ore multiplier by difficulty
*/
public class MassAI {
    private static ObjectSet<Tile> initializedCores = new ObjectSet<>();
    private static Array<BuildingLine> activeLines = new Array<>();
    private static Array<SubSection> activeSubsections = new Array<>();
    private static float spawnTimer = 0;
    private static final Item[] targetOres = {Items.scrap, Items.lead, Items.copper, Items.coal, Items.titanium, Items.thorium, Items.chromium};
    private static final int[] oreLimits = {6, 6, 6, 5, 4, 4, 4};
    private static int[] oreBoosts = new int[7];
    private static ObjectMap<Tile, Boolean> coreExpanded = new ObjectMap<>();

    static {
        Events.on(WorldLoadEvent.class, event -> {
            initializedCores.clear();
            activeLines.clear();
            activeSubsections.clear();
            coreExpanded.clear();
            spawnTimer = 0;
            for (int i = 0; i < oreBoosts.length; i++) oreBoosts[i] = 0;
        });
    }

    public static void update() {
        if (Vars.state.isPaused() || Vars.state.teams == null) return;

        ObjectSet<Tile> cores = Vars.state.teams.get(Team.themass).cores;
        
        // Remove tracking for destroyed cores
        ObjectSet.ObjectSetIterator<Tile> it = initializedCores.iterator();
        while (it.hasNext) {
            Tile tile = it.next();
            if (!cores.contains(tile)) {
                it.remove();
                coreExpanded.remove(tile);
            }
        }

        // Remove lines belonging to destroyed cores
        for(int i = activeLines.size - 1; i >= 0; i--){
            if(!cores.contains(activeLines.get(i).core)){
                activeLines.removeIndex(i);
            }
        }

        for (Tile core : cores) {
            if (!initializedCores.contains(core)) {
                startBuilding(core);
                initializedCores.add(core);
            }
        }

        for(int i = activeLines.size - 1; i >= 0; i--){
            BuildingLine line = activeLines.get(i);
            line.update();
        }

        spawnTimer += Timers.delta();
        if (spawnTimer >= 5f * 60f) {
            spawnTimer = 0;
            trySpawnSubsection();
            checkCoreExpansion();
            trySpawnSpawners();
        }

        for (int i = activeSubsections.size - 1; i >= 0; i--) {
            SubSection s = activeSubsections.get(i);
            s.update();
            if (s.failed) {
                activeSubsections.removeIndex(i);
            }
        }
    }

    private static void trySpawnSubsection() {
        int coreCount = Vars.state.teams.get(Team.themass).cores.size;
        for (int i = 0; i < targetOres.length; i++) {
            Item ore = targetOres[i];
            int limit = oreLimits[i] * coreCount;
            
            int activeDrills = 0;
            int building = 0;
            for (SubSection s : activeSubsections) {
                if (s.targetOre == ore) {
                    if (s.drillPlaced) activeDrills++;
                    else building++;
                }
            }

            if (activeDrills + building < limit) {
                // If 0 active drills or boosted, try to spawn up to 2 at once if is possible
                int toSpawn = (activeDrills == 0 || oreBoosts[i] > 0) ? 2 : 1;
                if (oreBoosts[i] > 0) oreBoosts[i]--;
                
                for (int j = 0; j < toSpawn; j++) {
                    if (activeDrills + building < limit) {
                        if (spawnForOre(ore)) {
                            building++;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    private static void trySpawnSpawners() {
        for (Tile core : Vars.state.teams.get(Team.themass).cores) {
            ItemModule items = core.entity.items;

            int hiveSpawners = 0;
            int airSpawners = 0;
            int radius = 30;

            // count existing spawners near the core
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    Tile t = world.tile(core.x + dx, core.y + dy);
                    if (t != null && t.getTeam() == Team.themass) {
                        if (t.block() == UnitBlocks.hiveSpawner) hiveSpawners++;
                        else if (t.block() == UnitBlocks.airHiveSpawner) airSpawners++;
                    }
                }
            }
            // max 10 spawn per core (if not welcome to unbalanced hell)
            if (hiveSpawners < 10 && items.has(Items.copper, 10)) {
                if (placeRandomSpawner(core, UnitBlocks.hiveSpawner, radius)) {
                    items.remove(Items.copper, 10);
                }
            }

            if (airSpawners < 10 && items.has(Items.lead, 10)) {
                if (placeRandomSpawner(core, UnitBlocks.airHiveSpawner, radius)) {
                    items.remove(Items.lead, 10);
                }
            }
        }
    }

    private static boolean placeRandomSpawner(Tile core, Block spawner, int radius) {
        for (int i = 0; i < 40; i++) {
            int tx = core.x + Mathf.random(-radius, radius);
            int ty = core.y + Mathf.random(-radius, radius);
            Tile target = world.tile(tx, ty);

            if (target != null && target.block() == Blocks.air && target.floor().placeableOn && !target.floor().isLiquid) {
                boolean occluded = false;
                int size = spawner.size;
                int offset = -(size - 1) / 2;
                
                for (int dx = 0; dx < size; dx++) {
                    for (int dy = 0; dy < size; dy++) {
                        Tile t = world.tile(tx + offset + dx, ty + offset + dy);
                        if (t == null || t.block() != Blocks.air || t.floor().isLiquid || !t.floor().placeableOn) {
                            occluded = true;
                            break;
                        }
                    }
                    if (occluded) break;
                }

                if (!occluded) {
                    Vars.world.setBlock(target, spawner, Team.themass);
                    return true;
                }
            }
        }
        return false;
    }

    private static void checkCoreExpansion() {
        if (Vars.state.teams.get(Team.themass).cores.size == 10) return;

        for (Tile core : Vars.state.teams.get(Team.themass).cores) {
            if (coreExpanded.get(core, false)) continue;

            Array<BuildingLine> lines = new Array<>();
            for (BuildingLine line : activeLines) {
                if (line.core == core) {
                    lines.add(line);
                }
            }

            if (lines.size > 0) {
                // Try to expand
                if (tryExpandCore(core, lines)) {
                    coreExpanded.put(core, true);
                }
            }
        }
    }

    private static boolean tryExpandCore(Tile core, Array<BuildingLine> lines) {
        if (!core.entity.items.has(Items.copper, 20) || !core.entity.items.has(Items.lead, 20)) return false; // they will consume more,

        // Shuffle lines to pick a random one that works
        // They still spawning the core in random places so this is mostly useless
        lines.shuffle();
        
        Block hive = StorageBlocks.hive;
        int size = hive.size;
        int offset = -(size - 1) / 2;
        
        for (BuildingLine line : lines) {
            if (line.tiles.size == 0) continue;
            
            // Prioritize tiles further from the core
            Array<PathTile> lineTiles = new Array<>(line.tiles);
            lineTiles.sort((a, b) -> {
                float d1 = Mathf.dst(a.tile.x - core.x, a.tile.y - core.y);
                float d2 = Mathf.dst(b.tile.x - core.x, b.tile.y - core.y);
                return Float.compare(d2, d1);
            });
            
            for (int i = 0; i < Math.min(lineTiles.size, 20); i++) {
                Tile base = lineTiles.get(i).tile;
                
                // If the line tile itself is near void/edge, skip it
                if (isNearEdge(base, 5)) continue;
                
                // Try several random offsets from this line tile
                for (int j = 0; j < 20; j++) {
                    int tx = base.x + Mathf.random(-30, 30);
                    int ty = base.y + Mathf.random(-30, 30);
                    
                    if (canPlaceCore(tx, ty)) {
                        core.entity.items.remove(Items.copper, 20);
                        core.entity.items.remove(Items.lead, 20);
                        
                        Tile target = world.tile(tx, ty);
                        
                        // clears the area to set the core (this useless but well in case of a bug or something)
                        for (int dx = 0; dx < size; dx++) {
                            for (int dy = 0; dy < size; dy++) {
                                Tile t = world.tile(tx + offset + dx, ty + offset + dy);
                                if (t != null && t.block() != Blocks.air) {
                                    world.removeBlock(t);
                                }
                            }
                        }
                        
                        Vars.world.setBlock(target, hive, Team.themass);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean canPlaceCore(int x, int y) {
        Block hive = StorageBlocks.hive;
        int size = hive.size;
        int offset = -(size - 1) / 2;
        
        // Ensure not too close to map edge (they're still placing cores near edges, but this is mostly to prevent them to spawn DIRECTLY on the edge)
        if (x + offset < 5 || y + offset < 5 || x + offset + size > world.width() - 5 || y + offset + size > world.height() - 5) return false;

        for (int dx = 0; dx < size; dx++) {
            for (int dy = 0; dy < size; dy++) {
                Tile t = world.tile(x + offset + dx, y + offset + dy);
                if (t == null || isNearEnemyCore(t) || isNearMassCore(t, 75)) return false;
                
                // void/Liquid check
                if (t.floor().isLiquid || !t.floor().placeableOn) return false;
                
                // allow placing over any themass building EXCEPT other cores and buildings from other teams
                if (t.block() != Blocks.air) {
                    if (t.getTeam() != Team.themass) return false;
                    if (t.block().isMultiblock() && t.target().block() instanceof io.anuke.mindustry.world.blocks.storage.CoreBlock) return false;
                    if (t.block() instanceof io.anuke.mindustry.world.blocks.storage.CoreBlock) return false;
                }
            }
        }
        return true;
    }

    private static boolean spawnForOre(Item ore) {
        Tile oreTile = find2x2Ore(ore);
        if (oreTile == null) return false;

        Array<Tile> p = findPathToAnyLine(oreTile);
        if (p != null && p.size > 1) {
            Tile startTile = p.get(0);
            Array<Tile> path = new Array<>();
            for (int i = 1; i < p.size; i++) {
                path.add(p.get(i));
            }

            activeSubsections.add(new SubSection(startTile, oreTile, ore, path));
            for (BuildingLine line : activeLines) {
                if (line.containsTile(startTile)) {
                    line.lastSubsectionTimer = 0;
                    break;
                }
            }
            return true;
        }
        return false;
    }

    private static Array<Tile> findPathToAnyLine(Tile target) {
        Queue<Tile> queue = new Queue<>();
        ObjectMap<Tile, Tile> parents = new ObjectMap<>();
        queue.addLast(target);
        parents.put(target, null);

        while (!queue.isEmpty()) {
            Tile curr = queue.removeFirst();

            //
            if (curr.block() == DistributionBlocks.veins && curr.getTeam() == Team.themass) {
                boolean isLine = false;
                for (BuildingLine line : activeLines) {
                    if (line.containsTile(curr)) {
                        isLine = true;
                        break;
                    }
                }

                if (isLine) {
                    Array<Tile> p = new Array<>();
                    Tile node = curr;
                    while (node != null) {
                        p.add(node);
                        node = parents.get(node);
                    }
                    return p;
                }
            }

            for (int i = 0; i < 4; i++) {
                Tile next = curr.getNearby(Geometry.d4[i]);
                if (next != null && !parents.containsKey(next) && isPassable(next)) {
                    parents.put(next, curr);
                    queue.addLast(next);
                    if (parents.size > 2000) return null;
                }
            }
        }
        return null;
    }

    private static Tile find2x2Ore(Item item) {
        Tile bestTile = null;
        float bestDist = Float.MAX_VALUE;

        ObjectSet<Tile> orePositions = Vars.world.indexer.getOrePositions(item);
        if (orePositions.size == 0) return null;

        // Find nearest line/core for each ore quadrant
        for (Tile quadTile : orePositions) {
            float minSourceDist = Float.MAX_VALUE;
            
            // check distance to cores
            for (Tile core : Vars.state.teams.get(Team.themass).cores) {
                float dist = Mathf.dst(core.x - quadTile.x, core.y - quadTile.y);
                if (dist < minSourceDist) minSourceDist = dist;
            }
            
            // Check distance to all conveyor lines
            for (BuildingLine line : activeLines) {
                for (PathTile pt : line.tiles) {
                    float dist = Mathf.dst(pt.tile.x - quadTile.x, pt.tile.y - quadTile.y);
                    if (dist < minSourceDist) minSourceDist = dist;
                }
            }

            if (minSourceDist < bestDist) {
                // if quad is good search for random/nearby ores
                for (int x = quadTile.x - 10; x < quadTile.x + 10; x++) {
                    for (int y = quadTile.y - 10; y < quadTile.y + 10; y++) {
                        if (isValid2x2(x, y, item)) {
                            bestTile = world.tile(x, y);
                            bestDist = minSourceDist;
                            x = quadTile.x + 10;
                            break;
                        }
                    }
                }
            }
        }
        return bestTile;
    }

    private static void boostOre(Item ore) {
        for (int i = 0; i < targetOres.length; i++) {
            if (targetOres[i] == ore) {
                oreBoosts[i] = 2;
                break;
            }
        }
    }

    private static boolean isValid2x2(int x, int y, Item item) {
        for (int dx = 0; dx < 2; dx++) {
            for (int dy = 0; dy < 2; dy++) {
                Tile t = world.tile(x + dx, y + dy);
                if (t == null || t.floor().drops == null || t.floor().drops.item != item || isNearEnemyCore(t)) return false;
                
                Block block = t.block();
                if (block != Blocks.air && !(t.getTeam() == Team.themass && block == DistributionBlocks.veins)) {
                    return false;
                }
            }
        }
        return true;
    }
    // I can't believe this shit is working
    private static boolean isNearMassCore(Tile tile, float radius) {
        if (Vars.state.teams == null) return false;
        ObjectSet<Tile> cores = Vars.state.teams.get(Team.themass).cores;
        if (cores == null) return false;
        for (Tile core : cores) {
            if (Mathf.dst(tile.x - core.x, tile.y - core.y) < radius) return true;
        }
        return false;
    }

    private static boolean isNearEdge(Tile tile, int distance) {
        return tile.x < distance || tile.y < distance || tile.x > world.width() - distance || tile.y > world.height() - distance;
    }

    private static boolean isNearEnemyCore(Tile tile) {
        if (Vars.state.teams == null) return false;
        float radius = Vars.state.mode.enemyCoreBuildRadius;
        for (Team team : Team.values()) {
            if (team == Team.themass) continue;
            ObjectSet<Tile> cores = Vars.state.teams.get(team).cores;
            if (cores == null) continue;
            for (Tile core : cores) {
                if (Mathf.dst(tile.x - core.x, tile.y - core.y) < radius / Vars.tilesize) return true;
            }
        }
        return false;
    }

    private static boolean hasOresNearby(Tile tile, int radius) {
        for (Item item : targetOres) {
            Tile ore = Vars.world.indexer.findClosestOre(tile.worldx(), tile.worldy(), item);
            if (ore != null && Mathf.dst(tile.x - ore.x, tile.y - ore.y) < radius) return true;
        }
        return false;
    }

    private static Array<Tile> findPath(Tile start, Tile target) {
        Queue<Tile> queue = new Queue<>();
        ObjectMap<Tile, Tile> parents = new ObjectMap<>();
        queue.addLast(start);
        parents.put(start, null);

        while (!queue.isEmpty()) {
            Tile curr = queue.removeFirst();
            if (curr.x == target.x && curr.y == target.y) {
                Array<Tile> path = new Array<>();
                while (curr != start) {
                    path.add(curr);
                    curr = parents.get(curr);
                }
                path.reverse();
                return path;
            }

            for (int i = 0; i < 4; i++) {
                Tile next = curr.getNearby(Geometry.d4[i]);
                if (next != null && !parents.containsKey(next) && isPassable(next)) {
                    parents.put(next, curr);
                    queue.addLast(next);
                    if (parents.size > 2000) return null;
                }
            }
        }
        return null;
    }

    private static boolean isPassable(Tile tile) {
        return (tile.block() == Blocks.air || (tile.getTeam() == Team.themass && tile.block() == DistributionBlocks.veins)) && !isNearEnemyCore(tile);
    }

    private static void startBuilding(Tile core) {
        for (int i = 0; i < 4; i++) {
            activeLines.add(new BuildingLine(core, i));
        }
    }

    private static class SubSection {
        Tile startTile;
        final Tile targetTile;
        final Item targetOre;
        final Array<Tile> path;
        int progress = 0;
        float timer = 0;
        float stuckTimer = 0;
        boolean drillPlaced = false;
        boolean failed = false;
        boolean destroying = false;

        SubSection(Tile startTile, Tile targetTile, Item targetOre, Array<Tile> path) {
            this.startTile = startTile;
            this.targetTile = targetTile;
            this.targetOre = targetOre;
            this.path = path;
        }

        void update() {
            if (failed) return;

            if (destroying) {
                if (targetTile.block() == Blocks.air || targetTile.entity == null || targetTile.entity.isDead()) {
                    failed = true;
                    return;
                }
                targetTile.entity.damage(20000f);
                return;
            }

            if (drillPlaced) {
                if (targetTile.block() != ProductionBlocks.biomassBulb || targetTile.getTeam() != Team.themass) {
                    boostOre(targetOre);
                    failed = true;
                    return;
                }

                // check if drill is stuck
                if (targetTile.entity != null && targetTile.entity.items.total() >= targetTile.block().itemCapacity) {
                    stuckTimer += Timers.delta();
                    if (stuckTimer >= 15f * 60f) { // 15 seconds
                        // try to reconnect drill
                        Array<Tile> newPath = findPathToAnyLine(targetTile);
                        if (newPath != null && newPath.size > 1) {
                            this.startTile = newPath.get(0);
                            path.clear();
                            for (int k = 1; k < newPath.size; k++) {
                                path.add(newPath.get(k));
                            }
                            progress = 0;
                            drillPlaced = false;
                            stuckTimer = 0;
                        } else {
                            // FAILED to reconnect -> destroy the drill
                            destroying = true;
                            boostOre(targetOre);
                        }
                    }
                } else {
                    stuckTimer = 0;
                }
                return;
            }

            timer += Timers.delta();
            if (timer >= 60f) {
                timer = 0;
                if (progress < path.size) {
                    Tile next = path.get(progress);
                    if (isNearEnemyCore(next)) {
                        failed = true;
                        return;
                    }
                    if (next.block() == Blocks.air || (next.getTeam() == Team.themass && next.block() == DistributionBlocks.veins)) {
                        Tile prev = (progress == 0) ? startTile : path.get(progress - 1);
                        int rotation = next.relativeTo(prev.x, prev.y);
                        
                        next.setBlock(DistributionBlocks.veins, Team.themass, rotation);
                        progress++;
                    } else {
                        failed = true;
                    }
                } else {
                    if (isValid2x2(targetTile.x, targetTile.y, targetOre)) {
                        Vars.world.setBlock(targetTile, ProductionBlocks.biomassBulb, Team.themass);
                        drillPlaced = true;
                    } else if (targetTile.block() == ProductionBlocks.biomassBulb && targetTile.getTeam() == Team.themass) {
                        drillPlaced = true; // reconnect the drill
                    } else {
                        failed = true;
                    }
                }
            }
        }
    }

    private static class BuildingLine {
        final Tile core;
        final int direction;
        final int dx, dy, rotation;
        int startX, startY;
        int targetLength;
        int divisions = 0;
        boolean subdivided = false;
        Array<PathTile> tiles = new Array<>();
        Array<Tile> pathBuffer = new Array<>();
        float timer = 0;
        float lastSubsectionTimer = 0;
        IntIntMap attempts = new IntIntMap();
        IntSet gaveUp = new IntSet();

        BuildingLine(Tile core, int direction) {
            this(core, direction, -1, -1, 20);
            this.targetLength = 100;  // they sometimes ignore this, this works only on big maps
        }

        BuildingLine(Tile core, int direction, int sx, int sy, int divisions) {
            this.core = core;
            this.direction = direction;
            this.divisions = divisions;
            this.dx = Geometry.d4[direction].x;
            this.dy = Geometry.d4[direction].y;
            this.rotation = (direction + 2) % 4;
            this.targetLength = Mathf.random(40, 100);

            if (sx == -1) {
                int size = core.block().size;
                int offset = (size + 1) / 2;
                this.startX = core.x + dx * offset;
                this.startY = core.y + dy * offset;
            } else {
                this.startX = sx;
                this.startY = sy;
            }
        }

        void update() {
            timer += Timers.delta();
            lastSubsectionTimer += Timers.delta();

            if (timer >= 60f) {
                timer = 0;

                // Try to rebuild broken blocks first
                for (int i = 0; i < tiles.size; i++) {
                    if (gaveUp.contains(i)) continue;

                    if (isBroken(i)) {
                        if (tryRebuild(i)) {
                            attempts.put(i, 0);
                            return; // limits actions per second
                        } else {
                            int a = attempts.get(i, 0) + 1;
                            attempts.put(i, a);
                            if (a >= 5) {
                                gaveUp.add(i);
                                targetLength--;
                            }
                            return;
                        }
                    } else {
                        attempts.put(i, 0);
                    }
                }

                // if idle for too long, branch or grow the line
                // this SHOULD STOP BRANCHING OR GROWING WHEN REACHING THE LIMITS, but it doesn't - Fixed by adding limits to the limits was fucking annoying to see absolute mess of conveyors across the god damn map
                if (lastSubsectionTimer >= 30f * 60f) {
                    lastSubsectionTimer = 0;
                    if (divisions >= 3 && Mathf.chance(0.1) && tiles.size > 5) {
                        branch();
                    } else if (tiles.size < 100) {
                        targetLength = Math.min(100, targetLength + 10);
                    }
                }

                // Increase targetLength if no ores nearby
                if (tiles.size >= targetLength && targetLength < 100) {
                    Tile last = tiles.size == 0 ? world.tile(startX, startY) : tiles.peek().tile;
                    if (last != null && !hasOresNearby(last, 20)) {
                        targetLength = Math.min(100, targetLength + 10);
                    }
                }

                // If no rebuilding is needed, try to expand the line
                if (tiles.size < targetLength && tiles.size < 100) {
                    buildNext();
                }
            }
        }
        //Absolutely this shit is broken
        boolean isBroken(int i) {
            if (i < 0 || i >= tiles.size) return false;
            PathTile pt = tiles.get(i);
            Tile tile = pt.tile;
            return tile == null || tile.block() != DistributionBlocks.veins || tile.getTeam() != Team.themass || tile.getRotation() != pt.rotation;
        }

        boolean tryRebuild(int i) {
            PathTile pt = tiles.get(i);
            Tile tile = pt.tile;
            if (tile != null && tile.block() == Blocks.air) {
                tile.setBlock(DistributionBlocks.veins, Team.themass, pt.rotation);
                return true;
            }
            return false;
        }

        void buildNext() {
            if (pathBuffer.size > 0) {
                Tile next = pathBuffer.removeIndex(0);
                if (next.block() == Blocks.air && !isNearEnemyCore(next)) {
                    Tile prev = tiles.size == 0 ? world.tile(startX - dx, startY - dy) : tiles.peek().tile;
                    int rot = next.relativeTo(prev.x, prev.y);
                    next.setBlock(DistributionBlocks.veins, Team.themass, rot);
                    tiles.add(new PathTile(next, rot));
                } else {
                    pathBuffer.clear();
                }
                return;
            }

            int tx, ty;
            if (tiles.size == 0) {
                tx = startX;
                ty = startY;
            } else {
                Tile last = tiles.peek().tile;
                tx = last.x + dx;
                ty = last.y + dy;
            }

            if (tx < 0 || ty < 0 || tx >= world.width() || ty >= world.height()) {
                targetLength = tiles.size;
                return;
            }

            Tile tile = world.tile(tx, ty);
            if (tile == null || isNearEnemyCore(tile)) {
                targetLength = tiles.size;
                return;
            }

            if (tile.block() == Blocks.air) {
                tile.setBlock(DistributionBlocks.veins, Team.themass, rotation);
                tiles.add(new PathTile(tile, rotation));
            } else if (tile.block().solid) {
                // Try to pathfind around
                for (int jump = 3; jump <= 7; jump++) {
                    Tile target = world.tile(tx + dx * jump, ty + dy * jump);
                    if (target != null && !target.block().solid && !isNearEnemyCore(target)) {
                        Array<Tile> path = findPath(tiles.size == 0 ? world.tile(startX - dx, startY - dy) : tiles.peek().tile, target);
                        if (path != null) {
                            pathBuffer.addAll(path);
                            return;
                        }
                    }
                }
                // Subdivide (ERROR tried to divide by zero)
                subdivide();
                targetLength = tiles.size;
            }
        }

        /*boolean isAtLimit() { // useless
            return tiles.size >= 100 || (divisions < 3 && (tiles.size >= targetLength || isBlocked()));
        }*/

        boolean isBlocked() { // for future use
            if (tiles.size == 0) return false;
            Tile last = tiles.peek().tile;
            Tile next = world.tile(last.x + dx, last.y + dy);
            return next == null || next.block().solid || isNearEnemyCore(next);
        }

        void subdivide() {
            if (tiles.size == 0 || divisions < 3) return;
            if (!Mathf.chance(0.2)) {
                subdivided = true;
                return;
            }
            subdivided = true;
            Tile last = tiles.peek().tile;
            int d1 = (direction + 1) % 4;
            int d2 = (direction + 3) % 4;
            
            int tx1 = last.x + Geometry.d4[d1].x;
            int ty1 = last.y + Geometry.d4[d1].y;
            if (canStartLine(tx1, ty1)) {
                activeLines.add(new BuildingLine(core, d1, tx1, ty1, divisions - 3));
            }
            
            int tx2 = last.x + Geometry.d4[d2].x;
            int ty2 = last.y + Geometry.d4[d2].y;
            if (canStartLine(tx2, ty2)) {
                activeLines.add(new BuildingLine(core, d2, tx2, ty2, divisions - 3));
            }
        }

        boolean canStartLine(int x, int y) {
            Tile t = world.tile(x, y);
            return t != null && t.block() == Blocks.air && !isNearEnemyCore(t);
        }

        void branch() {
            if (tiles.size < 5 || divisions < 3) return;
            divisions -= 3;
            int index = Mathf.random(tiles.size / 2, tiles.size - 1);
            Tile base = tiles.get(index).tile;

            int d1 = (direction + 1) % 4;
            int d2 = (direction + 3) % 4;
            int dir = Mathf.choose(d1, d2);
            int tx = base.x + Geometry.d4[dir].x;
            int ty = base.y + Geometry.d4[dir].y;
            Tile next = world.tile(tx, ty);

            if (next != null && next.block() == Blocks.air && !isNearEnemyCore(next)) {
                activeLines.add(new BuildingLine(core, dir, tx, ty, divisions - 3));
            }
        }

        boolean containsTile(Tile tile) {
            if (world.tile(startX, startY) == tile) return true;
            for (PathTile pt : tiles) {
                if (pt.tile == tile) return true;
            }
            return false;
        }
    }

    private static class PathTile {
        final Tile tile;
        final int rotation;
        PathTile(Tile tile, int rotation) {
            this.tile = tile;
            this.rotation = rotation;
        }
    }
}
