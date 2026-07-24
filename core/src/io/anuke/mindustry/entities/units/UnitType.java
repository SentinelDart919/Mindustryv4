package io.anuke.mindustry.entities.units;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectSet;
import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.content.StatusEffects;
import io.anuke.mindustry.content.Weapons;
import io.anuke.mindustry.entities.traits.TypeTrait;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.game.UnlockableContent;
import io.anuke.mindustry.type.ContentType;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.type.StatusEffect;
import io.anuke.mindustry.type.Weapon;
import io.anuke.mindustry.ui.ContentDisplay;
import io.anuke.ucore.function.Supplier;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.scene.ui.layout.Table;
import io.anuke.ucore.util.Bundles;
import io.anuke.ucore.util.Log;
import io.anuke.ucore.util.Strings;

public class UnitType extends UnlockableContent{
    protected final Supplier<? extends BaseUnit> constructor;

    public final String name;
    public final String description;
    public float health = 60;
    public float hitsize = 7f;
    public float hitsizeTile = 4f;
    public float speed = 0.4f;
    public float range = 0, attackLength = 150, pursueRange = 250f, healRange = 200f;
    public float rotatespeed = 0.2f;
    public float baseRotateSpeed = 0.1f;
    public float mass = 1f;
    public boolean isFlying;
    public boolean isTank;
    public boolean living;
    public boolean isHealer;
    public boolean rotateWeapon = false;
    public boolean playerControllable = true;
    public boolean rtsAIControllable = true;
    public boolean targetAir = true;
    public boolean spawnsInSiegeMode = true;
    /** Cost for the ExtraSurvival Mode*/
    public int unitCost = 10;
    public float drag = 0.1f;
    public float maxVelocity = 5f;
    public float retreatPercent = 0.2f;
    public float armor = 0f;
    public float carryWeight = 1f;
    public int itemCapacity = 30;
    public float shootCone = 15f;
    public ObjectSet<Item> toMine = ObjectSet.with(Items.lead, Items.copper);
    public float buildPower = 0.3f, minePower = 0.7f;
    public Weapon weapon = Weapons.blaster;
    public float weaponOffsetX, weaponOffsetY;
    public float healTurretOffsetX, healTurretOffsetY;
    public Color trailColor = Color.valueOf("ffa665");
    public float engineOffsetX = 0f;
    public float engineOffsetY = -6f;
    public boolean engineMirror = false;
    public float engineSize = 5f;
    public ObjectSet<StatusEffect> immunities = new ObjectSet<>();

    public TextureRegion iconRegion, legRegion, trackRegion, baseRegion, region;

    public <T extends BaseUnit> UnitType(String name, Class<T> type, Supplier<T> mainConstructor){
        this.name = name;
        this.constructor = mainConstructor;
        this.description = Bundles.getOrNull("unit." + name + ".description");

        TypeTrait.registerType(type, mainConstructor);

        if(!Bundles.has("unit." + this.name + ".name")){
            Log.err("Warning: unit '" + name + "' is missing a localized name. Add the follow to bundle.properties:");
            Log.err("unit." + this.name + ".name=" + Strings.capitalize(name.replace('-', '_')));
        }
    }

    @Override
    public void displayInfo(Table table){
        ContentDisplay.displayUnit(table, this);
    }

    @Override
    public String localizedName(){
        return Bundles.get("unit." + name + ".name");
    }

    @Override
    public TextureRegion getContentIcon(){
        return iconRegion;
    }

    @Override
    public void load(){
        iconRegion = Draw.region("unit-icon-" + name);
        region = Draw.region(name);

        if(!isFlying){
            if(!isTank)legRegion = Draw.region(name + "-leg");
            baseRegion = Draw.region(name + "-base");
            if(isTank)trackRegion = Draw.region(name + "-track");
        }
    }

    @Override
    public ContentType getContentType(){
        return ContentType.unit;
    }

    @Override
    public String getContentName(){
        return name;
    }

    public BaseUnit create(Team team){
        BaseUnit unit = constructor.get();
        unit.init(this, team);
        return unit;
    }
}
