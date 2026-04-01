package io.anuke.mindustry.world.blocks.power;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.content.Liquids;
import io.anuke.mindustry.content.fx.BlockFx;
import io.anuke.mindustry.content.fx.ExplosionFx;
import io.anuke.mindustry.entities.Damage;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.production.GenericCrafter.GenericCrafterEntity;
import io.anuke.mindustry.world.meta.BlockStat;
import io.anuke.mindustry.world.meta.StatUnit;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Graphics;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Mathf;
import io.anuke.ucore.util.Translator;

import static io.anuke.mindustry.Vars.tilesize;
import static io.anuke.mindustry.sounds.Sounds.explosionReactor;

public class FusionReactor extends PowerGenerator{
    protected final Translator tr = new Translator();
    protected int plasmas = 4;
    protected float maxPowerProduced = 3f;
    protected float warmupSpeed = 0.001f;
    protected int explosionRadius = 36;
    protected int explosionDamage = 270;

    protected Color plasma1 = Color.valueOf("ffd06b"), plasma2 = Color.valueOf("ff361b");
    protected Color ind1 = Color.valueOf("858585"), ind2 = Color.valueOf("fea080");

    public FusionReactor(String name){
        super(name);
        hasPower = true;
        hasLiquids = true;
        powerCapacity = 150f;
        liquidCapacity = 30f;
        hasItems = true;
        itemCapacity = 20;

        consumes.item(Items.blastCompound);
        consumes.liquid(Liquids.cryofluid, 0.09f);
        //consumes.power(0.6f);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(BlockStat.basePowerGeneration, maxPowerProduced * 60f, StatUnit.powerSecond);
    }

    @Override
    public void update(Tile tile){
        FusionReactorEntity entity = tile.entity();

        if(tile.isEnemyCheat()){
            entity.items.add(consumes.item(), itemCapacity - entity.items.get(consumes.item()));
            entity.liquids.add(Liquids.cryofluid, liquidCapacity - entity.liquids.get(Liquids.cryofluid));
        }

        if(entity.cons.valid()){
            entity.warmup = Mathf.lerpDelta(entity.warmup, 1f, warmupSpeed);
        }else{
            entity.warmup = Mathf.lerpDelta(entity.warmup, 0f, 0.01f);
        }

        float powerAdded = Math.min(powerCapacity - entity.power.amount, maxPowerProduced * Mathf.pow(entity.warmup, 4f) * Timers.delta());
        entity.power.amount += powerAdded;
        entity.totalProgress += entity.warmup * Timers.delta();

        tile.entity.power.graph.update();
    }

    @Override
    public float handleDamage(Tile tile, float amount){
        FusionReactorEntity entity = tile.entity();

        if(entity.warmup < 0.4f) return amount;

        float healthFract = tile.entity.health / health;

        //5% chance to explode when hit at <50% HP with a normal bullet
        if(amount > 5f && healthFract <= 0.5f && Mathf.chance(0.05)){
            return health;
            //10% chance to explode when hit at <25% HP with a powerful bullet
        }else if(amount > 8f && healthFract <= 0.2f && Mathf.chance(0.1)){
            return health;
        }

        return amount;
    }

    @Override
    public void draw(Tile tile){
        FusionReactorEntity entity = tile.entity();

        Draw.rect(name + "-bottom", tile.drawx(), tile.drawy());

        Graphics.setAdditiveBlending();

        for(int i = 0; i < plasmas; i++){
            float r = 29f + Mathf.absin(Timers.time(), 2f + i * 1f, 5f - i * 0.5f);

            Draw.color(plasma1, plasma2, (float) i / plasmas);
            Draw.alpha((0.3f + Mathf.absin(Timers.time(), 2f + i * 2f, 0.3f + i * 0.05f)) * entity.warmup);
            Draw.rect(name + "-plasma-" + i, tile.drawx(), tile.drawy(), r, r, Timers.time() * (12 + i * 6f) * entity.warmup);
        }

        Draw.color();

        Graphics.setNormalBlending();

        Draw.rect(region, tile.drawx(), tile.drawy());

        Draw.rect(name + "-top", tile.drawx(), tile.drawy());

        Draw.color(ind1, ind2, entity.warmup + Mathf.absin(entity.totalProgress, 3f, entity.warmup * 0.5f));
        Draw.rect(name + "-light", tile.drawx(), tile.drawy());

        Draw.color();
    }

    @Override
    public TextureRegion[] getIcon(){
        return new TextureRegion[]{Draw.region(name + "-bottom"), Draw.region(name), Draw.region(name + "-top")};
    }

    @Override
    public TileEntity newEntity(){
        return new FusionReactorEntity();
    }

    @Override
    public void onDestroyed(Tile tile){
        super.onDestroyed(tile);

        FusionReactorEntity entity = tile.entity();

        if(entity.warmup < 0.4f) return;

        Effects.shake(6f, 16f, tile.worldx(), tile.worldy());
        Effects.effect(ExplosionFx.nuclearShockwave, tile.worldx(), tile.worldy());
        Sound sound = explosionReactor;
        if(Vars.soundController != null && sound != null){
            Vars.soundController.at(sound, tile.x, tile.y, 1f, 2.0f);}
        for(int i = 0; i < 6; i++){
            Timers.run(Mathf.random(40), () -> Effects.effect(BlockFx.nuclearcloud, tile.worldx(), tile.worldy()));
        }

        Damage.damage(tile.worldx(), tile.worldy(), explosionRadius * tilesize, explosionDamage * 4);


        for(int i = 0; i < 20; i++){
            Timers.run(Mathf.random(50), () -> {
                tr.rnd(Mathf.random(40f));
                Effects.effect(ExplosionFx.explosion, tr.x + tile.worldx(), tr.y + tile.worldy());
            });
        }

        for(int i = 0; i < 70; i++){
            Timers.run(Mathf.random(80), () -> {
                tr.rnd(Mathf.random(120f));
                Effects.effect(BlockFx.nuclearsmoke, tr.x + tile.worldx(), tr.y + tile.worldy());
            });
        }
    }

    public static class FusionReactorEntity extends GenericCrafterEntity{

    }
}
