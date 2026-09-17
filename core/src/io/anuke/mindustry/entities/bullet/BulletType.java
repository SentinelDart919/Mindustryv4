package io.anuke.mindustry.entities.bullet;

import com.badlogic.gdx.graphics.Color;
import io.anuke.mindustry.content.StatusEffects;
import io.anuke.mindustry.content.fx.BulletFx;
import io.anuke.mindustry.game.Content;
import io.anuke.mindustry.graphics.Shaders;
import io.anuke.mindustry.type.ContentType;
import io.anuke.mindustry.type.StatusEffect;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Effects.Effect;
import io.anuke.ucore.entities.impl.BaseBulletType;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Translator;

public abstract class BulletType extends Content implements BaseBulletType<Bullet>{
    public float lifetime;
    public float speed;
    public float damage;
    public float hitsize = 4;
    public float drawSize = 20f;
    public float drag = 0f;
    public boolean pierce;
    public Effect hiteffect, despawneffect;

    public float splashDamage = 0f;
    /**Knockback in velocity.*/
    public float knockback;
    /**Whether this bullet hits tiles.*/
    public boolean hitTiles = true;
    /**Status effect applied on hit.*/
    public StatusEffect status = StatusEffects.none;
    /**Intensity of applied status effect in terms of duration.*/
    public float statusIntensity = 0.5f;
    /**What fraction of armor is pierced, 0-1*/
    public float armorPierce = 0f;
    /**Whether to sync this bullet to clients.*/
    public boolean syncable;
    /**Whether this bullet type collides with tiles.*/
    public boolean collidesTiles = true;
    /**Whether this bullet type collides with tiles that are of the same team.*/
    public boolean collidesTeam = false;
    /**Whether this bullet type collides with air units.*/
    public boolean collidesAir = true;
    /**Whether this bullet types collides with anything at all.*/
    public boolean collides = true;
    /**Whether velocity is inherited from the shooter.*/
    public boolean keepVelocity = true;

    public boolean emitLight = false;
    public Color lightColor = Color.WHITE;
    public float lightRadius = 40f;
    public float lightOpacity = 0.5f;

    /**Whether this bullet type renders a bloom glow behind the sprite.*/
    public boolean bloom = false;

    protected Translator vector = new Translator();

    public BulletType(float speed, float damage){
        this.speed = speed;
        this.damage = damage;
        lifetime = 40f;
        hiteffect = BulletFx.hitBulletSmall;
        despawneffect = BulletFx.hitBulletSmall;
    }

    public boolean collides(Bullet bullet, Tile tile){
        return true;
    }

    public void hitTile(Bullet b, Tile tile){
        hit(b);
    }

    @Override
    public float drawSize(){
        return 40;
    }

    @Override
    public float lifetime(){
        return lifetime;
    }

    @Override
    public float speed(){
        return speed;
    }

    @Override
    public float damage(){
        return damage;
    }

    @Override
    public float hitSize(){
        return hitsize;
    }

    @Override
    public float drag(){
        return drag;
    }

    @Override
    public boolean pierce(){
        return pierce;
    }

    @Override
    public Effect hitEffect(){
        return hiteffect;
    }

    @Override
    public Effect despawnEffect(){
        return despawneffect;
    }

    @Override
    public void hit(Bullet b, float hitx, float hity){
        Effects.effect(hiteffect, hitx, hity, b.angle());
    }

    public void drawLight(Bullet b){
        boolean emit = b.emitLight != null ? b.emitLight : emitLight;
        float radius = b.lightRadius < 0 ? lightRadius : b.lightRadius;
        float opacity = b.lightOpacity < 0 ? lightOpacity : b.lightOpacity;
        Color color = b.lightColor != null ? b.lightColor : lightColor;

        if(emit && radius > 0.001f){
            Draw.color(color);
            Shaders.light.region = Draw.region("circle");
            Draw.alpha(opacity);
            Draw.rect("circle", b.x, b.y, radius * 2, radius * 2);
            Draw.alpha(opacity * 0.5f);
            Draw.rect("circle", b.x, b.y, radius * 2, radius * 2);
        }
    }

    /**Draws the bullet body into the bloom emission surface. The bullet's own sprite is used as the bloom source.*/
    public void drawBloom(Bullet b){
        draw(b);
    }

    @Override
    public void despawned(Bullet b){
        Effects.effect(despawneffect, b.x, b.y, b.angle());
    }

    @Override
    public ContentType getContentType(){
        return ContentType.bullet;
    }
}
