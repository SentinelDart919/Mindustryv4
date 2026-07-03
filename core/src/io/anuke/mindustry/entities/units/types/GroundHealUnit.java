package io.anuke.mindustry.entities.units.types;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Rect;
import arc.struct.ObjectSet;
import arc.util.Timers;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.entities.Units;
import io.anuke.mindustry.entities.units.BaseUnit;
import io.anuke.mindustry.entities.units.GroundUnit;
import io.anuke.mindustry.game.GameMode;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.world.blocks.Floor;
import io.anuke.mindustry.world.meta.BlockFlag;
import arc.util.Time;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Shapes;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Log;

public class GroundHealUnit extends GroundUnit {
    private static Rect rect = new Rect();
    private static ObjectSet<String> warnedMissingHealTurretRegions = new ObjectSet<>();

    public boolean healTurretMirror = false;
    public float healSpeed = 0.3f;

    protected static int timerHeal = timerIndex++;
    protected Unit healTarget;
    protected float healStrength, healRotation = 90;
    protected TextureRegion healTurretRegion;

    @Override
    public void init(io.anuke.mindustry.entities.units.UnitType type, Team team) {
        super.init(type, team);

        // checks if there is regions and drops log, this may look silly but is for remind me these units are pretty volatile (crash when loading game)
        String turretRegionName = type.name + "-heal-turret";
        TextureRegion fallback = type.region != null ? type.region : Core.atlas.find("clear");
        if (!Core.atlas.has(turretRegionName) && warnedMissingHealTurretRegions.add(turretRegionName)) {
            Log.err("Missing texture region: '" + turretRegionName + " add it to sprites atlas or update it");
        }
        healTurretRegion = Core.atlas.find(turretRegionName, fallback);
    }

    @Override
    public void update() {
        super.update();

        if (healTarget != null && (healTarget.isDead() || dst(healTarget) > type.healRange ||
                healTarget.health >= healTarget.maxHealth())) {
            healTarget = null;
        } else if (healTarget != null) {
            healTarget.health += healSpeed * Timers.delta() * healStrength;
            healTarget.clampHealth();
            healRotation = Mathf.slerpDelta(healRotation, angleTo(healTarget), 0.5f);
        } else {
            healRotation = Mathf.slerpDelta(healRotation, rotation, 0.2f);
        }

        if (healTarget != null) {
            healStrength = Mathf.lerpDelta(healStrength, 1f, 0.08f * Timers.delta());
        } else {
            healStrength = Mathf.lerpDelta(healStrength, 0f, 0.07f * Timers.delta());
        }

        if (getTimer().get(timerHeal, 20)) {
            healTarget = Units.getClosest(getTeam(), x, y, type.healRange,
                    unit -> unit.health < unit.maxHealth() && unit != this);
        }
    }

    @Override
    public void behavior() {
        boolean isRedNonPvp = getTeam() == Team.red && !Vars.state.mode.isPvp && Vars.state.mode != GameMode.customAttackMode;
        //this should make sure healer units of the red team follow units and not avoid units or turrets on survival attack if not I fucked it up
        if (isRedNonPvp) {
            Unit ally = Units.getClosest(getTeam(), x, y, 400f, u -> u != this && !u.isFlying());
            if (ally != null) {
                if (dst(ally) > 40f) {
                    moveTo(ally.x, ally.y);
                }
            } else {
                super.behavior();
            }
        } else {// makes the healer to avoid dangers

            final float avoidDistUnit = 250f;
            final float avoidDistTurret = 350f;
            final float searchDist = 450f;

            final float[] targetX = {0}, targetY = {0};
            final int[] count = {0};

            // Units
            rect.setSize(searchDist * 2f).setCenter(x, y);
            Units.getNearbyEnemies(getTeam(), rect, enemy -> {
                float dist = dst(enemy);
                if (dist < avoidDistUnit) {
                    float ang = angleTo(enemy);
                    targetX[0] -= Angles.trnsx(ang, avoidDistUnit - dist);
                    targetY[0] -= Angles.trnsy(ang, avoidDistUnit - dist);
                    count[0]++;
                }
            });

            // Turrets
            TileEntity turret = Units.findEnemyTile(getTeam(), x, y, searchDist, t -> t.block().flags != null && t.block().flags.contains(BlockFlag.turret));
            if (turret != null) {
                float dist = dst(turret);
                if (dist < avoidDistTurret) {
                    float ang = angleTo(turret);
                    targetX[0] -= Angles.trnsx(ang, avoidDistTurret - dist);
                    targetY[0] -= Angles.trnsy(ang, avoidDistTurret - dist);
                    count[0]++;
                }
            }

            if (count[0] > 0) {
                moveTo(x + targetX[0], y + targetY[0]);
            } else {
                Unit lowHealthAlly = Units.getClosest(getTeam(), x, y, 400f, u -> u.health < u.maxHealth() && u != this && !u.isFlying());
                if (lowHealthAlly != null) {
                    if (dst(lowHealthAlly) > type.healRange * 0.5f) {
                        moveTo(lowHealthAlly.x, lowHealthAlly.y);
                    }
                } else {
                    idleFollow();
                }
            }
        }
    }

    protected void idleFollow() {
        Unit ally = Units.getClosest(getTeam(), x, y, 500f, u -> u != this && !u.isFlying() && !u.isHealer());
        if (ally != null) {
            float dist = dst(ally);
            float followDist = 70f;
            if (dist > followDist) {
                moveTo(ally.x, ally.y);
            } else if (dist < followDist - 20f) {
                float ang = angleTo(ally);
                moveTo(x - Angles.trnsx(ang, 20f), y - Angles.trnsy(ang, 20f));
            }
        } else {
            super.behavior();
        }
    }

    @Override
    public void draw() {
        super.draw();

        if (healTurretRegion == null) {
            String turretRegionName = type.name + "-heal-turret";
            TextureRegion fallback = type.region != null ? type.region : Core.atlas.find("clear");
            healTurretRegion = Core.atlas.find(turretRegionName, fallback);
        }// this should fix Crash of not region detected when loading game

        Draw.alpha(hitTime / hitDuration);
        for (int i : Mathf.signs) {
            if (i < 0 && !healTurretMirror) continue;

            float tx = x + Angles.trnsx(rotation - 90, type.healTurretOffsetX * i, type.healTurretOffsetY);
            float ty = y + Angles.trnsy(rotation - 90, type.healTurretOffsetX * i, type.healTurretOffsetY);

            if (healTurretRegion != null && healTurretRegion.texture != null) {
                Draw.rect(healTurretRegion, tx, ty, healRotation - 90);
            }

            if (healTarget != null && healStrength > 0.01f &&
                    Angles.angleDist(angleTo(healTarget), healRotation) < 30f) {
                float ang = angleTo(healTarget);
                float len = 5f;

                Draw.color(Color.valueOf("70f17f"));
                Shapes.laser("laser", "laser-end",
                        tx + Angles.trnsx(ang, len), ty + Angles.trnsy(ang, len),
                        healTarget.x, healTarget.y, healStrength);
                Draw.color();
            }
        }
    }
}
