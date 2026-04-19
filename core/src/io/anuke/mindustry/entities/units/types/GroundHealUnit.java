package io.anuke.mindustry.entities.units.types;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
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
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.graphics.Shapes;
import io.anuke.ucore.util.Angles;
import io.anuke.ucore.util.Mathf;

public class GroundHealUnit extends GroundUnit {
    private static Rectangle rect = new Rectangle();

    public float healTurretX = 0f, healTurretY = 0f;
    public boolean healTurretMirror = false;
    public float healSpeed = 0.3f;

    protected static int timerHeal = timerIndex++;
    protected Unit healTarget;
    protected float healStrength, healRotation = 90;
    protected TextureRegion healTurretRegion;

    @Override
    public void init(io.anuke.mindustry.entities.units.UnitType type, Team team) {
        super.init(type, team);
        healTurretRegion = Draw.region(type.name + "-heal-turret");
    }

    @Override
    public void update() {
        super.update();

        if (healTarget != null && (healTarget.isDead() || distanceTo(healTarget) > type.healRange ||
                healTarget.health >= healTarget.maxHealth())) {
            healTarget = null;
        } else if (healTarget != null) {
            healTarget.health += healSpeed * Timers.delta() * healStrength;
            healTarget.clampHealth();
            healRotation = Mathf.slerpDelta(healRotation, angleTo(healTarget), 0.5f);
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
                if (distanceTo(ally) > 40f) {
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
                float dist = distanceTo(enemy);
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
                float dist = distanceTo(turret);
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
                    if (distanceTo(lowHealthAlly) > type.healRange * 0.5f) {
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
            float dist = distanceTo(ally);
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

        for (int i : Mathf.signs) {
            if (i < 0 && !healTurretMirror) continue;

            float tx = x + Angles.trnsx(rotation - 90, healTurretX * i, healTurretY);
            float ty = y + Angles.trnsy(rotation - 90, healTurretX * i, healTurretY);

            Draw.rect(healTurretRegion, tx, ty, healRotation - 90);

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
