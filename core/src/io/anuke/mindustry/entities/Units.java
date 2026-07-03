package io.anuke.mindustry.entities;

import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import io.anuke.mindustry.entities.traits.TargetTrait;
import io.anuke.mindustry.entities.units.BaseUnit;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import arc.entities.EntityGroup;
import arc.entities.EntityQuery;
import arc.func.Cons;
import arc.func.Boolf;
import java.util.EnumSet;
import arc.math.geom.Geometry;

import static io.anuke.mindustry.Vars.*;

/**
 * Utility class for unit and team interactions.
 */
public class Units{
    private static Rect rect = new Rect();
    private static Rect rectGraphics = new Rect();
    private static Rect hitrect = new Rect();
    private static Unit result;
    private static float cdist;
    private static boolean boolResult, boolResultGraphics;

    /**
     * Validates a target.
     *
     * @param target The target to validate
     * @param team The team of the thing doing tha targeting
     * @param x The X position of the thing doign the targeting
     * @param y The Y position of the thing doign the targeting
     * @param range The maximum distance from the target X/Y the targeter can be for it to be valid
     * @return whether the target is invalid
     */
    public static boolean invalidateTarget(TargetTrait target, Team team, float x, float y, float range){
        return target == null || (range != Float.MAX_VALUE && target.distanceTo(x, y) > range) || target.getTeam() == team || !target.isValid();
    }

    /**See {@link #invalidateTarget(TargetTrait, Team, float, float, float)}*/
    public static boolean invalidateTarget(TargetTrait target, Team team, float x, float y){
        return invalidateTarget(target, team, x, y, Float.MAX_VALUE);
    }

    /**See {@link #invalidateTarget(TargetTrait, Team, float, float, float)}*/
    public static boolean invalidateTarget(TargetTrait target, Unit targeter){
        return invalidateTarget(target, targeter.team, targeter.x, targeter.y, targeter.getWeapon().getAmmo().getRange());
    }

    /**Returns whether there are any entities on this tile.*/
    public static boolean anyEntities(Tile tile){
        Block type = tile.block();
        rect.setSize(type.size * tilesize, type.size * tilesize);
        rect.setCenter(tile.drawx(), tile.drawy());

        return anyEntities(rect);
    }

    /**Can be called from any thread.*/
    public static boolean anyEntities(Rect rect){
        boolResult = false;

        Units.getNearby(rect, unit -> {
            if(boolResult) return;
            if(!unit.isFlying()){
                unit.getHitbox(hitrect);

                if(hitrect.overlaps(rect)){
                    boolResult = true;
                }
            }
        });

        return boolResult;
    }

    /**Returns whether there are any entities on this tile, with the hitbox expanded.*/
    public static boolean anyEntities(Tile tile, float expansion, Boolf<Unit> pred){
        Block type = tile.block();
        rect.setSize(type.size * tilesize + expansion, type.size * tilesize + expansion);
        rect.setCenter(tile.drawx(), tile.drawy());

        boolean[] value = new boolean[1];

        Units.getNearby(rect, unit -> {
            if(value[0] || !pred.get(unit) || unit.isDead()) return;
            if(!unit.isFlying()){
                unit.getHitbox(hitrect);

                if(hitrect.overlaps(rect)){
                    value[0] = true;
                }
            }
        });

        return value[0];
    }

    /**Returns the neareset damaged tile.*/
    public static TileEntity findDamagedTile(Team team, float x, float y){
        Tile tile = Geometry.findClosest(x, y, world.indexer.getDamaged(team));
        return tile == null ? null : tile.entity;
    }

    /**Returns the neareset ally tile in a range.*/
    public static TileEntity findAllyTile(Team team, float x, float y, float range, Boolf<Tile> pred){
        return world.indexer.findTile(team, x, y, range, pred);
    }

    /**Returns the neareset enemy tile in a range.*/
    public static TileEntity findEnemyTile(Team team, float x, float y, float range, Boolf<Tile> pred){
        for(Team enemy : state.teams.enemiesOf(team)){
            TileEntity entity = world.indexer.findTile(enemy, x, y, range, pred);
            if(entity != null){
                return entity;
            }
        }
        return null;
    }

    /**Iterates over all units on all teams, including players.*/
    public static void allUnits(Cons<Unit> cons){
        //check all unit groups first
        for(EntityGroup<BaseUnit> group : unitGroups){
            if(!group.isEmpty()){
                for(BaseUnit unit : group.all()){
                    cons.get(unit);
                }
            }
        }

        //then check all player groups
        for(Player player : playerGroup.all()){
            cons.get(player);
        }
    }

    /**Returns the closest target enemy. First, units are checked, then tile entities.*/
    public static TargetTrait getClosestTarget(Team team, float x, float y, float range){
        return getClosestTarget(team, x, y, range, u -> !u.isDead() && u.isAdded());
    }

    /**Returns the closest target enemy. First, units are checked, then tile entities.*/
    public static TargetTrait getClosestTarget(Team team, float x, float y, float range, Boolf<Unit> unitPred){
        Unit unit = getClosestEnemy(team, x, y, range, unitPred);
        if(unit != null){
            return unit;
        }else{
            return findEnemyTile(team, x, y, range, tile -> true);
        }
    }

    /**Returns the closest enemy of this team. Filter by Boolf.*/
    public static Unit getClosestEnemy(Team team, float x, float y, float range, Boolf<Unit> Boolf){
        result = null;
        cdist = 0f;

        rect.setSize(range * 2f).setCenter(x, y);

        getNearbyEnemies(team, rect, e -> {
            if(e.isDead() || !Boolf.get(e))
                return;

            float dist = Vec2.dst2(e.x, e.y, x, y);
            if(dist < range){
                if(result == null || dist < cdist){
                    result = e;
                    cdist = dist;
                }
            }
        });

        return result;
    }

    /**Returns the closest ally of this team. Filter by Boolf.*/
    public static Unit getClosest(Team team, float x, float y, float range, Boolf<Unit> Boolf){
        result = null;
        cdist = 0f;

        rect.setSize(range * 2f).setCenter(x, y);

        getNearby(team, rect, e -> {
            if(!Boolf.get(e))
                return;

            float dist = Vec2.dst2(e.x, e.y, x, y);
            if(dist < range){
                if(result == null || dist < cdist){
                    result = e;
                    cdist = dist;
                }
            }
        });

        return result;
    }

    /**Iterates over all units in a Rect.*/
    public static void getNearby(Team team, Rect rect, Cons<Unit> cons){

        EntityGroup<BaseUnit> group = unitGroups[team.ordinal()];
        if(!group.isEmpty()){
            EntityQuery.getNearby(group, rect, entity -> cons.get((Unit) entity));
        }

        //now check all players
        EntityQuery.getNearby(playerGroup, rect, player -> {
            if(((Unit) player).team == team) cons.get((Unit) player);
        });
    }

    /**Iterates over all units in a circle around this position.*/
    public static void getNearby(Team team, float x, float y, float radius, Cons<Unit> cons){
        rect.setSize(radius * 2).setCenter(x, y);

        EntityGroup<BaseUnit> group = unitGroups[team.ordinal()];
        if(!group.isEmpty()){
            EntityQuery.getNearby(group, rect, entity -> {
                if(entity.distanceTo(x, y) <= radius){
                    cons.get((Unit) entity);
                }
            });
        }

        //now check all players
        EntityQuery.getNearby(playerGroup, rect, player -> {
            if(((Unit) player).team == team && player.distanceTo(x, y) <= radius){
                cons.get((Unit) player);
            }
        });
    }

    /**Iterates over all units in a Rect.*/
    public static void getNearby(Rect rect, Cons<Unit> cons){

        for(Team team : Team.all){
            EntityGroup<BaseUnit> group = unitGroups[team.ordinal()];
            if(!group.isEmpty()){
                EntityQuery.getNearby(group, rect, entity -> cons.get((Unit) entity));
            }
        }

        //now check all enemy players
        EntityQuery.getNearby(playerGroup, rect, player -> cons.get((Unit) player));
    }

    /**Iterates over all units that are enemies of this team.*/
    public static void getNearbyEnemies(Team team, Rect rect, Cons<Unit> cons){
        EnumSet<Team> targets = state.teams.enemiesOf(team);

        for(Team other : targets){
            EntityGroup<BaseUnit> group = unitGroups[other.ordinal()];
            if(!group.isEmpty()){
                EntityQuery.getNearby(group, rect, entity -> cons.get((Unit) entity));
            }
        }

        //now check all enemy players
        EntityQuery.getNearby(playerGroup, rect, player -> {
            if(targets.contains(((Player) player).team)){
                cons.get((Unit) player);
            }
        });
    }

    /**Iterates over all units.*/
    public static void getAllUnits(Cons<Unit> cons){

        for(Team team : Team.all){
            EntityGroup<BaseUnit> group = unitGroups[team.ordinal()];
            for(Unit unit : group.all()){
                cons.get(unit);
            }
        }

        //now check all enemy players
        for(Unit unit : playerGroup.all()){
            cons.get(unit);
        }
    }

}
